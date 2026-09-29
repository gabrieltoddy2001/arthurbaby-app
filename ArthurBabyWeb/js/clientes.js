/* ============================================
   clientes.js — gestão de clientes
   ============================================ */

   let clientes = [];
   let pedidos = [];
   
   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       const [usuarios, peds] = await Promise.all([
         adminApi.listar('usuarios'),
         adminApi.listar('pedidos'),
       ]);
       // Filtra só clientes
       clientes = usuarios.filter(u => u.perfil === 'CLIENTE');
       pedidos = peds;
       renderTudo();
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderTudo() {
     const conteudo = document.getElementById('conteudo');
   
     if (!document.getElementById('tabela-clientes')) {
       conteudo.innerHTML = `
         <div class="filtros">
           <input type="text" id="f-busca" placeholder="Buscar por nome, e-mail ou CPF..." oninput="aplicarFiltros()">
           <select id="f-status" onchange="aplicarFiltros()">
             <option value="">Todos os status</option>
             <option value="ATIVO">ATIVO</option>
             <option value="INATIVO">INATIVO</option>
             <option value="BLOQUEADO">BLOQUEADO</option>
           </select>
         </div>
         <table class="tabela">
           <thead>
             <tr>
               <th>Cliente</th>
               <th>CPF</th>
               <th>Telefone</th>
               <th class="text-center">Pedidos</th>
               <th>Status</th>
               <th>Ações</th>
             </tr>
           </thead>
           <tbody id="tabela-clientes"></tbody>
         </table>
       `;
     }
   
     aplicarFiltros();
   }
   
   function aplicarFiltros() {
     const input = document.getElementById('f-busca');
     const sel = document.getElementById('f-status');
     if (!input || !sel) return;
   
     const busca = input.value.toLowerCase().trim();
     const status = sel.value;
   
     const filtrados = clientes.filter(c => {
       if (busca) {
         const alvo = `${c.nome} ${c.email} ${c.cpf}`.toLowerCase();
         if (!alvo.includes(busca)) return false;
       }
       if (status && c.status !== status) return false;
       return true;
     });
   
     renderTabela(filtrados);
   }
   
   function renderTabela(lista) {
     const tbody = document.getElementById('tabela-clientes');
   
     tbody.innerHTML = lista.length === 0
       ? `<tr><td colspan="6" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
            Nenhum cliente encontrado.
          </td></tr>`
       : lista.map(c => {
           const qtdPedidos = pedidos.filter(p => p.clienteId === c.id).length;
           const badge = {
             'ATIVO': 'badge-ativo',
             'INATIVO': 'badge-inativo',
             'BLOQUEADO': 'badge-esgotado',
           }[c.status] || 'badge-inativo';
   
           return `
             <tr>
               <td>
                 <strong>${escapeHtml(c.nome)}</strong><br>
                 <span style="font-size:.8rem;color:var(--texto-secundario);">${escapeHtml(c.email)}</span>
               </td>
               <td>${escapeHtml(c.cpf || '—')}</td>
               <td>${escapeHtml(c.telefone || '—')}</td>
               <td class="text-center">${qtdPedidos}</td>
               <td><span class="badge ${badge}">${c.status}</span></td>
               <td>
                 <div class="acoes-linha">
                   <button class="btn-mini editar" onclick="detalhar(${c.id})">Detalhar</button>
                   <button class="btn-mini ${c.status === 'BLOQUEADO' ? 'ativar' : 'inativar'}"
                           onclick="alternarBloqueio(${c.id})">
                     ${c.status === 'BLOQUEADO' ? 'Desbloquear' : 'Bloquear'}
                   </button>
                 </div>
               </td>
             </tr>`;
         }).join('');
   }
   
   /* ============================================
      DETALHAR CLIENTE (modal)
      ============================================ */
   function detalhar(id) {
     const c = clientes.find(x => x.id === id);
     if (!c) return;
   
     const meusPedidos = pedidos.filter(p => p.clienteId === c.id)
       .sort((a, b) => new Date(b.criadoEm) - new Date(a.criadoEm));
   
     // Endereços
     const enderecosHtml = (c.enderecos || []).length === 0
       ? '<p style="color:var(--texto-secundario);font-size:.9rem;">Nenhum endereço cadastrado.</p>'
       : c.enderecos.map(e => `
           <div style="padding:.6rem;background:var(--superficie);border-radius:6px;margin-bottom:.4rem;font-size:.85rem;">
             ${e.principal ? '<span class="badge badge-ativo" style="font-size:.65rem;">PRINCIPAL</span><br>' : ''}
             ${escapeHtml(e.logradouro)}, ${escapeHtml(e.numero)}
             ${e.complemento ? ' — ' + escapeHtml(e.complemento) : ''}<br>
             ${escapeHtml(e.bairro)} — ${escapeHtml(e.cidade)}/${escapeHtml(e.uf)}<br>
             CEP: ${escapeHtml(e.cep)}
           </div>
         `).join('');
   
     // Pedidos
     const pedidosHtml = meusPedidos.length === 0
       ? '<p style="color:var(--texto-secundario);font-size:.9rem;">Nenhum pedido realizado.</p>'
       : meusPedidos.map(p => `
           <div style="display:flex;justify-content:space-between;padding:.5rem .6rem;background:var(--superficie);border-radius:6px;margin-bottom:.4rem;font-size:.85rem;">
             <div>
               <strong>${escapeHtml(p.numeroPedido)}</strong><br>
               <span style="color:var(--texto-secundario);">${formatarData(p.criadoEm)}</span>
             </div>
             <div style="text-align:right;">
               <span class="badge ${badgeStatus(p.status)}">${p.status}</span><br>
               <strong>R$ ${formatarPreco(p.total)}</strong>
             </div>
           </div>
         `).join('');
   
     const overlay = document.createElement('div');
     overlay.className = 'modal-overlay';
     overlay.innerHTML = `
       <div class="modal" style="max-width:600px;">
         <h2>${escapeHtml(c.nome)}</h2>
   
         <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem;margin-bottom:1.25rem;font-size:.9rem;">
           <div>
             <div style="color:var(--texto-secundario);font-size:.75rem;text-transform:uppercase;">E-mail</div>
             <div>${escapeHtml(c.email)}</div>
           </div>
           <div>
             <div style="color:var(--texto-secundario);font-size:.75rem;text-transform:uppercase;">Telefone</div>
             <div>${escapeHtml(c.telefone || '—')}</div>
           </div>
           <div>
             <div style="color:var(--texto-secundario);font-size:.75rem;text-transform:uppercase;">CPF</div>
             <div>${escapeHtml(c.cpf || '—')}</div>
           </div>
           <div>
             <div style="color:var(--texto-secundario);font-size:.75rem;text-transform:uppercase;">Status</div>
             <div><span class="badge ${
               c.status === 'ATIVO' ? 'badge-ativo' :
               c.status === 'BLOQUEADO' ? 'badge-esgotado' : 'badge-inativo'
             }">${c.status}</span></div>
           </div>
           <div>
             <div style="color:var(--texto-secundario);font-size:.75rem;text-transform:uppercase;">Cliente desde</div>
             <div>${formatarData(c.criadoEm)}</div>
           </div>
         </div>
   
         <h3 class="secao-titulo" style="margin-top:1rem;">Endereços</h3>
         ${enderecosHtml}
   
         <h3 class="secao-titulo" style="margin-top:1.25rem;">Pedidos (${meusPedidos.length})</h3>
         ${pedidosHtml}
   
         <div class="modal-acoes" style="margin-top:1.5rem;justify-content:space-between;">
           <button type="button" class="btn btn-accent" onclick="resetarSenha(${c.id})">
             🔑 Resetar senha
           </button>
           <button type="button" class="btn btn-ghost" onclick="fecharModal()">Fechar</button>
         </div>
       </div>
     `;
     document.body.appendChild(overlay);
   }
   
   /* ============================================
      BLOQUEAR / DESBLOQUEAR
      ============================================ */
   async function alternarBloqueio(id) {
     const c = clientes.find(x => x.id === id);
     if (!c) return;
   
     const novoStatus = c.status === 'BLOQUEADO' ? 'ATIVO' : 'BLOQUEADO';
     const msg = novoStatus === 'BLOQUEADO'
       ? `Bloquear o cliente "${c.nome}"? Ele não conseguirá fazer login no app.`
       : `Desbloquear o cliente "${c.nome}"?`;
   
     if (!confirm(msg)) return;
   
     try {
       await adminApi.atualizar('usuarios', id, { ...c, status: novoStatus });
       await carregar();
     } catch (err) {
       alert('Erro: ' + err.message);
     }
   }
   
   /* ============================================
      RESETAR SENHA
      ============================================ */
   function resetarSenha(id) {
     const c = clientes.find(x => x.id === id);
     if (!c) return;
   
     if (!confirm(`Resetar a senha de "${c.nome}"?\n\nUma senha temporária será enviada para ${c.email}.`)) return;
   
     // Simula o envio de e-mail
     alert(`✅ Senha temporária enviada para ${c.email}.\n\nO cliente deverá alterá-la no próximo login.`);
   
     // Registra na auditoria
     registrarAuditoria({
       usuario: getUsuario()?.nome || 'Administrador',
       acao: 'RESETAR_SENHA',
       entidade: 'Usuario',
       entidadeId: id,
       descricao: `Resetou a senha do cliente ${c.nome}`,
     });
   }
   
   /* ============================================
      AUDITORIA (registra ação)
      ============================================ */
   async function registrarAuditoria(registro) {
     try {
       await adminApi.criar('auditoria', {
         ...registro,
         data: new Date().toISOString(),
       });
     } catch (e) {
       console.warn('Falha ao registrar auditoria', e);
     }
   }
   
   function fecharModal() {
     document.querySelector('.modal-overlay')?.remove();
   }
   
   /* ============================================
      HELPERS
      ============================================ */
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
   
   /* ============================================
      INIT
      ============================================ */
   document.addEventListener('DOMContentLoaded', () => {
     if (montarLayout('Clientes', 'clientes')) carregar();
   });