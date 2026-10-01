/* ============================================
   usuarios.js — CRUD de usuários internos (Admin/Vendedor)
   ============================================ */

   let usuarios = [];

   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       const todos = await adminApi.listar('usuarios');
       // Filtra só usuários internos (Admin e Vendedor)
       usuarios = todos.filter(u => u.perfil === 'ADMINISTRADOR' || u.perfil === 'VENDEDOR');
       renderTudo();
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderTudo() {
     const conteudo = document.getElementById('conteudo');
   
     if (!document.getElementById('tabela-usuarios')) {
       conteudo.innerHTML = `
         <div class="filtros">
           <input type="text" id="f-busca" placeholder="Buscar por nome ou e-mail..." oninput="aplicarFiltros()">
           <select id="f-perfil" onchange="aplicarFiltros()">
             <option value="">Todos os perfis</option>
             <option value="ADMINISTRADOR">Administrador</option>
             <option value="VENDEDOR">Vendedor</option>
           </select>
           <select id="f-status" onchange="aplicarFiltros()">
             <option value="">Todos os status</option>
             <option value="ATIVO">ATIVO</option>
             <option value="INATIVO">INATIVO</option>
             <option value="BLOQUEADO">BLOQUEADO</option>
           </select>
           <button class="btn btn-primary" onclick="criar()">+ Novo usuário</button>
         </div>
         <table class="tabela">
           <thead>
             <tr>
               <th>Nome</th>
               <th>E-mail</th>
               <th>Perfil</th>
               <th>Status</th>
               <th>Ações</th>
             </tr>
           </thead>
           <tbody id="tabela-usuarios"></tbody>
         </table>
       `;
     }
   
     aplicarFiltros();
   }
   
   function aplicarFiltros() {
     const input = document.getElementById('f-busca');
     const selPerfil = document.getElementById('f-perfil');
     const selStatus = document.getElementById('f-status');
     if (!input || !selPerfil || !selStatus) return;
   
     const busca = input.value.toLowerCase().trim();
     const perfil = selPerfil.value;
     const status = selStatus.value;
   
     const filtrados = usuarios.filter(u => {
       if (busca) {
         const alvo = `${u.nome} ${u.email}`.toLowerCase();
         if (!alvo.includes(busca)) return false;
       }
       if (perfil && u.perfil !== perfil) return false;
       if (status && u.status !== status) return false;
       return true;
     });
   
     renderTabela(filtrados);
   }
   
   function renderTabela(lista) {
     const tbody = document.getElementById('tabela-usuarios');
   
     tbody.innerHTML = lista.length === 0
       ? `<tr><td colspan="5" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
            Nenhum usuário encontrado.
          </td></tr>`
       : lista.map(u => {
           const badgePerfil = u.perfil === 'ADMINISTRADOR' ? 'badge-esgotado' : 'badge-ativo';
           const labelPerfil = u.perfil === 'ADMINISTRADOR' ? 'Administrador' : 'Vendedor';
           const badgeStatus = {
             'ATIVO': 'badge-ativo',
             'INATIVO': 'badge-inativo',
             'BLOQUEADO': 'badge-esgotado',
           }[u.status] || 'badge-inativo';
   
           return `
             <tr>
               <td>
                 <strong>${escapeHtml(u.nome)}</strong><br>
                 <span style="font-size:.8rem;color:var(--texto-secundario);">${escapeHtml(u.cpf || '—')}</span>
               </td>
               <td>${escapeHtml(u.email)}</td>
               <td><span class="badge ${badgePerfil}">${labelPerfil}</span></td>
               <td><span class="badge ${badgeStatus}">${u.status}</span></td>
               <td>
                 <div class="acoes-linha">
                   <button class="btn-mini editar" onclick="editar(${u.id})">Editar</button>
                   <button class="btn-mini ${u.status === 'ATIVO' ? 'inativar' : 'ativar'}"
                           onclick="alternar(${u.id})">
                     ${u.status === 'ATIVO' ? 'Inativar' : 'Ativar'}
                   </button>
                   <button class="btn-mini" style="background:#FEF3C7;color:#92400E;"
                           onclick="resetarSenha(${u.id})">Resetar</button>
                 </div>
               </td>
             </tr>`;
         }).join('');
   }
   
   /* ============================================
      CRIAR / EDITAR
      ============================================ */
   function criar() { abrirModal(null); }
   
   function editar(id) {
     const u = usuarios.find(x => x.id === id);
     if (u) abrirModal(u);
   }
   
   function abrirModal(usuario) {
     const editando = !!usuario;
     const u = usuario || {};
   
     const overlay = document.createElement('div');
     overlay.className = 'modal-overlay';
     overlay.innerHTML = `
       <div class="modal">
         <h2>${editando ? 'Editar usuário' : 'Novo usuário interno'}</h2>
         <div id="modal-alerta" class="alert alert-erro hidden"></div>
         <form id="form-usuario">
           <div class="field">
             <label>Nome completo *</label>
             <input type="text" id="u-nome" value="${escapeAttr(u.nome || '')}" required>
           </div>
           <div class="field">
             <label>E-mail *</label>
             <input type="email" id="u-email" value="${escapeAttr(u.email || '')}" required>
           </div>
           <div class="grid-2">
             <div class="field">
               <label>CPF</label>
               <input type="text" id="u-cpf" value="${escapeAttr(u.cpf || '')}" placeholder="000.000.000-00">
             </div>
             <div class="field">
               <label>Telefone</label>
               <input type="text" id="u-telefone" value="${escapeAttr(u.telefone || '')}" placeholder="(71) 99999-9999">
             </div>
           </div>
           <div class="grid-2">
             <div class="field">
               <label>Perfil *</label>
               <select id="u-perfil">
                 <option value="ADMINISTRADOR" ${u.perfil === 'ADMINISTRADOR' || !editando ? 'selected' : ''}>Administrador</option>
                 <option value="VENDEDOR" ${u.perfil === 'VENDEDOR' ? 'selected' : ''}>Vendedor</option>
               </select>
             </div>
             <div class="field">
               <label>Status</label>
               <select id="u-status">
                 <option value="ATIVO" ${u.status === 'ATIVO' || !editando ? 'selected' : ''}>ATIVO</option>
                 <option value="INATIVO" ${u.status === 'INATIVO' ? 'selected' : ''}>INATIVO</option>
                 <option value="BLOQUEADO" ${u.status === 'BLOQUEADO' ? 'selected' : ''}>BLOQUEADO</option>
               </select>
             </div>
           </div>
           ${!editando ? `
             <div class="field">
               <label>Senha inicial *</label>
               <input type="text" id="u-senha" value="123456" required>
               <small style="color:var(--texto-secundario);font-size:.8rem;">
                 O usuário deverá alterar no primeiro acesso.
               </small>
             </div>
           ` : ''}
           <div class="modal-acoes">
             <button type="button" class="btn btn-ghost" onclick="fecharModal()">Cancelar</button>
             <button type="submit" class="btn btn-primary" id="u-salvar">
               ${editando ? 'Salvar' : 'Criar'}
             </button>
           </div>
         </form>
       </div>
     `;
     document.body.appendChild(overlay);
   
     document.getElementById('form-usuario').addEventListener('submit', async (e) => {
       e.preventDefault();
       await salvar(usuario);
     });
   }
   
   async function salvar(usuario) {
     const alerta = document.getElementById('modal-alerta');
     const btn = document.getElementById('u-salvar');
     alerta.classList.add('hidden');
   
     const body = {
       nome: document.getElementById('u-nome').value.trim(),
       email: document.getElementById('u-email').value.trim(),
       cpf: document.getElementById('u-cpf').value.trim() || null,
       telefone: document.getElementById('u-telefone').value.trim() || null,
       perfil: document.getElementById('u-perfil').value,
       status: document.getElementById('u-status').value,
       enderecos: usuario?.enderecos || [],
     };
   
     if (!body.nome) return mostrarErroModal('O nome é obrigatório.');
     if (!body.email) return mostrarErroModal('O e-mail é obrigatório.');
   
     // Valida e-mail duplicado
     const duplicado = usuarios.find(u => u.email === body.email && u.id !== usuario?.id);
     if (duplicado) return mostrarErroModal('Já existe um usuário com esse e-mail.');
   
     btn.disabled = true;
     btn.textContent = 'Salvando...';
   
     try {
       if (usuario) {
         await adminApi.atualizar('usuarios', usuario.id, body);
       } else {
         const senha = document.getElementById('u-senha').value;
         body.criadoEm = new Date().toISOString();
         await adminApi.criar('usuarios', body);
         // A senha não fica no mock (isso é só no backend real)
         console.log('Senha inicial definida:', senha);
       }
       fecharModal();
       await carregar();
     } catch (err) {
       mostrarErroModal(err.message);
     }
   }
   
   function mostrarErroModal(msg) {
     const alerta = document.getElementById('modal-alerta');
     const btn = document.getElementById('u-salvar');
     alerta.textContent = msg;
     alerta.classList.remove('hidden');
     btn.disabled = false;
     btn.textContent = 'Salvar';
   }
   
   /* ============================================
      ATIVAR / INATIVAR
      ============================================ */
   async function alternar(id) {
     const u = usuarios.find(x => x.id === id);
     if (!u) return;
   
     // Não deixa inativar o último admin ativo
     if (u.perfil === 'ADMINISTRADOR' && u.status === 'ATIVO') {
       const outrosAdminsAtivos = usuarios.filter(
         x => x.id !== id && x.perfil === 'ADMINISTRADOR' && x.status === 'ATIVO'
       ).length;
       if (outrosAdminsAtivos === 0) {
         alert('Não é possível inativar o último administrador ativo do sistema.');
         return;
       }
     }
   
     const novoStatus = u.status === 'ATIVO' ? 'INATIVO' : 'ATIVO';
   
     if (!confirm(`Alterar status de "${u.nome}" para ${novoStatus}?`)) return;
   
     try {
       await adminApi.atualizar('usuarios', id, { ...u, status: novoStatus });
       await carregar();
     } catch (err) {
       alert('Erro: ' + err.message);
     }
   }
   
   /* ============================================
      RESETAR SENHA
      ============================================ */
   function resetarSenha(id) {
     const u = usuarios.find(x => x.id === id);
     if (!u) return;
   
     if (!confirm(`Resetar a senha de "${u.nome}"?\n\nUma senha temporária será enviada para ${u.email}.`)) return;
   
     alert(`✅ Senha temporária enviada para ${u.email}.\n\nO usuário deverá alterá-la no próximo login.`);
   }
   
   function fecharModal() {
     document.querySelector('.modal-overlay')?.remove();
   }
   
   /* ============================================
      HELPERS
      ============================================ */
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
     if (montarLayout('Usuários internos', 'usuarios')) carregar();
   });