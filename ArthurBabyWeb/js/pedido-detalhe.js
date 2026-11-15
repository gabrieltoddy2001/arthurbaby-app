/* ============================================
   pedido-detalhe.js — detalhe do pedido
   Fluxo de status, próxima etapa sugerida, cancelamento (com estorno de estoque),
   dados do cliente, itens, totais, histórico e impressão.
   ============================================ */

let pedido = null;

// Etapas exibidas no fluxo; a de "pronto" muda conforme a forma de recebimento
function etapasDoPedido(p) {
  const comum = ['PEDIDO_GERADO', 'EM_ANALISE', 'AGUARDANDO_CONFIRMACAO', 'CONFIRMADO', 'SEPARANDO_PRODUTOS'];
  return p.formaRecebimento === 'ENTREGA'
    ? [...comum, 'EM_TRANSPORTE', 'ENTREGUE']
    : [...comum, 'PRONTO_PARA_RETIRADA', 'ENTREGUE'];
}

function getIdUrl() {
  const id = new URLSearchParams(window.location.search).get('id');
  return id ? Number(id) : null;
}

async function init() {
  const id = getIdUrl();
  if (!id) {
    document.getElementById('conteudo').innerHTML =
      '<div class="alert alert-erro">ID do pedido não informado.</div>';
    return;
  }

  try {
    pedido = await pedidosApi.buscar(id);
    renderDetalhe();
  } catch (err) {
    document.getElementById('conteudo').innerHTML =
      `<div class="alert alert-erro">Erro: ${escapeHtml(err.message)}</div>
       <button class="btn btn-ghost" onclick="voltar()">Voltar para pedidos</button>`;
  }
}

