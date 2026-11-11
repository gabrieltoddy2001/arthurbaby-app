/* ============================================
   relatorios.js — relatórios do sistema
   3 abas: Mais vendidos, Estoque baixo, Faturamento
   ============================================ */

   let produtos = [];
   let pedidos = [];
   let abaAtual = 'mais-vendidos';
   
   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       const [prods, peds] = await Promise.all([
         adminApi.listar('produtos'),
         adminApi.listar('pedidos'),
       ]);
       produtos = prods;
       pedidos = peds;
       renderTudo();
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderTudo() {
     const conteudo = document.getElementById('conteudo');
   
     if (!document.getElementById('abas-relatorios')) {
       conteudo.innerHTML = `
         <div class="abas" id="abas-relatorios">
           <button class="aba" data-aba="mais-vendidos" onclick="trocarAba('mais-vendidos')">🏆 Mais vendidos</button>
           <button class="aba" data-aba="estoque-baixo" onclick="trocarAba('estoque-baixo')">⚠️ Estoque baixo</button>
           <button class="aba" data-aba="faturamento" onclick="trocarAba('faturamento')">💰 Faturamento</button>
         </div>
         <div class="filtros no-print">
           <select id="f-periodo" onchange="renderTudo()" title="Vale para Mais vendidos e Faturamento">
             <option value="">Todo o período</option>
             <option value="7">Últimos 7 dias</option>
             <option value="30">Últimos 30 dias</option>
             <option value="90">Últimos 90 dias</option>
             <option value="mes">Mês atual</option>
             <option value="ano">Ano atual</option>
           </select>
           <button class="btn btn-primary" id="btn-pdf" onclick="gerarPdf()">Gerar PDF</button>
         </div>
         <div id="painel-relatorios"></div>
       `;
     }
     const selPeriodo = document.getElementById('f-periodo');
     if (selPeriodo) selPeriodo.disabled = abaAtual === 'estoque-baixo';
   
     document.querySelectorAll('#abas-relatorios .aba').forEach(btn => {
       btn.classList.toggle('ativa', btn.dataset.aba === abaAtual);
     });
   
     if (abaAtual === 'mais-vendidos') renderMaisVendidos();
     else if (abaAtual === 'estoque-baixo') renderEstoqueBaixo();
     else renderFaturamento();
   }
   
   function trocarAba(nova) {
     abaAtual = nova;
     renderTudo();
   }
   
   /* ============================================
      RELATÓRIO 1 — MAIS VENDIDOS
      ============================================ */
   
   function renderMaisVendidos() {
     const painel = document.getElementById('painel-relatorios');
   
     // Consolida itens dos pedidos por produto
     const vendasPorProduto = {};
     pedidosFiltrados().forEach(p => {
       // Ignora pedidos cancelados
       if (p.status === 'CANCELADO') return;
       (p.itens || []).forEach(it => {
         const key = it.produtoId;
         if (!vendasPorProduto[key]) {
           vendasPorProduto[key] = {
             produtoId: key,
             nome: it.produtoNome,
             quantidade: 0,
             faturamento: 0,
           };
         }
         vendasPorProduto[key].quantidade += it.quantidade;
         vendasPorProduto[key].faturamento += it.valorTotal;
       });
     });
   
     const ranking = Object.values(vendasPorProduto)
       .sort((a, b) => b.quantidade - a.quantidade);
   
     if (ranking.length === 0) {
       painel.innerHTML = `
         <div class="card text-center" style="padding:2rem;">
           <p style="color:var(--texto-secundario);">Nenhuma venda registrada ainda.</p>
         </div>`;
       return;
     }
   
     // Descobre o máximo pra calcular a barra proporcional
     const maxQtd = Math.max(...ranking.map(r => r.quantidade));
     const totalQtd = ranking.reduce((s, r) => s + r.quantidade, 0);
     const totalFat = ranking.reduce((s, r) => s + r.faturamento, 0);
   
     const linhas = ranking.map((r, i) => {
       const pct = (r.quantidade / maxQtd) * 100;
       const medalha = ['🥇', '🥈', '🥉'][i] || `${i + 1}º`;
       return `
         <tr>
           <td style="font-size:1.1rem;text-align:center;width:60px;">${medalha}</td>
           <td>
             <strong>${escapeHtml(r.nome)}</strong>
             <div style="margin-top:.4rem;">
               <div style="height:6px;background:var(--superficie);border-radius:3px;overflow:hidden;">
                 <div style="height:100%;width:${pct}%;background:linear-gradient(90deg,var(--verde),var(--rosa));"></div>
               </div>
             </div>
           </td>
           <td class="text-center"><strong>${r.quantidade}</strong></td>
           <td class="text-right">R$ ${formatarPreco(r.faturamento)}</td>
           <td class="text-center">${((r.quantidade / totalQtd) * 100).toFixed(1)}%</td>
         </tr>
       `;
     }).join('');
   
     painel.innerHTML = `
       <div class="cards-kpi">
         <div class="kpi-card">
           <div class="kpi-label">Produtos vendidos</div>
           <div class="kpi-valor">${ranking.length}</div>
         </div>
         <div class="kpi-card">
           <div class="kpi-label">Unidades totais</div>
           <div class="kpi-valor">${totalQtd}</div>
         </div>
         <div class="kpi-card">
           <div class="kpi-label">Faturamento</div>
           <div class="kpi-valor">R$ ${formatarPreco(totalFat)}</div>
         </div>
       </div>
   
       <table class="tabela">
         <thead>
           <tr>
             <th class="text-center">#</th>
             <th>Produto</th>
             <th class="text-center">Qtd vendida</th>
             <th class="text-right">Faturamento</th>
             <th class="text-center">% do total</th>
           </tr>
         </thead>
         <tbody>${linhas}</tbody>
       </table>
     `;
   }
   
   /* ============================================
      RELATÓRIO 2 — ESTOQUE BAIXO
      ============================================ */
   
   function renderEstoqueBaixo() {
     const painel = document.getElementById('painel-relatorios');
   
     // Filtra produtos com estoque <= mínimo
     const criticos = produtos
       .map(p => {
         const atual = estoqueProduto(p);
         const minimo = p.estoqueMinimo || 0;
         return { p, atual, minimo };
       })
       .filter(({ atual, minimo }) => atual <= minimo)
       .sort((a, b) => (a.atual - a.minimo) - (b.atual - b.minimo));
   
     if (criticos.length === 0) {
       painel.innerHTML = `
         <div class="card text-center" style="padding:2rem;">
           <p style="font-size:2rem;">✅</p>
           <p style="color:var(--texto-secundario);">Nenhum produto com estoque crítico. Tudo em ordem!</p>
         </div>`;
       return;
     }
   
     const zerados = criticos.filter(c => c.atual === 0).length;
     const baixos = criticos.length - zerados;
   
     const linhas = criticos.map(({ p, atual, minimo }) => {
       const sugestao = Math.max(minimo * 2 - atual, 10);
       const ehZerado = atual === 0;
       const cor = ehZerado ? 'var(--erro)' : '#F59E0B';
       return `
         <tr>
           <td>
             <strong>${escapeHtml(p.nome)}</strong><br>
             <code style="font-size:.75rem;color:var(--texto-secundario);">${escapeHtml(p.codigo)}</code>
           </td>
           <td class="text-center" style="color:${cor};font-weight:700;font-size:1.05rem;">
             ${atual}
           </td>
           <td class="text-center">${minimo}</td>
           <td class="text-center">
             <span class="badge ${ehZerado ? 'badge-inativo' : 'badge-esgotado'}">
               ${ehZerado ? 'ZERADO' : 'BAIXO'}
             </span>
           </td>
           <td class="text-center"><strong>${sugestao}</strong></td>
         </tr>
       `;
     }).join('');
   
     painel.innerHTML = `
       <div class="cards-kpi">
         <div class="kpi-card" style="border-left:4px solid var(--erro);">
           <div class="kpi-label">Produtos zerados</div>
           <div class="kpi-valor" style="color:var(--erro);">${zerados}</div>
         </div>
         <div class="kpi-card" style="border-left:4px solid #F59E0B;">
           <div class="kpi-label">Estoque baixo</div>
           <div class="kpi-valor" style="color:#F59E0B;">${baixos}</div>
         </div>
         <div class="kpi-card">
           <div class="kpi-label">Total crítico</div>
           <div class="kpi-valor">${criticos.length}</div>
         </div>
       </div>
   
       <table class="tabela">
         <thead>
           <tr>
             <th>Produto</th>
             <th class="text-center">Estoque atual</th>
             <th class="text-center">Mínimo</th>
             <th class="text-center">Situação</th>
             <th class="text-center">Sugestão de compra</th>
           </tr>
         </thead>
         <tbody>${linhas}</tbody>
       </table>
     `;
   }
   
   /* ============================================
      RELATÓRIO 3 — FATURAMENTO
      ============================================ */
   
   function renderFaturamento() {
     const painel = document.getElementById('painel-relatorios');
   
     // Considera só pedidos não cancelados
     const lista = pedidosFiltrados();
     const validos = lista.filter(p => p.status !== 'CANCELADO');
     const faturamento = validos.reduce((s, p) => s + Number(p.total || 0), 0);
     const ticketMedio = validos.length > 0 ? faturamento / validos.length : 0;

     // Faturamento por mês (mais recente primeiro)
     const porMes = {};
     validos.forEach(p => {
       const d = new Date(p.criadoEm);
       const chave = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
       porMes[chave] = porMes[chave] || { pedidos: 0, total: 0 };
       porMes[chave].pedidos++;
       porMes[chave].total += Number(p.total || 0);
     });
     const linhasMes = Object.keys(porMes).sort().reverse().map(k => {
       const [ano, mes] = k.split('-');
       const nomeMes = new Date(Number(ano), Number(mes) - 1, 1).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
       return `<tr>
         <td>${nomeMes.charAt(0).toUpperCase() + nomeMes.slice(1)}</td>
         <td class="text-center">${porMes[k].pedidos}</td>
         <td class="text-right">R$ ${formatarPreco(porMes[k].total)}</td>
         <td class="text-right">R$ ${formatarPreco(porMes[k].total / porMes[k].pedidos)}</td>
       </tr>`;
     }).join('');

     // Conta pedidos por status
     const porStatus = {};
     lista.forEach(p => {
       porStatus[p.status] = (porStatus[p.status] || 0) + 1;
     });
   
     // Faturamento por status (só não cancelados)
     const fatPorStatus = {};
     validos.forEach(p => {
       fatPorStatus[p.status] = (fatPorStatus[p.status] || 0) + Number(p.total || 0);
     });
   
     const maxFat = Math.max(...Object.values(fatPorStatus), 1);
   
     const statusOrdem = [
       'RASCUNHO', 'PEDIDO_GERADO', 'EM_ANALISE', 'AGUARDANDO_CONFIRMACAO',
       'CONFIRMADO', 'SEPARANDO_PRODUTOS', 'PRONTO_PARA_RETIRADA',
       'EM_TRANSPORTE', 'ENTREGUE', 'CANCELADO',
     ];
   
     const linhasStatus = statusOrdem
       .filter(s => porStatus[s])
       .map(s => {
         const qtd = porStatus[s] || 0;
         const fat = fatPorStatus[s] || 0;
         const pct = s === 'CANCELADO' ? 0 : (fat / maxFat) * 100;
         return `
           <tr>
             <td><span class="badge ${classeStatus(s)}">${rotuloStatus(s)}</span></td>
             <td class="text-center">${qtd}</td>
             <td class="text-right">${s === 'CANCELADO' ? '—' : 'R$ ' + formatarPreco(fat)}</td>
             <td style="width:40%;">
               ${s !== 'CANCELADO' ? `
                 <div style="height:8px;background:var(--superficie);border-radius:4px;overflow:hidden;">
                   <div style="height:100%;width:${pct}%;background:var(--verde);"></div>
                 </div>
               ` : ''}
             </td>
           </tr>
         `;
       }).join('');
   
     painel.innerHTML = `
       <div class="cards-kpi">
         <div class="kpi-card" style="border-left:4px solid var(--verde);">
           <div class="kpi-label">Faturamento total</div>
           <div class="kpi-valor" style="color:var(--verde-escuro);">R$ ${formatarPreco(faturamento)}</div>
         </div>
         <div class="kpi-card">
           <div class="kpi-label">Pedidos válidos</div>
           <div class="kpi-valor">${validos.length}</div>
         </div>
         <div class="kpi-card">
           <div class="kpi-label">Ticket médio</div>
           <div class="kpi-valor">R$ ${formatarPreco(ticketMedio)}</div>
         </div>
         <div class="kpi-card" style="border-left:4px solid var(--erro);">
           <div class="kpi-label">Cancelados</div>
           <div class="kpi-valor" style="color:var(--erro);">${porStatus['CANCELADO'] || 0}</div>
         </div>
       </div>
   
       <div class="card">
         <h3 class="secao-titulo">Faturamento por status</h3>
         <table class="tabela">
           <thead>
             <tr>
               <th>Status</th>
               <th class="text-center">Pedidos</th>
               <th class="text-right">Faturamento</th>
               <th>Distribuição</th>
             </tr>
           </thead>
           <tbody>${linhasStatus}</tbody>
         </table>
       </div>

       <div class="card">
         <h3 class="secao-titulo">Faturamento por mês</h3>
         ${linhasMes ? `
         <table class="tabela">
           <thead>
             <tr>
               <th>Mês</th>
               <th class="text-center">Pedidos</th>
               <th class="text-right">Faturamento</th>
               <th class="text-right">Ticket médio</th>
             </tr>
           </thead>
           <tbody>${linhasMes}</tbody>
         </table>` : '<p class="texto-sec">Nenhum pedido válido no período.</p>'}
       </div>
     `;
   }

   /* ============================================
      PERÍODO
      ============================================ */
   function pedidosFiltrados() {
     const v = document.getElementById('f-periodo')?.value || '';
     if (!v) return pedidos;
     const hoje = new Date();
     let inicio;
     if (v === 'mes') inicio = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
     else if (v === 'ano') inicio = new Date(hoje.getFullYear(), 0, 1);
     else { inicio = new Date(); inicio.setHours(0, 0, 0, 0); inicio.setDate(inicio.getDate() - Number(v)); }
     return pedidos.filter(p => new Date(p.criadoEm) >= inicio);
   }

   function descricaoPeriodo() {
     if (abaAtual === 'estoque-baixo') return 'Posição atual do estoque';
     const sel = document.getElementById('f-periodo');
     return 'Período: ' + (sel ? sel.options[sel.selectedIndex].text : 'Todo o período');
   }

   /* ============================================
      EXPORTAR PDF
      Usa html2pdf.js (carregado sob demanda do CDN). Sem internet, cai para a
      impressão do navegador, onde é possível escolher "Salvar como PDF".
      ============================================ */
   const TITULOS_RELATORIO = {
     'mais-vendidos': 'Produtos mais vendidos',
     'estoque-baixo': 'Produtos com estoque baixo',
     'faturamento': 'Faturamento',
   };

   function carregarHtml2Pdf() {
     if (window.html2pdf) return Promise.resolve(window.html2pdf);
     return new Promise((resolve, reject) => {
       const s = document.createElement('script');
       s.src = 'https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js';
       s.onload = () => (window.html2pdf ? resolve(window.html2pdf) : reject(new Error('html2pdf indisponível')));
       s.onerror = () => reject(new Error('Falha ao carregar o gerador de PDF'));
       document.head.appendChild(s);
     });
   }

   function montarDocumentoPdf() {
     const titulo = TITULOS_RELATORIO[abaAtual];
     const usuario = getUsuario();
     const doc = document.createElement('div');
     doc.className = 'relatorio-pdf';
     doc.innerHTML = `
       <div class="relatorio-pdf-cabecalho">
         <img src="assets/logo-arthurbaby.png" alt="Lojão Arthur Baby">
         <div>
           <h1>${escapeHtml(titulo)}</h1>
           <p>${escapeHtml(descricaoPeriodo())}</p>
           <p>Gerado em ${new Date().toLocaleString('pt-BR')} por ${escapeHtml(usuario?.nome || '—')}</p>
         </div>
       </div>
       ${document.getElementById('painel-relatorios').innerHTML}
       <div class="relatorio-pdf-rodape">Lojão Arthur Baby — Tudo para seu bebê! · Relatório gerado pelo Painel Administrativo</div>
     `;
     return doc;
   }

   async function gerarPdf() {
     const btn = document.getElementById('btn-pdf');
     btn.disabled = true;
     btn.textContent = 'Gerando PDF...';
     const doc = montarDocumentoPdf();
     try {
       const html2pdf = await carregarHtml2Pdf();
       const nome = `relatorio-${abaAtual}-${new Date().toISOString().slice(0, 10)}.pdf`;
       await html2pdf().set({
         margin: [10, 10, 12, 10],
         filename: nome,
         image: { type: 'jpeg', quality: 0.95 },
         html2canvas: { scale: 2, useCORS: true, backgroundColor: '#ffffff' },
         jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
         pagebreak: { mode: ['css', 'legacy'], avoid: ['tr', '.kpi-card'] },
       }).from(doc).save();
     } catch (err) {
       console.warn(err);
       // Alternativa offline: impressão do navegador (opção "Salvar como PDF")
       const area = document.createElement('div');
       area.id = 'area-impressao';
       area.appendChild(doc);
       document.body.appendChild(area);
       document.body.classList.add('imprimindo-relatorio');
       alert('Não foi possível gerar o PDF direto. Na janela de impressão, escolha "Salvar como PDF".');
       window.print();
       document.body.classList.remove('imprimindo-relatorio');
       area.remove();
     } finally {
       btn.disabled = false;
       btn.textContent = 'Gerar PDF';
     }
   }
   
   /* ============================================
      HELPERS
      ============================================ */
   function formatarPreco(v) {
     return Number(v || 0).toFixed(2).replace('.', ',');
   }
   function badgeStatus(status) {
     if (status === 'CANCELADO') return 'badge-inativo';
     if (status === 'ENTREGUE') return 'badge-ativo';
     if (status === 'PEDIDO_GERADO' || status === 'EM_ANALISE' || status === 'AGUARDANDO_CONFIRMACAO') return 'badge-esgotado';
     return 'badge-ativo';
   }
   function escapeHtml(s) {
     return String(s ?? '').replace(/[&<>"']/g, m => ({
       '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
     }[m]));
   }
   function escapeAttr(s) { return escapeHtml(s); }
   
   /* ============================================
      INIT
      ============================================ */
   document.addEventListener('DOMContentLoaded', () => {
     if (montarLayout('Relatórios', 'relatorios')) carregar();
   });