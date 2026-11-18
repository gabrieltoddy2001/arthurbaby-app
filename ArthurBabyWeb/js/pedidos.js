/* ============================================
   pedidos.js — listagem de pedidos
   Indicadores, filtros (texto, status, forma, origem, período), exportação CSV
   ============================================ */

   const PENDENTES = ['RASCUNHO', 'PEDIDO_GERADO', 'EM_ANALISE', 'AGUARDANDO_CONFIRMACAO'];
   let cache = [];
   let filtradosAtuais = [];
   
   // Variáveis para os Selects Customizados
   let valorStatus = '';
   let valorForma = '';
   let valorOrigem = '';
   let valorPeriodo = '';
   
   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       cache = await pedidosApi.listar();
       renderTudo();
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${escapeHtml(err.message)}</div>`;
     }
   }
   
   function renderTudo() {
     const conteudo = document.getElementById('conteudo');
     const params = new URLSearchParams(window.location.search);
     const statusInicial = params.get('status') || '';
   
     // Se veio um status na URL, já aplica
     if (statusInicial) {
       valorStatus = statusInicial;
     }
   
     // Monta as opções de status
     const opcoesStatus = Object.entries(STATUS_PEDIDO)
       .map(([v, r]) => `<div class="custom-option" onclick="selecionarFiltro('status', '${v}', '${escapeHtml(r)}')">${escapeHtml(r)}</div>`).join('');
   
     conteudo.innerHTML = `
       <div id="resumo-pedidos" class="resumo-linha"></div>
       <div class="filtros">
         <input type="text" id="f-busca" placeholder="Buscar por número, cliente, e-mail ou telefone..." oninput="aplicarFiltros()">
         
         <!-- SELECT STATUS -->
         <div class="custom-select" id="custom-status" style="position:relative; min-width:160px;">
           <div class="custom-select-trigger" onclick="toggleSelect('status')">
             <span id="status-label" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
               ${valorStatus ? escapeHtml(STATUS_PEDIDO[valorStatus] || 'Status') : 'Todos os status'}
             </span>
             <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
           </div>
           <div class="custom-select-options hidden" id="status-options">
             <div class="custom-option" onclick="selecionarFiltro('status', '', 'Todos os status')">Todos os status</div>
             <div class="custom-option" onclick="selecionarFiltro('status', '__PENDENTES', 'Somente pendentes')">Somente pendentes</div>
             ${opcoesStatus}
           </div>
         </div>
   
         <!-- SELECT FORMA -->
         <div class="custom-select" id="custom-forma" style="position:relative; min-width:160px;">
           <div class="custom-select-trigger" onclick="toggleSelect('forma')">
             <span id="forma-label" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">Todas as formas</span>
             <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
           </div>
           <div class="custom-select-options hidden" id="forma-options">
             <div class="custom-option" onclick="selecionarFiltro('forma', '', 'Todas as formas')">Todas as formas</div>
             <div class="custom-option" onclick="selecionarFiltro('forma', 'RETIRADA_LOJA', 'Retirada na loja')">Retirada na loja</div>
             <div class="custom-option" onclick="selecionarFiltro('forma', 'ENTREGA', 'Entrega')">Entrega</div>
           </div>
         </div>
   
         <!-- SELECT ORIGEM -->
         <div class="custom-select" id="custom-origem" style="position:relative; min-width:160px;">
           <div class="custom-select-trigger" onclick="toggleSelect('origem')">
             <span id="origem-label" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">Todas as origens</span>
             <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
           </div>
           <div class="custom-select-options hidden" id="origem-options">
             <div class="custom-option" onclick="selecionarFiltro('origem', '', 'Todas as origens')">Todas as origens</div>
             <div class="custom-option" onclick="selecionarFiltro('origem', 'APP', 'App (cliente)')">App (cliente)</div>
             <div class="custom-option" onclick="selecionarFiltro('origem', 'PAINEL', 'Painel (vendedor)')">Painel (vendedor)</div>
           </div>
         </div>
   
         <!-- SELECT PERÍODO -->
         <div class="custom-select" id="custom-periodo" style="position:relative; min-width:160px;">
           <div class="custom-select-trigger" onclick="toggleSelect('periodo')">
             <span id="periodo-label" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">Todo o período</span>
             <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
           </div>
           <div class="custom-select-options hidden" id="periodo-options">
             <div class="custom-option" onclick="selecionarFiltro('periodo', '', 'Todo o período')">Todo o período</div>
             <div class="custom-option" onclick="selecionarFiltro('periodo', '0', 'Hoje')">Hoje</div>
             <div class="custom-option" onclick="selecionarFiltro('periodo', '7', 'Últimos 7 dias')">Últimos 7 dias</div>
             <div class="custom-option" onclick="selecionarFiltro('periodo', '30', 'Últimos 30 dias')">Últimos 30 dias</div>
             <div class="custom-option" onclick="selecionarFiltro('periodo', '90', 'Últimos 90 dias')">Últimos 90 dias</div>
           </div>
         </div>
   
         <button class="btn btn-ghost btn-sm" onclick="exportarCsv()" title="Exporta os pedidos filtrados">Exportar CSV</button>
         <button class="btn btn-primary" onclick="window.location.href='pedido-novo.html'">+ Novo pedido</button>
       </div>
       
       <div class="tabela-rolavel">
         <table class="tabela">
           <thead>
             <tr>
               <th>Número</th>
               <th>Cliente</th>
               <th>Data</th>
               <th>Forma</th>
               <th>Origem</th>
               <th class="text-center">Itens</th>
               <th class="text-right">Total</th>
               <th>Status</th>
               <th>Ações</th>
             </tr>
           </thead>
           <tbody id="tabela-pedidos"></tbody>
         </table>
       </div>
     `;
   
     aplicarFiltros();
   }
   
   /* ============================================
      CONTROLE DOS SELECTS CUSTOMIZADOS
      ============================================ */
   function toggleSelect(id) {
     document.getElementById(`${id}-options`).classList.toggle('hidden');
   }
   
   // Fecha os selects se clicar fora
   document.addEventListener('click', (e) => {
     if (!e.target.closest('#custom-status')) document.getElementById('status-options')?.classList.add('hidden');
     if (!e.target.closest('#custom-forma')) document.getElementById('forma-options')?.classList.add('hidden');
     if (!e.target.closest('#custom-origem')) document.getElementById('origem-options')?.classList.add('hidden');
     if (!e.target.closest('#custom-periodo')) document.getElementById('periodo-options')?.classList.add('hidden');
   });
   
   function selecionarFiltro(tipo, valor, label) {
     if (tipo === 'status') {
       valorStatus = valor;
       document.getElementById('status-label').textContent = label;
       document.getElementById('status-options').classList.add('hidden');
     } else if (tipo === 'forma') {
       valorForma = valor;
       document.getElementById('forma-label').textContent = label;
       document.getElementById('forma-options').classList.add('hidden');
     } else if (tipo === 'origem') {
       valorOrigem = valor;
       document.getElementById('origem-label').textContent = label;
       document.getElementById('origem-options').classList.add('hidden');
     } else if (tipo === 'periodo') {
       valorPeriodo = valor;
       document.getElementById('periodo-label').textContent = label;
       document.getElementById('periodo-options').classList.add('hidden');
     }
     aplicarFiltros();
   }
   
   function aplicarFiltros() {
     const busca = (document.getElementById('f-busca')?.value || '').toLowerCase().trim();
     const status = valorStatus;
     const forma = valorForma;
     const origem = valorOrigem;
     const periodo = valorPeriodo;
   
     let limite = null;
     if (periodo !== '') {
       limite = new Date();
       limite.setHours(0, 0, 0, 0);
       limite.setDate(limite.getDate() - Number(periodo));
     }
   
     filtradosAtuais = cache.filter(p => {
       if (busca) {
         const alvo = `${p.numeroPedido} ${p.clienteNome} ${p.clienteEmail || ''} ${p.clienteTelefone || ''}`.toLowerCase();
         if (!alvo.includes(busca)) return false;
       }
       if (status === '__PENDENTES' && !PENDENTES.includes(p.status)) return false;
       if (status && status !== '__PENDENTES' && p.status !== status) return false;
       if (forma && p.formaRecebimento !== forma) return false;
       if (origem && p.origem !== origem) return false;
       if (limite && new Date(p.criadoEm) < limite) return false;
       return true;
     });
   
     filtradosAtuais.sort((a, b) => new Date(b.criadoEm) - new Date(a.criadoEm));
   
     renderResumo(filtradosAtuais);
     renderTabela(filtradosAtuais);
   }
   
   function renderResumo(lista) {
     const validos = lista.filter(p => p.status !== 'CANCELADO');
     const faturamento = validos.reduce((s, p) => s + Number(p.total || 0), 0);
     const pendentes = lista.filter(p => PENDENTES.includes(p.status)).length;
     const ticket = validos.length ? faturamento / validos.length : 0;
   
     document.getElementById('resumo-pedidos').innerHTML = `
       <div class="resumo-item"><div class="rotulo">Pedidos</div><div class="valor">${lista.length}</div></div>
       <div class="resumo-item ${pendentes ? 'alerta' : ''}"><div class="rotulo">Pendentes</div><div class="valor">${pendentes}</div></div>
       <div class="resumo-item destaque"><div class="rotulo">Faturamento (sem cancelados)</div><div class="valor">${formatarMoeda(faturamento)}</div></div>
       <div class="resumo-item"><div class="rotulo">Ticket médio</div><div class="valor">${formatarMoeda(ticket)}</div></div>
     `;
   }
   
   function renderTabela(lista) {
     const tbody = document.getElementById('tabela-pedidos');
   
     tbody.innerHTML = lista.length === 0
       ? `<tr><td colspan="9" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
            Nenhum pedido encontrado.
          </td></tr>`
       : lista.map(p => {
           const qtdItens = (p.itens || []).reduce((s, i) => s + (i.quantidade || 0), 0);
           return `
         <tr>
           <td><a href="pedido-detalhe.html?id=${p.id}"><strong>${escapeHtml(p.numeroPedido)}</strong></a></td>
           <td>
             ${escapeHtml(p.clienteNome)}<br>
             <span class="texto-mini">${escapeHtml(p.clienteTelefone || p.clienteEmail || '')}</span>
           </td>
           <td>${formatarData(p.criadoEm)}</td>
           <td>${p.formaRecebimento === 'ENTREGA' ? 'Entrega' : 'Retirada'}</td>
           <td>${p.origem === 'PAINEL'
                 ? `<span class="badge badge-rosa" title="Lançado por ${escapeAttr(p.vendedorNome || '')}">Painel</span>`
                 : '<span class="badge badge-verde">App</span>'}</td>
           <td class="text-center">${qtdItens}</td>
           <td class="text-right"><strong>${formatarMoeda(p.total)}</strong></td>
           <td><span class="badge ${classeStatus(p.status)}">${rotuloStatus(p.status)}</span></td>
           <td>
             <div class="acoes-linha">
               <button class="btn-mini editar" onclick="abrirDetalhe(${p.id})">Detalhar</button>
             </div>
           </td>
         </tr>`;
         }).join('');
   }
   
   function abrirDetalhe(id) {
     window.location.href = `pedido-detalhe.html?id=${id}`;
   }
   
   function exportarCsv() {
     if (filtradosAtuais.length === 0) { alert('Nenhum pedido para exportar.'); return; }
     const cab = ['Numero', 'Data', 'Cliente', 'Email', 'Telefone', 'Forma', 'Origem', 'Vendedor', 'Status', 'Subtotal', 'Desconto', 'Frete', 'Total'];
     const num = v => Number(v || 0).toFixed(2).replace('.', ',');
     const linhas = filtradosAtuais.map(p => [
       p.numeroPedido, formatarData(p.criadoEm), p.clienteNome, p.clienteEmail || '', p.clienteTelefone || '',
       p.formaRecebimento === 'ENTREGA' ? 'Entrega' : 'Retirada', p.origem, p.vendedorNome || '',
       rotuloStatus(p.status), num(p.subtotal), num(p.desconto), num(p.frete), num(p.total),
     ]);
     const csv = [cab, ...linhas]
       .map(l => l.map(c => `"${String(c ?? '').replace(/"/g, '""')}"`).join(';'))
       .join('\r\n');
     const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' });
     const a = document.createElement('a');
     a.href = URL.createObjectURL(blob);
     a.download = `pedidos-${new Date().toISOString().slice(0, 10)}.csv`;
     a.click();
     URL.revokeObjectURL(a.href);
   }
   
   /* ----- Helpers ----- */
   function formatarData(iso) {
     if (!iso) return '—';
     return new Date(iso).toLocaleString('pt-BR', {
       day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
     });
   }
   function escapeHtml(s) {
     return String(s ?? '').replace(/[&<>"']/g, m => ({
       '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
     }[m]));
   }
   function escapeAttr(s) { return escapeHtml(s); }
   
   /* ----- Init ----- */
   document.addEventListener('DOMContentLoaded', () => {
     if (montarLayout('Pedidos', 'pedidos')) carregar();
   });