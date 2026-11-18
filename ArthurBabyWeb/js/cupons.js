// ============================================================
// Configuração da API
// Ajuste se o seu backend rodar em uma URL diferente
// ============================================================
const API_BASE = '/api/admin/cupons';

// ============================================================
// Estado da Aplicação
// ============================================================
let cupons = [];
let editandoId = null;

// Elementos do DOM
const tbody = document.getElementById('tbodyCupons');
const modal = document.getElementById('modalCupom');
const form = document.getElementById('formCupom');
const modalTitulo = document.getElementById('modalTitulo');
const alerta = document.getElementById('alerta-cupom');

// Elementos do Formulário
const campos = {
  id: document.getElementById('cupomId'),
  codigo: document.getElementById('codigo'),
  descricao: document.getElementById('descricao'),
  tipo: document.getElementById('tipo'),
  valor: document.getElementById('valor'),
  validoDe: document.getElementById('validoDe'),
  validoAte: document.getElementById('validoAte'),
  valorMinimo: document.getElementById('valorMinimo'),
  valorMaximoDesconto: document.getElementById('valorMaximoDesconto'),
  ativo: document.getElementById('ativo')
};

// ============================================================
// Funções Utilitárias
// ============================================================
const fmtMoeda = (v) => v ? Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) : '-';
const fmtData = (d) => d ? new Date(d).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '-';

// Retorna a classe CSS do badge de acordo com o tipo
const getBadgeTipo = (tipo) => {
  switch (tipo) {
    case 'PERCENTUAL': return 'badge badge-percentual';
    case 'VALOR_FIXO': return 'badge badge-valor-fixo';
    case 'FRETE_GRATIS': return 'badge badge-frete';
    default: return 'badge';
  }
};

// Formata o valor de exibição na tabela
const fmtValorTabela = (c) => {
  if (c.tipo === 'PERCENTUAL') return `${c.valor}%`;
  if (c.tipo === 'FRETE_GRATIS') return '-';
  return fmtMoeda(c.valor);
};

// Mostra mensagens de alerta
function mostrarAlerta(msg, tipo = 'erro') {
  alerta.textContent = msg;
  alerta.className = `alert alert-${tipo}`;
  alerta.style.display = 'flex';
  setTimeout(() => { alerta.style.display = 'none'; }, 5000);
}

// ============================================================
// Carregar e Renderizar Tabela
// ============================================================
async function carregarCupons() {
  try {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center loading">Carregando...</td></tr>';
    const res = await fetch(API_BASE);
    if (!res.ok) throw new Error('Falha ao buscar os dados');
    
    cupons = await res.json();
    renderTabela();
  } catch (error) {
    console.error(error);
    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-erro">Erro ao carregar cupons.</td></tr>';
    mostrarAlerta('Não foi possível carregar os cupons. Verifique a conexão.');
  }
}

