/* ============================================
   auditoria.js — log de auditoria
   ============================================ */

   let logs = [];

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
           <select id="f-acao" onchange="aplicarFiltros()">
             <option value="">Todas as ações</option>
             <option value="CRIAR">Criar</option>
             <option value="ATUALIZAR">Atualizar</option>
             <option value="DELETAR">Deletar</option>
             <option value="RESETAR_SENHA">Resetar senha</option>
             <option value="BLOQUEAR">Bloquear</option>
             <option value="DESBLOQUEAR">Desbloquear</option>
             <option value="CANCELAR">Cancelar</option>
           </select>
           <select id="f-entidade" onchange="aplicarFiltros()">
             <option value="">Todas as entidades</option>
             <option value="Produto">Produto</option>
             <option value="Categoria">Categoria</option>
             <option value="Marca">Marca</option>
             <option value="Pedido">Pedido</option>
             <option value="Usuario">Usuário</option>
             <option value="MovimentacaoEstoque">Movimentação de estoque</option>
             <option value="ConfiguracaoLoja">Configuração da loja</option>
           </select>
         </div>
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
   
   function aplicarFiltros() {
     const input = document.getElementById('f-busca');
     const selAcao = document.getElementById('f-acao');
     const selEnt = document.getElementById('f-entidade');
     if (!input || !selAcao || !selEnt) return;
   
     const busca = input.value.toLowerCase().trim();
     const acao = selAcao.value;
     const entidade = selEnt.value;
   
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