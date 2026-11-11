/* ============================================
   pedido-novo.js — lançamento de pedido pelo painel (balcão / WhatsApp)
   Todo pedido é vinculado a um cliente (perfil CLIENTE, ativo).
   Preços, frete, desconto e baixa de estoque são calculados no servidor;
   aqui mostramos uma prévia com as mesmas regras.
   ============================================ */

const FRETE_ENTREGA = 15;
const FRETE_GRATIS_A_PARTIR = 200;

let clientes = [];
let produtos = [];
let clienteSel = null;
let itens = [];          // { produtoId, variacaoId, nome, variacaoDescricao, sku, preco, quantidade, estoque }
let cupomAplicado = null; // resposta de /cupons/validar

async function init() {
  const conteudo = document.getElementById('conteudo');
  conteudo.innerHTML = '<div class="loading">Carregando clientes e produtos...</div>';
  try {
    const [usuarios, prods] = await Promise.all([
      adminApi.listar('usuarios'),
      adminApi.listar('produtos'),
    ]);
    clientes = usuarios
      .filter(u => u.perfil === 'CLIENTE' && u.status === 'ATIVO')
      .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
    produtos = prods
      .filter(p => p.status === 'ATIVO')
      .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));

    render();

    const idParam = Number(new URLSearchParams(window.location.search).get('clienteId'));
    if (idParam) selecionarCliente(idParam);
  } catch (err) {
    conteudo.innerHTML = `<div class="alert alert-erro">Erro ao carregar: ${escapeHtml(err.message)}</div>`;
  }
}

function render() {
  document.getElementById('conteudo').innerHTML = `
    <div class="form-produto">
      <div class="form-header">
        <div>
          <p class="texto-sec">O pedido fica vinculado ao cliente e você fica registrado como vendedor.</p>
        </div>
        <div>
          <button class="btn btn-ghost" onclick="window.location.href='pedidos.html'">Cancelar</button>
          <button class="btn btn-primary" id="btn-finalizar" onclick="finalizar()">Finalizar pedido</button>
        </div>
      </div>

      <div id="alerta" class="alert alert-erro hidden"></div>

      <div class="grid-detalhe">
        <div>
          <div class="card card-sobreposto">
            <h3 class="secao-titulo">1. Cliente *</h3>
            <div id="area-cliente"></div>
          </div>

          <div class="card">
            <h3 class="secao-titulo">2. Itens *</h3>
            <div class="linha-add-item">
              <div class="field" style="margin-bottom:0;">
                <label for="sel-produto">Produto</label>
                <select id="sel-produto" onchange="aoTrocarProduto()">
                  <option value="">Selecione...</option>
                  ${produtos.map(p => `<option value="${p.id}">${escapeHtml(p.nome)} — ${escapeHtml(p.codigo)}</option>`).join('')}
                </select>
              </div>
              <div class="field" style="margin-bottom:0;">
                <label for="sel-variacao">Variação</label>
                <select id="sel-variacao" disabled><option value="">—</option></select>
              </div>
              <div class="field" style="margin-bottom:0;">
                <label for="inp-qtd">Qtd</label>
                <input type="number" id="inp-qtd" min="1" value="1">
              </div>
              <button type="button" class="btn btn-accent" onclick="adicionarItem()">Adicionar</button>
            </div>
            <p id="info-produto" class="dica"></p>
            <div id="lista-itens" style="margin-top:1rem;"></div>
          </div>
        </div>

        <div>
          <div class="card">
            <h3 class="secao-titulo">3. Recebimento</h3>
            <div class="field">
              <label for="f-forma">Forma</label>
              <select id="f-forma" onchange="renderResumo()">
                <option value="RETIRADA_LOJA">Retirada na loja</option>
                <option value="ENTREGA">Entrega</option>
              </select>
            </div>
            <div id="aviso-endereco" class="dica"></div>
            <div class="field" style="margin-top:.75rem;">
              <label for="f-cupom">Cupom</label>
              <div style="display:flex;gap:.5rem;">
                <input type="text" id="f-cupom" placeholder="Ex.: ARTHUR10" style="flex:1;text-transform:uppercase;"
                       oninput="cupomAplicado=null; renderResumo();">
                <button type="button" class="btn btn-ghost btn-sm" onclick="aplicarCupom()">Aplicar</button>
              </div>
              <div id="info-cupom" class="dica"></div>
            </div>
            <div class="field">
              <label for="f-obs">Observação</label>
              <textarea id="f-obs" rows="2" maxlength="500" placeholder="Ex.: cliente retira sábado pela manhã"></textarea>
            </div>
          </div>

          <div class="card">
            <h3 class="secao-titulo">Resumo</h3>
            <div id="resumo"></div>
            <p class="texto-mini" style="margin-top:.6rem;">
              Valores conferidos pelo servidor ao finalizar. Frete de entrega: ${formatarMoeda(FRETE_ENTREGA)},
              grátis a partir de ${formatarMoeda(FRETE_GRATIS_A_PARTIR)}.
            </p>
          </div>
        </div>
      </div>
    </div>
  `;
  renderCliente();
  renderItens();
  renderResumo();
}

