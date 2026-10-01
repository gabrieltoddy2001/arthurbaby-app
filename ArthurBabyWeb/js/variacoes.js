/* ============================================
   variacoes.js — Tamanhos, Cores e Modelos
   Tela com 3 abas
   ============================================ */

   let abaAtual = 'tamanhos';
   let cache = [];
   
   const CONFIG = {
     tamanhos: {
       titulo: 'Tamanhos',
       temOrdem: true,
       temHex: false,
       statusAtivo: 'ATIVO',
       statusInativo: 'INATIVO',
     },
     cores: {
       titulo: 'Cores',
       temOrdem: false,
       temHex: true,
       statusAtivo: 'ATIVA',
       statusInativo: 'INATIVA',
     },
     modelos: {
       titulo: 'Modelos',
       temOrdem: false,
       temHex: false,
       statusAtivo: 'ATIVO',
       statusInativo: 'INATIVO',
     },
   };
   
   function trocarAba(novaAba) {
     abaAtual = novaAba;
     carregar();
   }
   
   async function carregar() {
     const conteudo = document.getElementById('conteudo');
   
     // Se a estrutura (abas) ainda não existe, cria UMA vez
     if (!document.getElementById('abas')) {
       conteudo.innerHTML = `
         <div class="abas">
           <button class="aba" data-aba="tamanhos" onclick="trocarAba('tamanhos')">Tamanhos</button>
           <button class="aba" data-aba="cores" onclick="trocarAba('cores')">Cores</button>
           <button class="aba" data-aba="modelos" onclick="trocarAba('modelos')">Modelos</button>
         </div>
         <div id="painel-aba"></div>
       `;
     }
   
     // Atualiza o estado das abas
     document.querySelectorAll('.aba').forEach(btn => {
       btn.classList.toggle('ativa', btn.dataset.aba === abaAtual);
     });
   
     // Carrega os dados da aba atual
     try {
       cache = await adminApi.listar(abaAtual);
       renderTabela(cache);
     } catch (err) {
       document.getElementById('painel-aba').innerHTML =
         `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderTabela(lista) {
     const painel = document.getElementById('painel-aba');
     const cfg = CONFIG[abaAtual];
   
     // Cria a estrutura da aba só uma vez
     if (!document.getElementById(`tabela-${abaAtual}`)) {
       painel.innerHTML = `
         <div class="acoes-topo">
           <input type="text" id="filtro" placeholder="Filtrar por nome..." oninput="filtrar()">
           <button class="btn btn-primary" onclick="criar()">+ Novo ${cfg.titulo.slice(0, -1).toLowerCase()}</button>
         </div>
         <table class="tabela">
           <thead>
             <tr id="cabecalho">
               <th>ID</th>
               <th>Nome</th>
               ${cfg.temHex ? '<th>Cor</th>' : ''}
               ${cfg.temOrdem ? '<th>Ordem</th>' : ''}
               <th>Status</th>
               <th>Ações</th>
             </tr>
           </thead>
           <tbody id="tabela-${abaAtual}"></tbody>
         </table>
       `;
     }
   
     // Atualiza só o corpo da tabela
     const tbody = document.getElementById(`tabela-${abaAtual}`);
   
     tbody.innerHTML = lista.length === 0
       ? `<tr><td colspan="6" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
            Nenhum registro encontrado.
          </td></tr>`
       : lista.map(item => {
           const statusOk = item.status === cfg.statusAtivo;
           return `
             <tr>
               <td>${item.id}</td>
               <td><strong>${escapeHtml(item.nome)}</strong></td>
               ${cfg.temHex ? `
                 <td>
                   <span style="display:inline-block;width:22px;height:22px;border-radius:50%;
                                background:${item.codigoHex || '#ccc'};
                                border:1px solid var(--borda);vertical-align:middle;margin-right:.4rem;"></span>
                   <code style="font-size:.8rem;color:var(--texto-secundario)">${item.codigoHex || '—'}</code>
                 </td>` : ''}
               ${cfg.temOrdem ? `<td>${item.ordem ?? '—'}</td>` : ''}
               <td>
                 <span class="badge ${statusOk ? 'badge-ativo' : 'badge-inativo'}">
                   ${item.status}
                 </span>
               </td>
               <td>
                 <div class="acoes-linha">
                   <button class="btn-mini editar" onclick="editar(${item.id})">Editar</button>
                   <button class="btn-mini ${statusOk ? 'inativar' : 'ativar'}"
                           onclick="alternar(${item.id})">
                     ${statusOk ? 'Inativar' : 'Ativar'}
                   </button>
                 </div>
               </td>
             </tr>`;
         }).join('');
   }
   
   function filtrar() {
     const termo = document.getElementById('filtro').value.toLowerCase();
     renderTabela(cache.filter(x => x.nome.toLowerCase().includes(termo)));
   }
   
   function criar() { abrirModal(null); }
   
   function editar(id) {
     const item = cache.find(x => x.id === id);
     if (item) abrirModal(item);
   }
   
   function abrirModal(item) {
     const editando = !!item;
     const cfg = CONFIG[abaAtual];
     const singular = cfg.titulo.slice(0, -1).toLowerCase();
   
     const overlay = document.createElement('div');
     overlay.className = 'modal-overlay';
     overlay.innerHTML = `
       <div class="modal">
         <h2>${editando ? 'Editar' : 'Novo'} ${singular}</h2>
         <div id="modal-alerta" class="alert alert-erro hidden"></div>
         <form id="form-item">
           <div class="field">
             <label>Nome *</label>
             <input type="text" id="f-nome" required value="${escapeAttr(item?.nome || '')}">
           </div>
   
           ${cfg.temHex ? `
             <div class="field">
               <label>Código da cor (hex)</label>
               <div style="display:flex; gap:.5rem; align-items:center;">
                 <input type="color" id="f-hex-picker" value="${item?.codigoHex || '#FFFFFF'}"
                        style="width:50px; height:40px; padding:0; border:1px solid var(--borda); border-radius:6px; cursor:pointer;">
                 <input type="text" id="f-hex" placeholder="#FFFFFF" value="${escapeAttr(item?.codigoHex || '#FFFFFF')}"
                        style="flex:1;" maxlength="7">
               </div>
             </div>
           ` : ''}
   
           ${cfg.temOrdem ? `
             <div class="field">
               <label>Ordem de exibição</label>
               <input type="number" id="f-ordem" min="0" value="${item?.ordem ?? 0}">
             </div>
           ` : ''}
   
           <div class="field">
             <label>Status</label>
             <select id="f-status">
               <option value="${cfg.statusAtivo}"
                 ${item?.status === cfg.statusAtivo || !editando ? 'selected' : ''}>
                 ${cfg.statusAtivo}
               </option>
               <option value="${cfg.statusInativo}"
                 ${item?.status === cfg.statusInativo ? 'selected' : ''}>
                 ${cfg.statusInativo}
               </option>
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
   
     // Sincroniza o color picker com o input de texto
     if (cfg.temHex) {
       const picker = document.getElementById('f-hex-picker');
       const texto = document.getElementById('f-hex');
       picker.addEventListener('input', () => { texto.value = picker.value.toUpperCase(); });
       texto.addEventListener('input', () => {
         if (/^#[0-9A-Fa-f]{6}$/.test(texto.value)) picker.value = texto.value;
       });
     }
   
     document.getElementById('form-item').addEventListener('submit', async (e) => {
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
   
       if (cfg.temHex) body.codigoHex = document.getElementById('f-hex').value.trim() || null;
       if (cfg.temOrdem) body.ordem = Number(document.getElementById('f-ordem').value) || 0;
   
       try {
         if (editando) await adminApi.atualizar(abaAtual, item.id, body);
         else await adminApi.criar(abaAtual, body);
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
     const item = cache.find(x => x.id === id);
     if (!item) return;
     const cfg = CONFIG[abaAtual];
   
     const body = {
       nome: item.nome,
       status: item.status === cfg.statusAtivo ? cfg.statusInativo : cfg.statusAtivo,
     };
     if (cfg.temHex) body.codigoHex = item.codigoHex || null;
     if (cfg.temOrdem) body.ordem = item.ordem ?? 0;
   
     try {
       await adminApi.atualizar(abaAtual, id, body);
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
  if (montarLayout('Atributos', 'variacoes')) carregar();
});