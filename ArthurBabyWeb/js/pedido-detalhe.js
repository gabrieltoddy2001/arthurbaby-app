/* ============================================
   pedido-detalhe.js — detalhe do pedido
   Com itens, totais, mudança de status, cancelamento e histórico
   ============================================ */

   const TIPO = 'pedidos';
   let pedido = null;
   
   const STATUS_LISTA = [
     'RASCUNHO', 'PEDIDO_GERADO', 'EM_ANALISE', 'AGUARDANDO_CONFIRMACAO',
     'CONFIRMADO', 'SEPARANDO_PRODUTOS', 'PRONTO_PARA_RETIRADA',
     'EM_TRANSPORTE', 'ENTREGUE', 'CANCELADO',
   ];
   
   function getIdUrl() {
     const params = new URLSearchParams(window.location.search);
     const id = params.get('id');
     return id ? Number(id) : null;
   }
   
   async function init() {
     const usuario = exigirLogin();
     if (!usuario) return;
   
     const id = getIdUrl();
     if (!id) {
       document.getElementById('conteudo').innerHTML =
         '<div class="alert alert-erro">ID do pedido não informado.</div>';
       return;
     }
   
     try {
       const lista = await adminApi.listar(TIPO);
       pedido = lista.find(p => p.id === id) || null;
       if (!pedido) throw new Error('Pedido não encontrado');
       renderDetalhe();
     } catch (err) {
       document.getElementById('conteudo').innerHTML =
         `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderDetalhe() {
     const conteudo = document.getElementById('conteudo');
     const p = pedido;
   
     // Linhas dos itens
     const itensHtml = (p.itens || []).map(it => `
       <tr>
         <td>
           <strong>${escapeHtml(it.produtoNome)}</strong><br>
           <span style="font-size:.8rem;color:var(--texto-secundario);">${escapeHtml(it.variacaoDescricao || '—')}</span>
         </td>
         <td class="text-center">${it.quantidade}</td>
         <td class="text-right">R$ ${formatarPreco(it.valorUnitario)}</td>
         <td class="text-right">R$ ${formatarPreco(it.desconto)}</td>
         <td class="text-right"><strong>R$ ${formatarPreco(it.valorTotal)}</strong></td>
       </tr>
     `).join('') || `<tr><td colspan="5" class="text-center" style="padding:2rem;color:var(--texto-secundario)">Nenhum item.</td></tr>`;
   
     // Histórico (timeline)
     const historicoHtml = (p.historico || []).map((h, i, arr) => `
       <div class="timeline-item ${i === arr.length - 1 ? 'atual' : ''}">
         <div class="timeline-ponto"></div>
         <div class="timeline-conteudo">
           <div class="timeline-status">
             <span class="badge ${badgeStatus(h.status)}">${h.status}</span>
           </div>
           <div class="timeline-info">
             <strong>${escapeHtml(h.usuario)}</strong> • ${formatarData(h.data)}
           </div>
           ${h.observacao ? `<div class="timeline-obs">${escapeHtml(h.observacao)}</div>` : ''}
         </div>
       </div>
     `).join('') || '<p style="color:var(--texto-secundario)">Sem histórico.</p>';
   
     // Endereço de entrega (se for entrega)
     const enderecoHtml = p.formaRecebimento === 'ENTREGA' && p.enderecoEntrega
       ? `
         <div class="card">
           <h3 class="secao-titulo">Endereço de entrega</h3>
           <p>
             ${escapeHtml(p.enderecoEntrega.logradouro)}, ${escapeHtml(p.enderecoEntrega.numero)}
             ${p.enderecoEntrega.complemento ? ' — ' + escapeHtml(p.enderecoEntrega.complemento) : ''}<br>
             ${escapeHtml(p.enderecoEntrega.bairro)} — ${escapeHtml(p.enderecoEntrega.cidade)}/${escapeHtml(p.enderecoEntrega.uf)}<br>
             CEP: ${escapeHtml(p.enderecoEntrega.cep)}
             ${p.enderecoEntrega.referencia ? '<br>Ref: ' + escapeHtml(p.enderecoEntrega.referencia) : ''}
           </p>
         </div>` : '';
   
     // Botão de cancelar (só se não estiver cancelado nem entregue)
     const podeCancelar = p.status !== 'CANCELADO' && p.status !== 'ENTREGUE';
   
     // Motivo do cancelamento (se cancelado)
     const motivoHtml = p.status === 'CANCELADO' && p.motivoCancelamento
       ? `<div class="alert alert-erro" style="margin-top:1rem;">
            <strong>Motivo do cancelamento:</strong> ${escapeHtml(p.motivoCancelamento)}
          </div>` : '';
   
     conteudo.innerHTML = `
       <div class="form-produto">
         <div class="form-header">
           <div>
             <h1>Pedido ${escapeHtml(p.numeroPedido)}</h1>
             <p style="color:var(--texto-secundario);font-size:.9rem;">
               Criado em ${formatarData(p.criadoEm)}
             </p>
           </div>
           <div>
             <button class="btn btn-ghost" onclick="voltar()">Voltar</button>
             ${podeCancelar ? `<button class="btn btn-danger" onclick="cancelar()">Cancelar pedido</button>` : ''}
           </div>
         </div>
   
         <div id="alerta-pedido" class="alert alert-erro hidden"></div>
         ${motivoHtml}
   
         <!-- Status atual + mudança -->
         <div class="card">
           <h3 class="secao-titulo">Status do pedido</h3>
           <div class="status-linha">
             <span class="badge ${badgeStatus(p.status)}" style="font-size:.9rem;padding:.4rem .9rem;">
               ${p.status}
             </span>
             ${p.status !== 'CANCELADO' && p.status !== 'ENTREGUE' ? `
               <select id="f-novo-status" style="margin-left:1rem;">
                 ${STATUS_LISTA
                   .filter(s => s !== 'CANCELADO')
                   .map(s => `<option value="${s}" ${s === p.status ? 'selected' : ''}>${s}</option>`)
                   .join('')}
               </select>
               <button class="btn btn-primary btn-sm" onclick="mudarStatus()">Atualizar</button>
             ` : ''}
           </div>
         </div>
   
         <!-- Dados do cliente -->
         <div class="card">
           <h3 class="secao-titulo">Cliente</h3>
           <p>
             <strong>${escapeHtml(p.clienteNome)}</strong><br>
             ${escapeHtml(p.clienteEmail || '')}<br>
             ${escapeHtml(p.clienteTelefone || '')}
           </p>
         </div>
   
         ${enderecoHtml}
   
         <!-- Itens -->
         <div class="card">
           <h3 class="secao-titulo">Itens do pedido</h3>
           <div class="tabela-rolavel">
             <table class="tabela tabela-mini">
               <thead>
                 <tr>
                   <th>Produto</th>
                   <th class="text-center">Qtd</th>
                   <th class="text-right">Valor unit.</th>
                   <th class="text-right">Desconto</th>
                   <th class="text-right">Total</th>
                 </tr>
               </thead>
               <tbody>${itensHtml}</tbody>
             </table>
           </div>
   
           <div class="totais">
             <div><span>Subtotal:</span> <strong>R$ ${formatarPreco(p.subtotal)}</strong></div>
             ${p.desconto ? `<div><span>Desconto:</span> <strong>- R$ ${formatarPreco(p.desconto)}</strong></div>` : ''}
             <div><span>Frete:</span> <strong>R$ ${formatarPreco(p.frete)}</strong></div>
             <div class="total-final"><span>Total:</span> <strong>R$ ${formatarPreco(p.total)}</strong></div>
           </div>
         </div>
   
         <!-- Forma de recebimento -->
         <div class="card">
           <h3 class="secao-titulo">Forma de recebimento</h3>
           <p>${p.formaRecebimento === 'ENTREGA' ? '🚚 Entrega no endereço' : '🏬 Retirada na loja'}</p>
           ${p.observacao ? `<p style="margin-top:.5rem;"><strong>Observação:</strong> ${escapeHtml(p.observacao)}</p>` : ''}
         </div>
   
         <!-- Histórico -->
         <div class="card">
           <h3 class="secao-titulo">Histórico de status</h3>
           <div class="timeline">${historicoHtml}</div>
         </div>
       </div>
     `;
   }
   
   /* ============================================
      MUDAR STATUS
      ============================================ */
   async function mudarStatus() {
     const novoStatus = document.getElementById('f-novo-status').value;
     if (novoStatus === pedido.status) {
       alert('O status já é esse.');
       return;
     }
   
     const usuario = getUsuario();
     const agora = new Date().toISOString();
   
     // Atualiza o pedido
     const historicoAtualizado = [
       ...(pedido.historico || []),
       {
         status: novoStatus,
         data: agora,
         usuario: usuario?.nome || 'Administrador',
         observacao: `Status alterado de ${pedido.status} para ${novoStatus}.`,
       },
     ];
   
     const atualizado = {
       ...pedido,
       status: novoStatus,
       historico: historicoAtualizado,
     };
   
     try {
       await adminApi.atualizar(TIPO, pedido.id, atualizado);
       pedido = atualizado;
       renderDetalhe();
       mostrarSucesso('Status atualizado com sucesso.');
     } catch (err) {
       mostrarErro('Erro ao atualizar: ' + err.message);
     }
   }
   
   /* ============================================
      CANCELAR PEDIDO
      ============================================ */
   async function cancelar() {
     const motivo = prompt('Motivo do cancelamento (obrigatório):');
     if (!motivo || !motivo.trim()) {
       if (motivo !== null) alert('É necessário informar o motivo.');
       return;
     }
   
     const usuario = getUsuario();
     const agora = new Date().toISOString();
   
     const historicoAtualizado = [
       ...(pedido.historico || []),
       {
         status: 'CANCELADO',
         data: agora,
         usuario: usuario?.nome || 'Administrador',
         observacao: `Cancelado: ${motivo.trim()}`,
       },
     ];
   
     const atualizado = {
       ...pedido,
       status: 'CANCELADO',
       motivoCancelamento: motivo.trim(),
       historico: historicoAtualizado,
     };
   
     try {
       await adminApi.atualizar(TIPO, pedido.id, atualizado);
       pedido = atualizado;
       renderDetalhe();
       mostrarSucesso('Pedido cancelado.');
     } catch (err) {
       mostrarErro('Erro ao cancelar: ' + err.message);
     }
   }
   
   function mostrarSucesso(msg) {
     const alerta = document.getElementById('alerta-pedido');
     alerta.className = 'alert alert-sucesso';
     alerta.textContent = msg;
     alerta.classList.remove('hidden');
     window.scrollTo({ top: 0, behavior: 'smooth' });
     setTimeout(() => alerta.classList.add('hidden'), 3000);
   }
   
   function mostrarErro(msg) {
     const alerta = document.getElementById('alerta-pedido');
     alerta.className = 'alert alert-erro';
     alerta.textContent = msg;
     alerta.classList.remove('hidden');
     window.scrollTo({ top: 0, behavior: 'smooth' });
   }
   
   function voltar() {
     window.location.href = 'pedidos.html';
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
     if (montarLayout('Detalhe do Pedido', 'pedidos')) init();
   });