/* ============================================
   CLIENTE
   ============================================ */
function renderCliente() {
  const area = document.getElementById('area-cliente');
  if (clienteSel) {
    const c = clienteSel;
    area.innerHTML = `
      <div class="cliente-selecionado">
        <div>
          <strong>${escapeHtml(c.nome)}</strong><br>
          <span class="texto-mini">${escapeHtml(c.email)}${c.telefone ? ' · ' + escapeHtml(c.telefone) : ''}${c.cpf ? ' · CPF ' + escapeHtml(c.cpf) : ''}</span>
        </div>
        <button type="button" class="btn btn-ghost btn-sm" onclick="trocarCliente()">Trocar</button>
      </div>`;
  } else {
    area.innerHTML = `
      <div class="busca-cliente">
        <input type="text" id="busca-cliente" placeholder="Digite nome, e-mail, telefone ou CPF..." autocomplete="off"
               oninput="sugerirClientes()" onfocus="sugerirClientes()"
               style="width:100%;padding:.6rem .8rem;border:1px solid var(--borda);border-radius:8px;">
        <div id="sugestoes" class="sugestoes hidden"></div>
      </div>
      <p class="dica">${clientes.length} cliente(s) ativo(s). Para um cliente novo, peça o cadastro pelo app.</p>`;
  }
  renderResumo();
}

function sugerirClientes() {
  const termo = (document.getElementById('busca-cliente')?.value || '').toLowerCase().trim();
  const box = document.getElementById('sugestoes');
  const soDigitos = termo.replace(/\D/g, '');
  const lista = clientes.filter(c => {
    if (!termo) return true;
    const alvo = `${c.nome} ${c.email}`.toLowerCase();
    const nums = `${c.telefone || ''} ${c.cpf || ''}`.replace(/\D/g, '');
    return alvo.includes(termo) || (soDigitos.length >= 3 && nums.includes(soDigitos));
  }).slice(0, 8);

  box.innerHTML = lista.length
    ? lista.map(c => `<button type="button" onclick="selecionarCliente(${c.id})">
        <strong>${escapeHtml(c.nome)}</strong><br><span class="texto-mini">${escapeHtml(c.email)}${c.telefone ? ' · ' + escapeHtml(c.telefone) : ''}</span>
      </button>`).join('')
    : '<div style="padding:.75rem;" class="texto-sec">Nenhum cliente ativo encontrado.</div>';
  box.classList.remove('hidden');
}

function selecionarCliente(id) {
  const c = clientes.find(x => x.id === id);
  if (!c) {
    mostrarErro('Cliente não encontrado ou inativo/bloqueado.');
    return;
  }
  clienteSel = c;
  renderCliente();
}

function trocarCliente() {
  clienteSel = null;
  renderCliente();
  document.getElementById('busca-cliente')?.focus();
}

document.addEventListener('click', (e) => {
  if (!e.target.closest('.busca-cliente')) document.getElementById('sugestoes')?.classList.add('hidden');
});

/* ============================================
   ITENS
   ============================================ */
function precoBase(p) {
  return p.precoPromocional != null && Number(p.precoPromocional) > 0 ? Number(p.precoPromocional) : Number(p.preco);
}

function descricaoVariacao(v) {
  return [v.tamanho?.nome, v.cor?.nome, v.modelo?.nome].filter(Boolean).join(' / ') || v.sku;
}

function aoTrocarProduto() {
  const p = produtos.find(x => x.id === Number(document.getElementById('sel-produto').value));
  const sel = document.getElementById('sel-variacao');
  const info = document.getElementById('info-produto');
  if (!p) {
    sel.innerHTML = '<option value="">—</option>';
    sel.disabled = true;
    info.textContent = '';
    return;
  }
  const vars = (p.variacoes || []).filter(v => v.status !== 'INATIVA');
  if (vars.length === 0) {
    sel.innerHTML = '<option value="">Sem variação cadastrada</option>';
    sel.disabled = true;
    info.innerHTML = `<span style="color:var(--erro);">${escapeHtml(MensagensLoja.produtoSemVariacao(p.nome))}</span>`;
    return;
  }
  sel.disabled = false;
  sel.innerHTML = '<option value="">Selecione...</option>' + vars.map(v => {
    const preco = v.preco != null ? Number(v.preco) : precoBase(p);
    const est = v.estoqueAtual || 0;
    return `<option value="${v.id}" ${est <= 0 ? 'disabled' : ''}>
      ${escapeHtml(descricaoVariacao(v))} — ${formatarMoeda(preco)} — ${est > 0 ? est + ' em estoque' : 'sem estoque'}
    </option>`;
  }).join('');
  info.textContent = `${vars.length} variação(ões). Variações sem estoque aparecem desabilitadas.`;
}

