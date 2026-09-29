/* ============================================
   pedidos.js — listagem de pedidos
   ============================================ */

   const TIPO = 'pedidos';
   let cache = [];
   
   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       cache = await adminApi.listar(TIPO);
       renderTudo();
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderTudo() {
     const conteudo = document.getElementById('conteudo');
   
     if (!document.getElementById('tabela-pedidos')) {
       conteudo.innerHTML = `
         <div class="filtros">
           <input type="text" id="f-busca" placeholder="Buscar por número ou cliente..." oninput="aplicarFiltros()">
           <select id="f-status" onchange="aplicarFiltros()">
             <option value="">Todos os status</option>
             <option value="RASCUNHO">RASCUNHO</option>
             <option value="PEDIDO_GERADO">PEDIDO_GERADO</option>
             <option value="EM_ANALISE">EM_ANALISE</option>
             <option value="AGUARDANDO_CONFIRMACAO">AGUARDANDO_CONFIRMACAO</option>
             <option value="CONFIRMADO">CONFIRMADO</option>
             <option value="SEPARANDO_PRODUTOS">SEPARANDO_PRODUTOS</option>
             <option value="PRONTO_PARA_RETIRADA">PRONTO_PARA_RETIRADA</option>
             <option value="EM_TRANSPORTE">EM_TRANSPORTE</option>
             <option value="ENTREGUE">ENTREGUE</option>
             <option value="CANCELADO">CANCELADO</option>
           </select>
           <select id="f-forma" onchange="aplicarFiltros()">
             <option value="">Todas as formas</option>
             <option value="RETIRADA_LOJA">Retirada na loja</option>
             <option value="ENTREGA">Entrega</option>
           </select>
         </div>
         <table class="tabela">
           <thead>
             <tr>
               <th>Número</th>
               <th>Cliente</th>
               <th>Data</th>
               <th>Forma</th>
               <th>Total</th>
               <th>Status</th>
               <th>Ações</th>
             </tr>
           </thead>
           <tbody id="tabela-pedidos"></tbody>
         </table>
       `;
     }
   
     aplicarFiltros();
   }
   
   function aplicarFiltros() {
     const busca = (document.getElementById('f-busca')?.value || '').toLowerCase().trim();
     const status = document.getElementById('f-status')?.value || '';
     const forma = document.getElementById('f-forma')?.value || '';
   
     const filtrados = cache.filter(p => {
       if (busca) {
         const alvo = `${p.numeroPedido} ${p.clienteNome}`.toLowerCase();
         if (!alvo.includes(busca)) return false;
       }
       if (status && p.status !== status) return false;
       if (forma && p.formaRecebimento !== forma) return false;
       return true;
     });
   
     // Ordena por data decrescente (mais recente primeiro)
     filtrados.sort((a, b) => new Date(b.criadoEm) - new Date(a.criadoEm));
   
     renderTabela(filtrados);
   }
   
   function renderTabela(lista) {
     const tbody = document.getElementById('tabela-pedidos');
   
     tbody.innerHTML = lista.length === 0
       ? `<tr><td colspan="7" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
            Nenhum pedido encontrado.
          </td></tr>`
       : lista.map(p => `
         <tr>
           <td><strong>${escapeHtml(p.numeroPedido)}</strong></td>
           <td>${escapeHtml(p.clienteNome)}</td>
           <td>${formatarData(p.criadoEm)}</td>
           <td>${p.formaRecebimento === 'ENTREGA' ? '🚚 Entrega' : '🏬 Retirada'}</td>
           <td><strong>R$ ${formatarPreco(p.total)}</strong></td>
           <td><span class="badge ${badgeStatus(p.status)}">${p.status}</span></td>
           <td>
             <div class="acoes-linha">
               <button class="btn-mini editar" onclick="abrirDetalhe(${p.id})">Detalhar</button>
             </div>
           </td>
         </tr>`).join('');
   }
   
   function abrirDetalhe(id) {
     window.location.href = `pedido-detalhe.html?id=${id}`;
   }
   
   /* ----- Helpers ----- */
   function formatarPreco(v) {
     return Number(v || 0).toFixed(2).replace('.', ',');
   }
   function formatarData(iso) {
     if (!iso) return '—';
     const d = new Date(iso);
     return d.toLocaleString('pt-BR', {
       day: '2-digit', month: '2-digit', year: 'numeric',
       hour: '2-digit', minute: '2-digit',
     });
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
   
   /* ----- Init ----- */
   document.addEventListener('DOMContentLoaded', () => {
     if (montarLayout('Pedidos', 'pedidos')) carregar();
   });