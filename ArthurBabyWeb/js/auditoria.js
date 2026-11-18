/* ============================================
   auditoria.js — log de auditoria
   ============================================ */

   let logs = [];

   // Variáveis para os Selects Customizados
   let valorAcao = '';
   let valorEntidade = '';
   
   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       logs = await adminApi.listar('auditoria');
       renderTudo();
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderTudo() {
     const conteudo = document.getElementById('conteudo');
   
     if (!document.getElementById('tabela-auditoria')) {
       conteudo.innerHTML = `
         <div class="cards-kpi" id="kpis-auditoria"></div>
         
         <div class="filtros">
           <input type="text" id="f-busca" placeholder="Buscar por usuário ou descrição..." oninput="aplicarFiltros()">
           
           <!-- SELECT AÇÃO -->
           <div class="custom-select" id="custom-acao" style="position:relative; min-width:160px;">
             <div class="custom-select-trigger" onclick="toggleSelect('acao')">
               <span id="acao-label" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">Todas as ações</span>
               <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
             </div>
             <div class="custom-select-options hidden" id="acao-options">
               <div class="custom-option" onclick="selecionarFiltro('acao', '', 'Todas as ações')">Todas as ações</div>
               <div class="custom-option" onclick="selecionarFiltro('acao', 'CRIAR', 'Criar')">Criar</div>
               <div class="custom-option" onclick="selecionarFiltro('acao', 'ATUALIZAR', 'Atualizar')">Atualizar</div>
               <div class="custom-option" onclick="selecionarFiltro('acao', 'DELETAR', 'Deletar')">Deletar</div>
               <div class="custom-option" onclick="selecionarFiltro('acao', 'RESETAR_SENHA', 'Resetar senha')">Resetar senha</div>
               <div class="custom-option" onclick="selecionarFiltro('acao', 'BLOQUEAR', 'Bloquear')">Bloquear</div>
               <div class="custom-option" onclick="selecionarFiltro('acao', 'DESBLOQUEAR', 'Desbloquear')">Desbloquear</div>
               <div class="custom-option" onclick="selecionarFiltro('acao', 'CANCELAR', 'Cancelar')">Cancelar</div>
               <div class="custom-option" onclick="selecionarFiltro('acao', 'ALTERAR_STATUS', 'Alterar status')">Alterar status</div>
               <div class="custom-option" onclick="selecionarFiltro('acao', 'MOVIMENTAR_ESTOQUE', 'Movimentar estoque')">Movimentar estoque</div>
             </div>
           </div>
   
           <!-- SELECT ENTIDADE -->
           <div class="custom-select" id="custom-entidade" style="position:relative; min-width:160px;">
             <div class="custom-select-trigger" onclick="toggleSelect('entidade')">
               <span id="entidade-label" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">Todas as entidades</span>
               <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
             </div>
             <div class="custom-select-options hidden" id="entidade-options">
               <div class="custom-option" onclick="selecionarFiltro('entidade', '', 'Todas as entidades')">Todas as entidades</div>
               <div class="custom-option" onclick="selecionarFiltro('entidade', 'Produto', 'Produto')">Produto</div>
               <div class="custom-option" onclick="selecionarFiltro('entidade', 'Categoria', 'Categoria')">Categoria</div>
               <div class="custom-option" onclick="selecionarFiltro('entidade', 'Marca', 'Marca')">Marca</div>
               <div class="custom-option" onclick="selecionarFiltro('entidade', 'Pedido', 'Pedido')">Pedido</div>
               <div class="custom-option" onclick="selecionarFiltro('entidade', 'Usuario', 'Usuário')">Usuário</div>
               <div class="custom-option" onclick="selecionarFiltro('entidade', 'MovimentacaoEstoque', 'Movimentação de estoque')">Movimentação de estoque</div>
               <div class="custom-option" onclick="selecionarFiltro('entidade', 'ConfiguracaoLoja', 'Configuração da loja')">Configuração da loja</div>
             </div>
           </div>
         </div>
         
         <div class="tabela-rolavel">
           <table class="tabela">
             <thead>
               <tr>
                 <th>Data/hora</th>
                 <th>Usuário</th>
                 <th>Ação</th>
                 <th>Entidade</th>
                 <th>Descrição</th>
               </tr>
             </thead>
             <tbody id="tabela-auditoria"></tbody>
           </table>
         </div>
       `;
     }
   
     renderKpis();
     aplicarFiltros();
   }
   
   function renderKpis() {
     const div = document.getElementById('kpis-auditoria');
     if (!div) return;
   
     const total = logs.length;
     const hoje = logs.filter(l => {
       const d = new Date(l.data);
       const agora = new Date();
       return d.toDateString() === agora.toDateString();
     }).length;
     const usuarios = new Set(logs.map(l => l.usuario)).size;
     const entidades = new Set(logs.map(l => l.entidade)).size;
   
     div.innerHTML = `
       <div class="kpi-card">
         <div class="kpi-label">Total de registros</div>
         <div class="kpi-valor">${total}</div>
       </div>
       <div class="kpi-card">
         <div class="kpi-label">Hoje</div>
         <div class="kpi-valor">${hoje}</div>
       </div>
       <div class="kpi-card">
         <div class="kpi-label">Usuários distintos</div>
         <div class="kpi-valor">${usuarios}</div>
       </div>
       <div class="kpi-card">
         <div class="kpi-label">Tipos de entidade</div>
         <div class="kpi-valor">${entidades}</div>
       </div>
     `;
   }
   
   /* ============================================
      CONTROLE DOS SELECTS CUSTOMIZADOS
      ============================================ */
   function toggleSelect(id) {
     document.getElementById(`${id}-options`).classList.toggle('hidden');
   }
   
   // Fecha os selects se clicar fora
   document.addEventListener('click', (e) => {
     if (!e.target.closest('#custom-acao')) document.getElementById('acao-options')?.classList.add('hidden');
     if (!e.target.closest('#custom-entidade')) document.getElementById('entidade-options')?.classList.add('hidden');
   });
   
   function selecionarFiltro(tipo, valor, label) {
     if (tipo === 'acao') {
       valorAcao = valor;
       document.getElementById('acao-label').textContent = label;
       document.getElementById('acao-options').classList.add('hidden');
     } else if (tipo === 'entidade') {
       valorEntidade = valor;
       document.getElementById('entidade-label').textContent = label;
       document.getElementById('entidade-options').classList.add('hidden');
     }
     aplicarFiltros();
   }
   
   function aplicarFiltros() {
     const input = document.getElementById('f-busca');
     if (!input) return;
   
     const busca = input.value.toLowerCase().trim();
     const acao = valorAcao;
     const entidade = valorEntidade;
   
     const filtrados = logs
       .filter(l => {
         if (busca) {
           const alvo = `${l.usuario} ${l.descricao}`.toLowerCase();
           if (!alvo.includes(busca)) return false;
         }
         if (acao && l.acao !== acao) return false;
         if (entidade && l.entidade !== entidade) return false;
         return true;
       })
       .sort((a, b) => new Date(b.data) - new Date(a.data));
   
     renderTabela(filtrados);
   }
   
   function renderTabela(lista) {
     const tbody = document.getElementById('tabela-auditoria');
   
     tbody.innerHTML = lista.length === 0
       ? `<tr><td colspan="5" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
            Nenhum registro encontrado.
          </td></tr>`
       : lista.map(l => `
         <tr>
           <td style="white-space:nowrap;font-size:.85rem;">${formatarData(l.data)}</td>
           <td><strong>${escapeHtml(l.usuario)}</strong></td>
           <td><span class="badge ${badgeAcao(l.acao)}">${formatarAcao(l.acao)}</span></td>
           <td>
             <span style="font-size:.85rem;">${escapeHtml(l.entidade)}</span>
             ${l.entidadeId ? `<span style="color:var(--texto-secundario);font-size:.75rem;">#${l.entidadeId}</span>` : ''}
           </td>
           <td style="font-size:.9rem;">${escapeHtml(l.descricao)}</td>
         </tr>
       `).join('');
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
   
   function formatarAcao(acao) {
     const mapa = {
       'CRIAR': 'Criar',
       'ATUALIZAR': 'Atualizar',
       'DELETAR': 'Deletar',
       'RESETAR_SENHA': 'Resetar senha',
       'BLOQUEAR': 'Bloquear',
       'DESBLOQUEAR': 'Desbloquear',
       'CANCELAR': 'Cancelar',
       'ALTERAR_STATUS': 'Alterar status',
       'MOVIMENTAR_ESTOQUE': 'Movimentar estoque',
     };
     return mapa[acao] || acao;
   }
   
   function badgeAcao(acao) {
     if (acao === 'CRIAR') return 'badge-ativo';
     if (acao === 'DELETAR' || acao === 'CANCELAR') return 'badge-inativo';
     if (acao === 'RESETAR_SENHA' || acao === 'BLOQUEAR') return 'badge-esgotado';
     return 'badge-ativo';
   }
   
   function escapeHtml(s) {
     return String(s ?? '').replace(/[&<>"']/g, m => ({
       '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
     }[m]));
   }
   
   /* ============================================
      INIT
      ============================================ */
   document.addEventListener('DOMContentLoaded', () => {
     if (montarLayout('Auditoria', 'auditoria')) carregar();
   });