function renderTabela() {
  tbody.innerHTML = '';

  if (cupons.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center texto-sec">Nenhum cupom cadastrado.</td></tr>';
    return;
  }

  cupons.forEach(c => {
    const tr = document.createElement('tr');
    
    // Verifica status
    const badgeStatus = c.ativo ? 'badge-ativo' : 'badge-inativo';
    const txtStatus = c.ativo ? 'Ativo' : 'Inativo';

    tr.innerHTML = `
      <td><span class="cupom-codigo">${c.codigo}</span></td>
      <td>${c.descricao || '-'}</td>
      <td><span class="${getBadgeTipo(c.tipo)}">${c.tipo.replace('_', ' ')}</span></td>
      <td><strong>${fmtValorTabela(c)}</strong></td>
      <td>
        <div style="font-size: .8rem; line-height: 1.2;">
          <span class="texto-sec">De:</span> ${fmtData(c.validoDe)}<br>
          <span class="texto-sec">Até:</span> ${fmtData(c.validoAte)}
        </div>
      </td>
      <td><span class="badge ${badgeStatus}">${txtStatus}</span></td>
      <td>
        <div class="acoes-linha">
          <button class="btn-mini editar" onclick="abrirModalEdicao(${c.id})">Editar</button>
          <button class="btn-mini ${c.ativo ? 'inativar' : 'ativar'}" onclick="alternarStatus(${c.id}, ${c.ativo})">
            ${c.ativo ? 'Inativar' : 'Ativar'}
          </button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// ============================================================
// Modal e Formulário
// ============================================================
function abrirModalNovo() {
  editandoId = null;
  form.reset();
  campos.id.value = '';
  campos.ativo.checked = true;
  modalTitulo.textContent = 'Novo Cupom';
  
  // Configura datas padrão (hoje e daqui a 30 dias)
  const hoje = new Date();
  const daqui30Dias = new Date();
  daqui30Dias.setDate(hoje.getDate() + 30);
  
  campos.validoDe.value = hoje.toISOString().slice(0, 16);
  campos.validoAte.value = daqui30Dias.toISOString().slice(0, 16);

  modal.classList.remove('hidden');
}

function abrirModalEdicao(id) {
  const cupom = cupons.find(c => c.id === id);
  if (!cupom) return;

  editandoId = id;
  modalTitulo.textContent = 'Editar Cupom';

  campos.id.value = cupom.id;
  campos.codigo.value = cupom.codigo;
  campos.descricao.value = cupom.descricao || '';
  campos.tipo.value = cupom.tipo;
  campos.valor.value = cupom.valor || '';
  campos.ativo.checked = cupom.ativo;
  campos.valorMinimo.value = cupom.valorMinimo || '';
  campos.valorMaximoDesconto.value = cupom.valorMaximoDesconto || '';

  // Formata datas para o input datetime-local
  campos.validoDe.value = cupom.validoDe ? cupom.validoDe.slice(0, 16) : '';
  campos.validoAte.value = cupom.validoAte ? cupom.validoAte.slice(0, 16) : '';

  modal.classList.remove('hidden');
}

function fecharModal() {
  modal.classList.add('hidden');
}

// Salvar (Criar ou Atualizar)
form.addEventListener('submit', async (e) => {
  e.preventDefault();

  // Monta o objeto
  const dados = {
    codigo: campos.codigo.value.trim().toUpperCase(),
    descricao: campos.descricao.value.trim(),
    tipo: campos.tipo.value,
    valor: campos.tipo.value === 'FRETE_GRATIS' ? null : parseFloat(campos.valor.value),
    validoDe: campos.validoDe.value ? campos.validoDe.value + ':00' : null,
    validoAte: campos.validoAte.value ? campos.validoAte.value + ':00' : null,
    valorMinimo: campos.valorMinimo.value ? parseFloat(campos.valorMinimo.value) : null,
    valorMaximoDesconto: campos.valorMaximoDesconto.value ? parseFloat(campos.valorMaximoDesconto.value) : null,
    ativo: campos.ativo.checked
  };

  const method = editandoId ? 'PUT' : 'POST';
  const url = editandoId ? `${API_BASE}/${editandoId}` : API_BASE;

  try {
    const btnSalvar = form.querySelector('button[type="submit"]');
    btnSalvar.disabled = true;
    btnSalvar.textContent = 'Salvando...';

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dados)
    });

    if (!res.ok) {
      const errorData = await res.text();
      throw new Error(errorData || 'Erro ao salvar');
    }

    fecharModal();
    mostrarAlerta(editandoId ? 'Cupom atualizado com sucesso!' : 'Cupom criado com sucesso!', 'sucesso');
    carregarCupons();

  } catch (error) {
    console.error(error);
    mostrarAlerta(`Erro ao salvar: ${error.message}`, 'erro');
  } finally {
    const btnSalvar = form.querySelector('button[type="submit"]');
    btnSalvar.disabled = false;
    btnSalvar.textContent = 'Salvar';
  }
});

// Alternar Status (Inativar/Ativar)
async function alternarStatus(id, statusAtual) {
  const acao = statusAtual ? 'inativar' : 'ativar';
  if (!confirm(`Deseja realmente ${acao} este cupom?`)) return;

  try {
    // Como o seu backend usa o AdminCrudService, o padrão é PUT para atualizar
    const cupom = cupons.find(c => c.id === id);
    cupom.ativo = !statusAtual;

    const res = await fetch(`${API_BASE}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(cupom)
    });

    if (!res.ok) throw new Error('Falha ao alterar status');
    
    mostrarAlerta(`Cupom ${acao === 'ativar' ? 'ativado' : 'inativado'} com sucesso!`, 'sucesso');
    carregarCupons();
  } catch (error) {
    console.error(error);
    mostrarAlerta(`Erro ao alterar status: ${error.message}`, 'erro');
  }
}

// ============================================================
// Eventos de Inicialização
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('btnNovoCupom').addEventListener('click', abrirModalNovo);
  document.getElementById('btnCancelar').addEventListener('click', fecharModal);
  document.getElementById('btnFecharModal').addEventListener('click', fecharModal);
  
  // Fecha o modal se clicar fora dele
  modal.addEventListener('click', (e) => {
    if (e.target === modal) fecharModal();
  });

  carregarCupons();
});