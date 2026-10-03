/* ============================================
   estoque.js — controle de estoque
   Duas abas: Visão geral (por produto) e Movimentações
   ============================================ */

   let produtos = [];
   let movimentacoes = [];
   let abaAtual = 'visao';
   
   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       const [prods, movs] = await Promise.all([
         adminApi.listar('produtos'),
         adminApi.listar('movimentacoes'),
       ]);
       produtos = prods;
       movimentacoes = movs;
       renderTudo();
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderTudo() {
     const conteudo = document.getElementById('conteudo');
   
     if (!document.getElementById('abas-estoque')) {
       conteudo.innerHTML = `
         <div class="abas" id="abas-estoque">
           <button class="aba" data-aba="visao" onclick="trocarAba('visao')">Visão geral</button>
           <button class="aba" data-aba="movimentacoes" onclick="trocarAba('movimentacoes')">Movimentações</button>
         </div>
         <div id="painel-estoque"></div>
       `;
     }
   
     document.querySelectorAll('#abas-estoque .aba').forEach(btn => {
       btn.classList.toggle('ativa', btn.dataset.aba === abaAtual);
     });
   
     if (abaAtual === 'visao') renderVisao();
     else renderMovimentacoes();
   }
   
   function trocarAba(nova) {
     abaAtual = nova;
     renderTudo();
   }
   
   /* ============================================
      VISÃO GERAL (por produto)
      ============================================ */
   
   function renderVisao() {
     const painel = document.getElementById('painel-estoque');
   
     if (!document.getElementById('tabela-visao-estoque')) {
       painel.innerHTML = `
         <div class="filtros">
           <input type="text" id="f-busca-visao" placeholder="Buscar produto..." oninput="aplicarFiltroVisao()">
           <select id="f-situacao" onchange="aplicarFiltroVisao()">
             <option value="">Todas as situações</option>
             <option value="OK">Estoque OK</option>
             <option value="BAIXO">Estoque baixo</option>
             <option value="ZERADO">Zerado</option>
           </select>
           <button class="btn btn-primary" onclick="abrirModalMovimentacao()">+ Nova movimentação</button>
         </div>
         <table class="tabela">
           <thead>
             <tr>
               <th>Produto</th>
               <th>Código</th>
               <th class="text-center">Estoque atual</th>
               <th class="text-center">Estoque mínimo</th>
               <th class="text-center">Situação</th>
             </tr>
           </thead>
           <tbody id="tabela-visao-estoque"></tbody>
         </table>
       `;
     }
   
     aplicarFiltroVisao();
   }
   
   function aplicarFiltroVisao() {
     const input = document.getElementById('f-busca-visao');
     const sel = document.getElementById('f-situacao');
     if (!input || !sel) return;
   
     const busca = input.value.toLowerCase().trim();
     const situacao = sel.value;
   
     const linhas = produtos
       .filter(p => !busca || p.nome.toLowerCase().includes(busca))
       .map(p => {
         const atual = estoqueProduto(p);
         const minimo = p.estoqueMinimo || 0;
         let situacaoProduto = 'OK';
         if (atual === 0) situacaoProduto = 'ZERADO';
         else if (atual <= minimo) situacaoProduto = 'BAIXO';
   
         return { p, atual, minimo, situacaoProduto };
       })
       .filter(({ situacaoProduto }) => !situacao || situacaoProduto === situacao);
   
     const tbody = document.getElementById('tabela-visao-estoque');
     tbody.innerHTML = linhas.length === 0
       ? `<tr><td colspan="5" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
            Nenhum produto encontrado.
          </td></tr>`
       : linhas.map(({ p, atual, minimo, situacaoProduto }) => {
           const badge = {
             'OK': 'badge-ativo',
             'BAIXO': 'badge-esgotado',
             'ZERADO': 'badge-inativo',
           }[situacaoProduto];
           const label = {
             'OK': 'OK',
             'BAIXO': 'Baixo',
             'ZERADO': 'Zerado',
           }[situacaoProduto];
           return `
             <tr>
               <td><strong>${escapeHtml(p.nome)}</strong></td>
               <td><code style="font-size:.8rem;color:var(--texto-secundario);">${escapeHtml(p.codigo)}</code></td>
               <td class="text-center"><strong>${atual}</strong></td>
               <td class="text-center">${minimo}</td>
               <td class="text-center"><span class="badge ${badge}">${label}</span></td>
             </tr>`;
         }).join('');
   }
   
   /* ============================================
      MOVIMENTAÇÕES
      ============================================ */
   
   function renderMovimentacoes() {
     const painel = document.getElementById('painel-estoque');
   
     if (!document.getElementById('tabela-movimentacoes')) {
       painel.innerHTML = `
         <div class="filtros">
           <input type="text" id="f-busca-mov" placeholder="Buscar produto..." oninput="aplicarFiltroMov()">
           <select id="f-tipo" onchange="aplicarFiltroMov()">
             <option value="">Todos os tipos</option>
             <option value="ENTRADA">ENTRADA</option>
             <option value="SAIDA">SAIDA</option>
             <option value="AJUSTE">AJUSTE</option>
             <option value="RESERVA">RESERVA</option>
             <option value="ESTORNO">ESTORNO</option>
           </select>
           <button class="btn btn-primary" onclick="abrirModalMovimentacao()">+ Nova movimentação</button>
         </div>
         <table class="tabela">
           <thead>
             <tr>
               <th>Data</th>
               <th>Produto</th>
               <th>Tipo</th>
               <th class="text-center">Qtd</th>
               <th class="text-center">Antes</th>
               <th class="text-center">Depois</th>
               <th>Usuário</th>
               <th>Observação</th>
             </tr>
           </thead>
           <tbody id="tabela-movimentacoes"></tbody>
         </table>
       `;
     }
   
     aplicarFiltroMov();
   }
   
   function aplicarFiltroMov() {
     const input = document.getElementById('f-busca-mov');
     const sel = document.getElementById('f-tipo');
     if (!input || !sel) return;
   
     const busca = input.value.toLowerCase().trim();
     const tipo = sel.value;
   
     const filtradas = movimentacoes
       .filter(m => {
         if (busca && !`${m.produtoNome || ''} ${m.sku || ''}`.toLowerCase().includes(busca)) return false;
         if (tipo && m.tipo !== tipo) return false;
         return true;
       })
       .sort((a, b) => new Date(b.data) - new Date(a.data));
   
     const tbody = document.getElementById('tabela-movimentacoes');
     tbody.innerHTML = filtradas.length === 0
       ? `<tr><td colspan="8" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
            Nenhuma movimentação encontrada.
          </td></tr>`
       : filtradas.map(m => {
           const badge = {
             'ENTRADA': 'badge-ativo',
             'SAIDA': 'badge-inativo',
             'AJUSTE': 'badge-esgotado',
             'RESERVA': 'badge-esgotado',
             'ESTORNO': 'badge-ativo',
           }[m.tipo] || 'badge-inativo';
           return `
             <tr>
               <td>${formatarData(m.data)}</td>
               <td>${escapeHtml(m.produtoNome)}${m.sku ? `<br><code style="font-size:.75rem;color:var(--texto-secundario);">${escapeHtml(m.sku)}</code>` : ''}</td>
               <td><span class="badge ${badge}">${m.tipo}</span></td>
               <td class="text-center">${sinalMovimentacao(m)}${m.quantidade}</td>
               <td class="text-center">${m.estoqueAnterior}</td>
               <td class="text-center"><strong>${m.estoquePosterior}</strong></td>
               <td>${escapeHtml(m.usuario || '—')}</td>
               <td style="font-size:.85rem;color:var(--texto-secundario);">${escapeHtml(m.observacao || '—')}</td>
             </tr>`;
         }).join('');
   }
   
   // O backend grava a quantidade sempre positiva; o sinal vem do tipo (ou da diferença, no ajuste)
   function sinalMovimentacao(m) {
     if (m.tipo === 'SAIDA' || m.tipo === 'RESERVA') return '-';
     if (m.tipo === 'AJUSTE') return (m.estoquePosterior ?? 0) < (m.estoqueAnterior ?? 0) ? '-' : '+';
     return '+';
   }
   
   /* ============================================
      MODAL — NOVA MOVIMENTAÇÃO
      ============================================ */
   
   function abrirModalMovimentacao() {
     const overlay = document.createElement('div');
     overlay.className = 'modal-overlay';
     overlay.innerHTML = `
       <div class="modal">
         <h2>Nova movimentação de estoque</h2>
         <div id="modal-alerta" class="alert alert-erro hidden"></div>
         <form id="form-mov">
           <div class="field">
             <label>Produto *</label>
             <select id="m-produto" onchange="preencherVariacoes()">
               <option value="">Selecione...</option>
               ${produtos.map(p => `<option value="${p.id}">${escapeHtml(p.nome)}</option>`).join('')}
             </select>
           </div>
           <div class="field">
             <label>Variação *</label>
             <select id="m-variacao">
               <option value="">Selecione o produto primeiro</option>
             </select>
           </div>
           <div class="field">
             <label>Tipo *</label>
             <select id="m-tipo">
               <option value="ENTRADA">ENTRADA (adicionar estoque)</option>
               <option value="SAIDA">SAIDA (remover estoque)</option>
               <option value="AJUSTE">AJUSTE (definir novo saldo)</option>
               <option value="ESTORNO">ESTORNO (devolver saldo)</option>
             </select>
           </div>
           <div class="field">
             <label>Quantidade *</label>
             <input type="number" id="m-qtd" min="0" value="1">
             <small style="color:var(--texto-secundario);font-size:.8rem;">
               Para AJUSTE, informe o novo saldo total da variação (ex.: contagem do inventário).
             </small>
           </div>
           <div class="field">
             <label>Observação</label>
             <textarea id="m-obs" rows="2" placeholder="Ex: Entrada do fornecedor XYZ"></textarea>
           </div>
           <div class="modal-acoes">
             <button type="button" class="btn btn-ghost" onclick="fecharModal()">Cancelar</button>
             <button type="submit" class="btn btn-primary" id="m-salvar">Registrar</button>
           </div>
         </form>
       </div>
     `;
     document.body.appendChild(overlay);
   
     document.getElementById('form-mov').addEventListener('submit', async (e) => {
       e.preventDefault();
       await registrarMovimentacao();
     });
   }
   
   function preencherVariacoes() {
     const produtoId = Number(document.getElementById('m-produto').value);
     const sel = document.getElementById('m-variacao');
     const produto = produtos.find(p => p.id === produtoId);
     const variacoes = produto?.variacoes || [];
   
     if (!produto) {
       sel.innerHTML = '<option value="">Selecione o produto primeiro</option>';
     } else if (variacoes.length === 0) {
       sel.innerHTML = '<option value="">Produto sem variações cadastradas</option>';
     } else {
       sel.innerHTML = variacoes.map(v => {
         const desc = [v.tamanho?.nome, v.cor?.nome, v.modelo?.nome].filter(Boolean).join(' / ');
         return `<option value="${v.id}">${escapeHtml(v.sku)}${desc ? ' — ' + escapeHtml(desc) : ''} (estoque: ${v.estoqueAtual ?? 0})</option>`;
       }).join('');
     }
   }
   
   async function registrarMovimentacao() {
     const alerta = document.getElementById('modal-alerta');
     const btn = document.getElementById('m-salvar');
   
     const variacaoId = Number(document.getElementById('m-variacao').value);
     const tipo = document.getElementById('m-tipo').value;
     const qtdRaw = document.getElementById('m-qtd').value;
     const quantidade = Number(qtdRaw);
     const obs = document.getElementById('m-obs').value.trim();
   
     alerta.classList.add('hidden');
   
     if (!Number(document.getElementById('m-produto').value)) return mostrarErroModal('Selecione um produto.');
     if (!variacaoId) return mostrarErroModal('Selecione uma variação.');
     if (qtdRaw === '' || !Number.isInteger(quantidade) || quantidade < 0) return mostrarErroModal('Informe uma quantidade válida.');
     if (tipo !== 'AJUSTE' && quantidade === 0) return mostrarErroModal('A quantidade deve ser maior que zero.');
   
     btn.disabled = true;
     btn.textContent = 'Registrando...';
   
     try {
       await adminApi.movimentarEstoque(variacaoId, tipo.toLowerCase(), {
         quantidade,
         observacao: obs || null,
       });
       fecharModal();
       await carregar();
     } catch (err) {
       mostrarErroModal(err.message);
     }
   }
   
   function mostrarErroModal(msg) {
     const alerta = document.getElementById('modal-alerta');
     const btn = document.getElementById('m-salvar');
     alerta.textContent = msg;
     alerta.classList.remove('hidden');
     btn.disabled = false;
     btn.textContent = 'Registrar';
   }
   
   function fecharModal() {
     document.querySelector('.modal-overlay')?.remove();
   }
   
   /* ============================================
      HELPERS
      ============================================ */
   
   function formatarData(iso) {
     if (!iso) return '—';
     const d = new Date(iso);
     return d.toLocaleString('pt-BR', {
       day: '2-digit', month: '2-digit', year: 'numeric',
       hour: '2-digit', minute: '2-digit',
     });
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
     if (montarLayout('Estoque', 'estoque')) carregar();
   });