/* ============================================
   categorias.js — CRUD de categorias
   Com suporte a subcategorias (categoria pai)
   ============================================ */

   const TIPO = 'categorias';
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
  
    // Se a estrutura ainda não existe, cria ela UMA vez
    if (!document.getElementById('tabela-categorias')) {
      conteudo.innerHTML = `
        <div class="acoes-topo">
          <input type="text" id="filtro" placeholder="Filtrar por nome..." oninput="filtrar()">
          <button class="btn btn-primary" onclick="criar()">+ Nova categoria</button>
        </div>
        <table class="tabela">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Categoria pai</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody id="tabela-categorias"></tbody>
        </table>
      `;
    }
  
    // Agora só atualiza o corpo da tabela
    const tbody = document.getElementById('tabela-categorias');
  
    const raizes = lista.filter(c => !c.categoriaPaiId);
    const filhas = lista.filter(c => c.categoriaPaiId);
  
    const linhas = [];
  
    raizes.forEach(c => {
      linhas.push(linhaTabela(c, false));
      filhas
        .filter(f => f.categoriaPaiId === c.id)
        .forEach(f => linhas.push(linhaTabela(f, true)));
    });
  
    const idsRaizes = raizes.map(r => r.id);
    filhas
      .filter(f => !idsRaizes.includes(f.categoriaPaiId))
      .forEach(f => linhas.push(linhaTabela(f, true)));
  
    tbody.innerHTML = linhas.length === 0
      ? `<tr><td colspan="5" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
           Nenhuma categoria encontrada.
         </td></tr>`
      : linhas.join('');
  }
   
   function linhaTabela(c, ehSub) {
     const pai = c.categoriaPaiId
       ? cache.find(x => x.id === c.categoriaPaiId)
       : null;
   
     const nomeExibido = ehSub
       ? `<span style="color:var(--texto-secundario)">↳</span> ${escapeHtml(c.nome)}`
       : `<strong>${escapeHtml(c.nome)}</strong>`;
   
     return `
       <tr>
         <td>${c.id}</td>
         <td>${nomeExibido}</td>
         <td>${pai ? escapeHtml(pai.nome) : '<span style="color:var(--texto-secundario)">—</span>'}</td>
         <td>
           <span class="badge ${c.status === 'ATIVA' ? 'badge-ativo' : 'badge-inativo'}">
             ${c.status}
           </span>
         </td>
         <td>
           <div class="acoes-linha">
             <button class="btn-mini editar" onclick="editar(${c.id})">Editar</button>
             <button class="btn-mini ${c.status === 'ATIVA' ? 'inativar' : 'ativar'}"
                     onclick="alternar(${c.id})">
               ${c.status === 'ATIVA' ? 'Inativar' : 'Ativar'}
             </button>
           </div>
         </td>
       </tr>
     `;
   }
   
   function filtrar() {
     const termo = document.getElementById('filtro').value.toLowerCase();
     renderTabela(cache.filter(c => c.nome.toLowerCase().includes(termo)));
   }
   
   function criar() { abrirModal(null); }
   
   function editar(id) {
     const cat = cache.find(c => c.id === id);
     if (cat) abrirModal(cat);
   }
   
   function abrirModal(categoria) {
     const editando = !!categoria;
   
     // Opções de categoria pai (só as raízes, sem a própria categoria sendo editada)
     const opcoesPai = cache
       .filter(c => !c.categoriaPaiId && c.id !== categoria?.id)
       .map(c => `<option value="${c.id}" ${categoria?.categoriaPaiId === c.id ? 'selected' : ''}>
                    ${escapeHtml(c.nome)}
                  </option>`)
       .join('');
   
     const overlay = document.createElement('div');
     overlay.className = 'modal-overlay';
     overlay.innerHTML = `
       <div class="modal">
         <h2>${editando ? 'Editar categoria' : 'Nova categoria'}</h2>
         <div id="modal-alerta" class="alert alert-erro hidden"></div>
         <form id="form-categoria">
           <div class="field">
             <label>Nome *</label>
             <input type="text" id="f-nome" required value="${escapeAttr(categoria?.nome || '')}">
           </div>
           <div class="field">
             <label>Descrição</label>
             <textarea id="f-descricao" rows="3">${escapeHtml(categoria?.descricao || '')}</textarea>
           </div>
           <div class="field">
             <label>Categoria pai</label>
             <select id="f-pai">
               <option value="">— Nenhuma (categoria raiz) —</option>
               ${opcoesPai}
             </select>
           </div>
           <div class="field">
             <label>Status</label>
             <select id="f-status">
               <option value="ATIVA" ${categoria?.status === 'ATIVA' || !editando ? 'selected' : ''}>ATIVA</option>
               <option value="INATIVA" ${categoria?.status === 'INATIVA' ? 'selected' : ''}>INATIVA</option>
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
   
     document.getElementById('form-categoria').addEventListener('submit', async (e) => {
       e.preventDefault();
       const btn = document.getElementById('btn-salvar');
       const alerta = document.getElementById('modal-alerta');
       alerta.classList.add('hidden');
       btn.disabled = true;
       btn.textContent = 'Salvando...';
   
       const paiValor = document.getElementById('f-pai').value;
   
       const body = {
         nome: document.getElementById('f-nome').value.trim(),
         descricao: document.getElementById('f-descricao').value.trim() || null,
         categoriaPaiId: paiValor ? Number(paiValor) : null,
         status: document.getElementById('f-status').value,
       };
   
       try {
         if (editando) await adminApi.atualizar(TIPO, categoria.id, body);
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
     const cat = cache.find(c => c.id === id);
     if (!cat) return;
   
     // Se estiver inativando uma categoria pai, avisa que as filhas continuam ativas
     if (cat.status === 'ATIVA') {
       const filhas = cache.filter(c => c.categoriaPaiId === id);
       if (filhas.length > 0) {
         const ok = confirm(
           `Esta categoria tem ${filhas.length} subcategoria(s). ` +
           `Elas continuarão ativas. Deseja inativar mesmo assim?`
         );
         if (!ok) return;
       }
     }
   
     try {
       await adminApi.atualizar(TIPO, id, {
         nome: cat.nome,
         descricao: cat.descricao,
         categoriaPaiId: cat.categoriaPaiId || null,
         status: cat.status === 'ATIVA' ? 'INATIVA' : 'ATIVA',
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
     if (montarLayout('Categorias', 'categorias')) carregar();
   });