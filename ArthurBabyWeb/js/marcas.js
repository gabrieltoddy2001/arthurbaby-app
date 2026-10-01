/* ============================================
   marcas.js — CRUD de marcas
   ============================================ */

   const TIPO = 'marcas';
   let cache = [];
   
   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       cache = await adminApi.listar(TIPO);
       renderTabela(cache);
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderTabela(lista) {
    const conteudo = document.getElementById('conteudo');
  
    // Cria a estrutura só uma vez
    if (!document.getElementById('tabela-marcas')) {
      conteudo.innerHTML = `
        <div class="acoes-topo">
          <input type="text" id="filtro" placeholder="Filtrar por nome..." oninput="filtrar()">
          <button class="btn btn-primary" onclick="criar()">+ Nova marca</button>
        </div>
        <table class="tabela">
          <thead>
            <tr><th>ID</th><th>Nome</th><th>Status</th><th>Ações</th></tr>
          </thead>
          <tbody id="tabela-marcas"></tbody>
        </table>
      `;
    }
  
    // Atualiza só o corpo
    const tbody = document.getElementById('tabela-marcas');
  
    tbody.innerHTML = lista.length === 0
      ? `<tr><td colspan="4" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
           Nenhuma marca encontrada.
         </td></tr>`
      : lista.map(m => `
        <tr>
          <td>${m.id}</td>
          <td><strong>${escapeHtml(m.nome)}</strong></td>
          <td>
            <span class="badge ${m.status === 'ATIVO' ? 'badge-ativo' : 'badge-inativo'}">
              ${m.status}
            </span>
          </td>
          <td>
            <div class="acoes-linha">
              <button class="btn-mini editar" onclick="editar(${m.id})">Editar</button>
              <button class="btn-mini ${m.status === 'ATIVO' ? 'inativar' : 'ativar'}"
                      onclick="alternar(${m.id})">
                ${m.status === 'ATIVO' ? 'Inativar' : 'Ativar'}
              </button>
            </div>
          </td>
        </tr>`).join('');
  }
   function filtrar() {
     const termo = document.getElementById('filtro').value.toLowerCase();
     renderTabela(cache.filter(m => m.nome.toLowerCase().includes(termo)));
   }
   
   function criar() { abrirModal(null); }
   
   function editar(id) {
     const marca = cache.find(m => m.id === id);
     if (marca) abrirModal(marca);
   }
   
   function abrirModal(marca) {
     const editando = !!marca;
     const overlay = document.createElement('div');
     overlay.className = 'modal-overlay';
     overlay.innerHTML = `
       <div class="modal">
         <h2>${editando ? 'Editar marca' : 'Nova marca'}</h2>
         <div id="modal-alerta" class="alert alert-erro hidden"></div>
         <form id="form-marca">
           <div class="field">
             <label>Nome *</label>
             <input type="text" id="f-nome" required value="${escapeAttr(marca?.nome || '')}">
           </div>
           <div class="field">
             <label>Status</label>
             <select id="f-status">
               <option value="ATIVO" ${marca?.status === 'ATIVO' || !editando ? 'selected' : ''}>ATIVO</option>
               <option value="INATIVO" ${marca?.status === 'INATIVO' ? 'selected' : ''}>INATIVO</option>
             </select>
           </div>
           <div class="modal-acoes">
             <button type="button" class="btn btn-ghost" onclick="fecharModal()">Cancelar</button>
             <button type="submit" class="btn btn-primary" id="btn-salvar">
               ${editando ? 'Salvar' : 'Criar'}
             </button>
           </div>
         </form>
       </div>
     `;
     document.body.appendChild(overlay);
   
     document.getElementById('form-marca').addEventListener('submit', async (e) => {
       e.preventDefault();
       const btn = document.getElementById('btn-salvar');
       const alerta = document.getElementById('modal-alerta');
       alerta.classList.add('hidden');
       btn.disabled = true;
       btn.textContent = 'Salvando...';
   
       const body = {
         nome: document.getElementById('f-nome').value.trim(),
         status: document.getElementById('f-status').value,
       };
   
       try {
         if (editando) await adminApi.atualizar(TIPO, marca.id, body);
         else await adminApi.criar(TIPO, body);
         fecharModal();
         carregar();
       } catch (err) {
         alerta.textContent = err.message;
         alerta.classList.remove('hidden');
         btn.disabled = false;
         btn.textContent = editando ? 'Salvar' : 'Criar';
       }
     });
   }
   
   function fecharModal() {
     document.querySelector('.modal-overlay')?.remove();
   }
   
   async function alternar(id) {
     const marca = cache.find(m => m.id === id);
     if (!marca) return;
     try {
       await adminApi.atualizar(TIPO, id, {
         nome: marca.nome,
         status: marca.status === 'ATIVO' ? 'INATIVO' : 'ATIVO',
       });
       carregar();
     } catch (err) {
       alert('Erro: ' + err.message);
     }
   }
   
   /* ----- Helpers ----- */
   function escapeHtml(s) {
     return String(s ?? '').replace(/[&<>"']/g, m => ({
       '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
     }[m]));
   }
   function escapeAttr(s) { return escapeHtml(s); }
   
   /* ----- Init ----- */
   document.addEventListener('DOMContentLoaded', () => {
     if (montarLayout('Marcas', 'marcas')) carregar();
   });