function renderDetalhe() {
  const conteudo = document.getElementById('conteudo');
  const p = pedido;
  const finalizado = p.status === 'CANCELADO' || p.status === 'ENTREGUE';
  const etapas = etapasDoPedido(p);
  const idxAtual = etapas.indexOf(p.status);
  const proxima = !finalizado && idxAtual >= 0 && idxAtual < etapas.length - 1 ? etapas[idxAtual + 1] : null;

  const fluxoHtml = p.status === 'CANCELADO'
    ? `<div class="alert alert-erro" style="margin:0;display:block;">
         <div><strong>Pedido cancelado</strong>${p.canceladoEm ? ' em ' + formatarData(p.canceladoEm) : ''}.</div>
         ${p.motivoCancelamento ? `<div>Motivo: ${escapeHtml(p.motivoCancelamento)}</div>` : ''}
         <div class="texto-mini" style="margin-top:.25rem;">O estoque dos itens foi estornado automaticamente.</div>
       </div>`
    : `<div class="fluxo-status">
         ${etapas.map((s, i) => `
           <div class="fluxo-etapa ${i < idxAtual || p.status === 'ENTREGUE' ? 'feita' : ''} ${i === idxAtual && p.status !== 'ENTREGUE' ? 'atual' : ''}">
             <div class="ponto">${i < idxAtual || p.status === 'ENTREGUE' ? '✓' : i + 1}</div>
             ${rotuloStatus(s)}
           </div>`).join('')}
       </div>`;

  const itensHtml = (p.itens || []).map(it => `
    <tr>
      <td>
        <strong>${escapeHtml(it.produtoNome)}</strong><br>
        <span class="texto-mini">
          ${escapeHtml(it.codigoProduto || '')}${it.variacaoDescricao ? ' · ' + escapeHtml(it.variacaoDescricao) : ''}
        </span>
      </td>
      <td class="text-center">${it.quantidade}</td>
      <td class="text-right">${formatarMoeda(it.valorUnitario)}</td>
      <td class="text-right"><strong>${formatarMoeda(it.valorTotal)}</strong></td>
    </tr>
  `).join('') || `<tr><td colspan="4" class="text-center" style="padding:2rem;color:var(--texto-secundario)">Nenhum item.</td></tr>`;

  const historicoHtml = [...(p.historico || [])].reverse().map((h, i) => `
    <div class="timeline-item ${i === 0 ? 'atual' : ''}">
      <div class="timeline-ponto"></div>
      <div class="timeline-conteudo">
        <div class="timeline-status">
          <span class="badge ${classeStatus(h.status)}">${rotuloStatus(h.status)}</span>
        </div>
        <div class="timeline-info">
          <strong>${escapeHtml(h.usuario || 'Sistema')}</strong> • ${formatarData(h.data)}
        </div>
        ${h.observacao ? `<div class="timeline-obs">${escapeHtml(h.observacao)}</div>` : ''}
      </div>
    </div>
  `).join('') || '<p class="texto-sec">Sem histórico.</p>';

  const e = p.enderecoEntrega;
  const enderecoHtml = e
    ? `${escapeHtml(e.logradouro)}, ${escapeHtml(e.numero)}${e.complemento ? ' — ' + escapeHtml(e.complemento) : ''}<br>
       ${escapeHtml(e.bairro)} — ${escapeHtml(e.cidade)}/${escapeHtml(e.uf)}<br>
       CEP: ${escapeHtml(e.cep)}${e.referencia ? '<br>Ref.: ' + escapeHtml(e.referencia) : ''}`
    : '<span class="texto-sec">Cliente sem endereço cadastrado.</span>';

  const telefoneLimpo = (p.clienteTelefone || '').replace(/\D/g, '');
  const whatsapp = telefoneLimpo
    ? `https://wa.me/55${telefoneLimpo.replace(/^55/, '')}?text=${encodeURIComponent(
        `Olá ${p.clienteNome?.split(' ')[0] || ''}! Sobre o seu pedido ${p.numeroPedido} na Lojão Arthur Baby: status atual "${rotuloStatus(p.status)}".`)}`
    : null;

  const opcoesStatus = Object.keys(STATUS_PEDIDO)
    .filter(s => s !== 'CANCELADO' && s !== p.status)
    .map(s => `<option value="${s}" ${s === proxima ? 'selected' : ''}>${rotuloStatus(s)}</option>`)
    .join('');

  conteudo.innerHTML = `
    <div class="form-produto">
      <div class="form-header">
        <div>
          <h1>Pedido ${escapeHtml(p.numeroPedido)}</h1>
          <p class="texto-sec">
            Criado em ${formatarData(p.criadoEm)} ·
            ${p.origem === 'PAINEL' ? `lançado no painel por <strong>${escapeHtml(p.vendedorNome || '—')}</strong>` : 'feito pelo app'}
          </p>
        </div>
        <div class="no-print">
          <button class="btn btn-ghost" onclick="voltar()">Voltar</button>
          <button class="btn btn-ghost" onclick="window.print()">Imprimir</button>
          ${!finalizado ? `<button class="btn btn-danger" onclick="cancelar()">Cancelar pedido</button>` : ''}
        </div>
      </div>

      <div id="alerta-pedido" class="alert alert-erro hidden"></div>

      <div class="card">
        <div class="secao-header">
          <h3 class="secao-titulo">Status do pedido</h3>
          <span class="badge ${classeStatus(p.status)}" style="font-size:.85rem;padding:.35rem .8rem;">${rotuloStatus(p.status)}</span>
        </div>
        ${fluxoHtml}
        ${!finalizado ? `
          <div class="grid-2 no-print" style="align-items:end;">
            <div class="field" style="margin-bottom:0;">
              <label for="f-novo-status">Alterar para</label>
              <select id="f-novo-status">${opcoesStatus}</select>
            </div>
            <div class="field" style="margin-bottom:0;">
              <label for="f-obs-status">Observação (opcional, vai para o histórico)</label>
              <input type="text" id="f-obs-status" maxlength="250" placeholder="Ex.: Pagamento confirmado via PIX">
            </div>
          </div>
          <div class="no-print" style="display:flex;gap:.5rem;margin-top:.75rem;flex-wrap:wrap;">
            <button class="btn btn-primary" id="btn-status" onclick="mudarStatus()">Atualizar status</button>
            ${proxima ? `<button class="btn btn-accent" onclick="avancar('${proxima}')">Avançar para "${rotuloStatus(proxima)}"</button>` : ''}
          </div>` : ''}
      </div>

      <div class="grid-detalhe">
        <div>
          <div class="card">
            <h3 class="secao-titulo">Itens do pedido</h3>
            <div class="tabela-rolavel">
              <table class="tabela tabela-mini">
                <thead>
                  <tr>
                    <th>Produto</th>
                    <th class="text-center">Qtd</th>
                    <th class="text-right">Valor unit.</th>
                    <th class="text-right">Total</th>
                  </tr>
                </thead>
                <tbody>${itensHtml}</tbody>
              </table>
            </div>

            <div class="totais">
              <div><span>Subtotal:</span> <strong>${formatarMoeda(p.subtotal)}</strong></div>
              ${Number(p.desconto) ? `<div><span>Desconto${p.cupom ? ` (cupom ${escapeHtml(p.cupom)})` : ''}:</span> <strong>- ${formatarMoeda(p.desconto)}</strong></div>` : ''}
              <div><span>Frete:</span> <strong>${Number(p.frete) ? formatarMoeda(p.frete) : 'Grátis'}</strong></div>
              <div class="total-final"><span>Total:</span> <strong>${formatarMoeda(p.total)}</strong></div>
            </div>
          </div>

          <div class="card">
            <h3 class="secao-titulo">Histórico de status</h3>
            <div class="timeline">${historicoHtml}</div>
          </div>
        </div>

        <div>
          <div class="card">
            <h3 class="secao-titulo">Cliente</h3>
            <dl class="info-lista">
              <dt>Nome</dt><dd><strong>${escapeHtml(p.clienteNome)}</strong></dd>
              <dt>E-mail</dt><dd>${p.clienteEmail ? `<a href="mailto:${escapeAttr(p.clienteEmail)}">${escapeHtml(p.clienteEmail)}</a>` : '—'}</dd>
              <dt>Telefone</dt><dd>${escapeHtml(p.clienteTelefone || '—')}</dd>
              ${p.clienteCpf ? `<dt>CPF</dt><dd>${escapeHtml(p.clienteCpf)}</dd>` : ''}
            </dl>
            <div class="no-print" style="display:flex;gap:.5rem;margin-top:.9rem;flex-wrap:wrap;">
              ${whatsapp ? `<a class="btn btn-accent btn-sm" href="${whatsapp}" target="_blank" rel="noopener">WhatsApp</a>` : ''}
              <a class="btn btn-ghost btn-sm" href="clientes.html">Ver clientes</a>
            </div>
          </div>

          <div class="card">
            <h3 class="secao-titulo">Recebimento</h3>
            <p><strong>${p.formaRecebimento === 'ENTREGA' ? 'Entrega no endereço' : 'Retirada na loja'}</strong></p>
            ${p.formaRecebimento === 'ENTREGA' ? `
              <p style="margin-top:.5rem;font-size:.9rem;">${enderecoHtml}</p>
              <p class="texto-mini" style="margin-top:.4rem;">Endereço principal do cadastro do cliente.</p>` : ''}
            ${p.observacao ? `<p style="margin-top:.75rem;"><strong>Observação:</strong> ${escapeHtml(p.observacao)}</p>` : ''}
          </div>
        </div>
      </div>
    </div>
  `;
}

