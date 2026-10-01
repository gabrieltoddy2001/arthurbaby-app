/* ============================================
   relatorios.js — relatórios do sistema
   3 abas: Mais vendidos, Estoque baixo, Faturamento
   ============================================ */

   let produtos = [];
   let pedidos = [];
   let movimentacoes = [];
   let abaAtual = 'mais-vendidos';
   
   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       const [prods, peds, movs] = await Promise.all([
         adminApi.listar('produtos'),
         adminApi.listar('pedidos'),
         adminApi.listar('movimentacoes'),
       ]);
       produtos = prods;
       pedidos = peds;
       movimentacoes = movs;
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
         <div id="painel-relatorios"></div>
       `;
     }
   
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
     pedidos.forEach(p => {
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
   
     // Calcula estoque atual por produto
     const estoqueAtual = {};
     movimentacoes.forEach(m => {
       estoqueAtual[m.produtoId] = m.estoquePosterior ?? 0;
     });
   
     // Filtra produtos com estoque <= mínimo
     const criticos = produtos
       .map(p => {
         const atual = estoqueAtual[p.id] ?? 0;
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
     const validos = pedidos.filter(p => p.status !== 'CANCELADO');
     const faturamento = validos.reduce((s, p) => s + p.total, 0);
     const ticketMedio = validos.length > 0 ? faturamento / validos.length : 0;
   
     // Conta pedidos por status
     const porStatus = {};
     pedidos.forEach(p => {
       porStatus[p.status] = (porStatus[p.status] || 0) + 1;
     });
   
     // Faturamento por status (só não cancelados)
     const fatPorStatus = {};
     validos.forEach(p => {
       fatPorStatus[p.status] = (fatPorStatus[p.status] || 0) + p.total;
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
             <td><span class="badge ${badgeStatus(s)}">${s}</span></td>
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
     `;
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