function adicionarItem() {
  limparErro();
  const p = produtos.find(x => x.id === Number(document.getElementById('sel-produto').value));
  const qtd = parseInt(document.getElementById('inp-qtd').value, 10);
  if (!p) return mostrarErro('Selecione um produto.');
  if (!qtd || qtd < 1) return mostrarErro('Informe uma quantidade válida.');

  // Mesma regra do servidor: todo item precisa de uma variação, pois o estoque é controlado nela
  const vars = (p.variacoes || []).filter(v => v.status !== 'INATIVA');
  if (!vars.length) return mostrarErro(MensagensLoja.produtoSemVariacao(p.nome));
  const v = vars.find(x => x.id === Number(document.getElementById('sel-variacao').value));
  if (!v) return mostrarErro(MensagensLoja.selecioneVariacao(p.nome));

  const existente = itens.find(i => i.produtoId === p.id && i.variacaoId === v.id);
  const qtdTotal = qtd + (existente?.quantidade || 0);
  if (qtdTotal > (v.estoqueAtual || 0)) {
    return mostrarErro(MensagensLoja.estoqueInsuficiente(p.nome, descricaoVariacao(v), v.estoqueAtual || 0, qtdTotal));
  }

  if (existente) {
    existente.quantidade = qtdTotal;
  } else {
    itens.push({
      produtoId: p.id,
      variacaoId: v?.id ?? null,
      nome: p.nome,
      codigo: p.codigo,
      variacaoDescricao: v ? descricaoVariacao(v) : '',
      sku: v?.sku || p.sku,
      preco: v?.preco != null ? Number(v.preco) : precoBase(p),
      quantidade: qtd,
      estoque: v ? (v.estoqueAtual || 0) : null,
    });
  }
  document.getElementById('inp-qtd').value = 1;
  cupomAplicado = null;
  renderItens();
}

function alterarQtd(i, valor) {
  const qtd = parseInt(valor, 10);
  const it = itens[i];
  if (!qtd || qtd < 1) { renderItens(); return; }
  if (it.estoque != null && qtd > it.estoque) {
    mostrarErro(MensagensLoja.estoqueInsuficiente(it.nome, it.variacaoDescricao, it.estoque, qtd));
    renderItens();
    return;
  }
  it.quantidade = qtd;
  cupomAplicado = null;
  renderItens();
}

function removerItem(i) {
  itens.splice(i, 1);
  cupomAplicado = null;
  renderItens();
}

function renderItens() {
  const div = document.getElementById('lista-itens');
  if (!itens.length) {
    div.innerHTML = '<p class="texto-sec">Nenhum item adicionado.</p>';
  } else {
    div.innerHTML = `
      <div class="tabela-rolavel">
        <table class="tabela tabela-mini">
          <thead><tr><th>Produto</th><th class="text-center">Qtd</th><th class="text-right">Unit.</th><th class="text-right">Total</th><th></th></tr></thead>
          <tbody>
            ${itens.map((it, i) => `
              <tr>
                <td><strong>${escapeHtml(it.nome)}</strong><br>
                  <span class="texto-mini">${escapeHtml(it.sku || '')}${it.variacaoDescricao ? ' · ' + escapeHtml(it.variacaoDescricao) : ''}</span></td>
                <td class="text-center"><input type="number" class="qtd-input" min="1" ${it.estoque != null ? `max="${it.estoque}"` : ''}
                       value="${it.quantidade}" onchange="alterarQtd(${i}, this.value)"></td>
                <td class="text-right">${formatarMoeda(it.preco)}</td>
                <td class="text-right"><strong>${formatarMoeda(it.preco * it.quantidade)}</strong></td>
                <td><button type="button" class="btn-mini inativar" onclick="removerItem(${i})" title="Remover">×</button></td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  }
  renderResumo();
}

/* ============================================
   CUPOM E RESUMO
   ============================================ */
function subtotal() {
  return itens.reduce((s, it) => s + it.preco * it.quantidade, 0);
}