/* ============================================
   MUDAR STATUS
   ============================================ */
async function enviarStatus(status, observacao, motivoCancelamento, msgSucesso) {
  const btn = document.getElementById('btn-status');
  if (btn) { btn.disabled = true; btn.textContent = 'Salvando...'; }
  try {
    pedido = await pedidosApi.alterarStatus(pedido.id, { status, observacao: observacao || null, motivoCancelamento });
    renderDetalhe();
    mostrarSucesso(msgSucesso);
  } catch (err) {
    mostrarErro(err.message);
    if (btn) { btn.disabled = false; btn.textContent = 'Atualizar status'; }
  }
}

function mudarStatus() {
  const novo = document.getElementById('f-novo-status').value;
  const obs = document.getElementById('f-obs-status').value.trim();
  const etapas = etapasDoPedido(pedido);
  if (etapas.indexOf(novo) >= 0 && etapas.indexOf(novo) < etapas.indexOf(pedido.status)) {
    if (!confirm(`O status "${rotuloStatus(novo)}" é anterior ao atual. Deseja voltar o pedido mesmo assim?`)) return;
  }
  if (novo === 'ENTREGUE' && !confirm('Marcar como ENTREGUE finaliza o pedido (não poderá mais ser alterado). Confirmar?')) return;
  enviarStatus(novo, obs, null, `Status alterado para "${rotuloStatus(novo)}".`);
}

function avancar(status) {
  if (status === 'ENTREGUE' && !confirm('Marcar como ENTREGUE finaliza o pedido (não poderá mais ser alterado). Confirmar?')) return;
  const obs = document.getElementById('f-obs-status')?.value.trim();
  enviarStatus(status, obs, null, `Status alterado para "${rotuloStatus(status)}".`);
}

/* ============================================
   CANCELAR PEDIDO (estorna o estoque no servidor)
   ============================================ */
function cancelar() {
  const motivo = prompt('Motivo do cancelamento (obrigatório).\nO estoque dos itens será devolvido automaticamente.');
  if (motivo === null) return;
  if (!motivo.trim()) { alert('É necessário informar o motivo.'); return; }
  enviarStatus('CANCELADO', null, motivo.trim(), 'Pedido cancelado e estoque estornado.');
}

function mostrarSucesso(msg) {
  const alerta = document.getElementById('alerta-pedido');
  alerta.className = 'alert alert-sucesso';
  alerta.textContent = msg;
  window.scrollTo({ top: 0, behavior: 'smooth' });
  setTimeout(() => alerta.classList.add('hidden'), 3500);
}

function mostrarErro(msg) {
  const alerta = document.getElementById('alerta-pedido');
  alerta.className = 'alert alert-erro';
  alerta.textContent = msg;
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function voltar() {
  window.location.href = 'pedidos.html';
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
  if (montarLayout('Detalhe do Pedido', 'pedidos')) init();
});