async function aplicarCupom() {
  const codigo = document.getElementById('f-cupom').value.trim();
  const info = document.getElementById('info-cupom');
  cupomAplicado = null;
  if (!codigo) { info.textContent = ''; renderResumo(); return; }
  if (!itens.length) { info.textContent = 'Adicione itens antes de aplicar o cupom.'; return; }
  try {
    const r = await validarCupom(codigo, Number(subtotal().toFixed(2)));
    if (r.valido) {
      cupomAplicado = r;
      info.innerHTML = `<span style="color:var(--sucesso);">Cupom aplicado: ${escapeHtml(r.descricao || r.codigo)}</span>`;
    } else {
      info.innerHTML = `<span style="color:var(--erro);">${escapeHtml(r.motivo || 'Cupom inválido')}</span>`;
    }
  } catch (err) {
    info.innerHTML = `<span style="color:var(--erro);">${escapeHtml(err.message)}</span>`;
  }
  renderResumo();
}

function renderResumo() {
  const div = document.getElementById('resumo');
  if (!div) return;
  const forma = document.getElementById('f-forma')?.value;
  const sub = subtotal();
  const desconto = cupomAplicado ? Number(cupomAplicado.desconto || 0) : 0;
  let frete = forma === 'ENTREGA' && sub < FRETE_GRATIS_A_PARTIR ? FRETE_ENTREGA : 0;
  if (cupomAplicado?.freteGratis) frete = 0;
  const total = Math.max(0, sub - desconto + frete);
  const qtd = itens.reduce((s, i) => s + i.quantidade, 0);

  const aviso = document.getElementById('aviso-endereco');
  if (aviso) {
    if (forma === 'ENTREGA' && clienteSel) {
      const e = (clienteSel.enderecos || []).find(x => x.principal) || (clienteSel.enderecos || [])[0];
      aviso.innerHTML = e
        ? `Entrega em: ${escapeHtml(e.logradouro)}, ${escapeHtml(e.numero)} — ${escapeHtml(e.bairro)}, ${escapeHtml(e.cidade)}/${escapeHtml(e.uf)}`
        : '<span style="color:var(--erro);">Cliente sem endereço cadastrado. Use retirada ou peça o cadastro do endereço no app.</span>';
    } else {
      aviso.textContent = '';
    }
  }

  div.innerHTML = `
    <dl class="info-lista">
      <dt>Cliente</dt><dd>${clienteSel ? escapeHtml(clienteSel.nome) : '<span style="color:var(--erro);">não selecionado</span>'}</dd>
      <dt>Itens</dt><dd>${qtd} un.</dd>
      <dt>Subtotal</dt><dd>${formatarMoeda(sub)}</dd>
      ${desconto ? `<dt>Desconto</dt><dd>- ${formatarMoeda(desconto)}</dd>` : ''}
      <dt>Frete</dt><dd>${frete ? formatarMoeda(frete) : 'Grátis'}</dd>
      <dt><strong>Total</strong></dt><dd><strong style="font-size:1.15rem;color:var(--rosa-escuro);">${formatarMoeda(total)}</strong></dd>
    </dl>`;
}

/* ============================================
   FINALIZAR
   ============================================ */
async function finalizar() {
  limparErro();
  if (!clienteSel) return mostrarErro('Selecione o cliente do pedido.');
  if (!itens.length) return mostrarErro('Adicione pelo menos um item.');
  const forma = document.getElementById('f-forma').value;
  if (forma === 'ENTREGA' && !(clienteSel.enderecos || []).length) {
    return mostrarErro('O cliente não tem endereço cadastrado para entrega. Selecione "Retirada na loja".');
  }
  const cupom = document.getElementById('f-cupom').value.trim();
  if (cupom && !cupomAplicado) return mostrarErro('Clique em "Aplicar" para validar o cupom antes de finalizar (ou apague o código).');

  if (!confirm(`Confirmar pedido para ${clienteSel.nome}?\nO estoque será baixado.`)) return;

  const btn = document.getElementById('btn-finalizar');
  btn.disabled = true;
  btn.textContent = 'Enviando...';
  try {
    const criado = await pedidosApi.criar({
      clienteId: clienteSel.id,
      formaRecebimento: forma,
      cupom: cupom || null,
      observacao: document.getElementById('f-obs').value.trim() || null,
      itens: itens.map(i => ({ produtoId: i.produtoId, variacaoId: i.variacaoId, quantidade: i.quantidade })),
    });
    window.location.href = `pedido-detalhe.html?id=${criado.id}`;
  } catch (err) {
    mostrarErro(err.message);
    btn.disabled = false;
    btn.textContent = 'Finalizar pedido';
  }
}

function mostrarErro(msg) {
  const a = document.getElementById('alerta');
  a.textContent = msg;
  a.classList.remove('hidden');
  window.scrollTo({ top: 0, behavior: 'smooth' });
}
function limparErro() { document.getElementById('alerta')?.classList.add('hidden'); }

function escapeHtml(s) {
  return String(s ?? '').replace(/[&<>"']/g, m => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[m]));
}

document.addEventListener('DOMContentLoaded', () => {
  if (montarLayout('Novo pedido', 'pedido-novo')) init();
});
