/* ============================================
   api.js — camada de dados (INTEGRADA com o backend)
   Toda chamada HTTP do painel passa por aqui.
   ============================================ */

const MOCK = false;
const API_BASE = 'http://localhost:8080/api';

/* ============================================
   SESSÃO
   ============================================ */
function salvarSessao(auth) {
  localStorage.setItem('ab_token', auth.token);
  localStorage.setItem('ab_usuario', JSON.stringify({
    id: auth.usuarioId,
    nome: auth.nome,
    perfil: auth.perfil,
  }));
}
function getToken() { return localStorage.getItem('ab_token'); }
function getUsuario() {
  const raw = localStorage.getItem('ab_usuario');
  return raw ? JSON.parse(raw) : null;
}
function limparSessao() {
  localStorage.removeItem('ab_token');
  localStorage.removeItem('ab_usuario');
}

/* ============================================
   HELPER — fetch autenticado
   ============================================ */
async function apiFetch(path, options = {}) {
  const url = `${API_BASE}${path}`;
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  let resp;
  try {
    resp = await fetch(url, { ...options, headers });
  } catch (_) {
    throw new Error('Não foi possível conectar ao servidor. Verifique se a API está em execução.');
  }

  if (resp.status === 401) {
    limparSessao();
    window.location.href = 'login.html';
    throw new Error('Sessão expirada');
  }

  return resp;
}

/** Lê a mensagem de erro padrão da API ({ "erro": "..." }) ou usa a mensagem padrão. */
async function erroDaResposta(resp, padrao) {
  if (resp.status === 403) return 'Acesso negado para o seu perfil.';
  try {
    const erro = await resp.json();
    return erro.erro || erro.message || padrao;
  } catch (_) {
    return padrao;
  }
}

async function apiJson(path, options = {}, erroPadrao = 'Erro na requisição') {
  const resp = await apiFetch(path, options);
  if (!resp.ok) throw new Error(await erroDaResposta(resp, erroPadrao));
  const texto = await resp.text();
  return texto ? JSON.parse(texto) : null;
}

/* ============================================
   LOGIN (real)
   ============================================ */
async function login(email, senha) {
  let resp;
  try {
    resp = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ login: email, senha }),
    });
  } catch (_) {
    throw new Error('Não foi possível conectar ao servidor. Verifique se a API está em execução.');
  }

  if (!resp.ok) {
    let msg = 'E-mail ou senha inválidos';
    try {
      const erro = await resp.json();
      msg = erro.erro || msg;
    } catch (_) {}
    throw new Error(msg);
  }

  const data = await resp.json();

  // Verifica se é admin ou vendedor
  if (data.perfil !== 'ADMINISTRADOR' && data.perfil !== 'VENDEDOR') {
    throw new Error('Acesso restrito a administradores e vendedores.');
  }

  return data;
}

/* ============================================
   ADAPTAÇÃO DE CAMPOS
   A API devolve as entidades (relacionamentos como objeto, ex.: categoria: { id, nome }).
   As telas trabalham com ids e nomes simples; aqui criamos esses atalhos na leitura
   e convertemos de volta na escrita, num único lugar.
   ============================================ */
/**
 * Pedido no formato do painel (PedidoAdminResponse). Se a API ainda devolver a entidade
 * (versão anterior do backend: cliente/vendedor como objeto), converte para o mesmo formato.
 */
function normalizarPedido(p) {
  if (!p || typeof p !== 'object' || p.clienteNome !== undefined) return p;
  const c = p.cliente || {};
  const v = p.vendedor || null;
  const end = (c.enderecos || []).find(e => e.principal) || (c.enderecos || [])[0] || null;
  return {
    ...p,
    origem: v ? 'PAINEL' : 'APP',
    clienteId: c.id ?? null,
    clienteNome: c.nomeCompleto ?? '',
    clienteEmail: c.email ?? null,
    clienteTelefone: c.telefone ?? null,
    clienteCpf: c.cpf ?? null,
    vendedorId: v?.id ?? null,
    vendedorNome: v?.nomeCompleto ?? null,
    enderecoEntrega: end,
    itens: (p.itens || []).map(i => ({
      ...i,
      produtoId: i.produto?.id ?? i.produtoId ?? null,
      variacaoId: i.variacao?.id ?? i.variacaoId ?? null,
      produtoNome: i.nomeProduto ?? i.produtoNome ?? '',
    })),
    historico: (p.historico || []).map(h => ({
      ...h,
      status: h.statusNovo ?? h.status,
      data: h.criadoEm ?? h.data,
      usuarioId: h.usuario?.id ?? null,
      usuario: h.usuario?.nomeCompleto ?? (typeof h.usuario === 'string' ? h.usuario : 'Sistema'),
    })).sort((a, b) => new Date(a.data) - new Date(b.data)),
  };
}

function normalizar(tipo, item) {
  if (!item || typeof item !== 'object') return item;
  switch (tipo) {
    case 'pedidos':
      return normalizarPedido(item);
    case 'produtos':
      item.categoriaId = item.categoria?.id ?? item.categoriaId ?? null;
      item.marcaId = item.marca?.id ?? item.marcaId ?? null;
      item.categoriaNome = item.categoria?.nome ?? null;
      item.marcaNome = item.marca?.nome ?? null;
      (item.variacoes || []).forEach(v => {
        v.tamanhoId = v.tamanho?.id ?? v.tamanhoId ?? null;
        v.corId = v.cor?.id ?? v.corId ?? null;
        v.modeloId = v.modelo?.id ?? v.modeloId ?? null;
      });
      break;
    case 'categorias':
      item.categoriaPaiId = item.categoriaPai?.id ?? item.categoriaPaiId ?? null;
      break;
    case 'usuarios':
      item.nome = item.nomeCompleto ?? item.nome ?? '';
      break;
    case 'tamanhos':
      item.ordem = item.ordemExibicao ?? item.ordem ?? 0;
      break;
  }
  return item;
}

function paraApi(tipo, body) {
  const b = { ...body };
  switch (tipo) {
    case 'categorias':
      if ('categoriaPaiId' in b) {
        b.categoriaPai = b.categoriaPaiId ? { id: Number(b.categoriaPaiId) } : null;
        delete b.categoriaPaiId;
      }
      break;
    case 'usuarios':
      if (b.nome !== undefined) b.nomeCompleto = b.nome;
      delete b.nome;
      break;
    case 'tamanhos':
      if (b.ordem !== undefined) b.ordemExibicao = b.ordem;
      delete b.ordem;
      break;
  }
  return b;
}

/* ============================================
   CRUD ADMIN (real)
   ============================================ */
const adminApi = {
  listar: async (tipo) => {
    const lista = await apiJson(`/admin/${tipo}`, {}, `Erro ao listar ${tipo}`);
    return (lista || []).map(item => normalizar(tipo, item));
  },

  buscar: async (tipo, id) => {
    const item = await apiJson(`/admin/${tipo}/${id}`, {}, 'Registro não encontrado');
    return normalizar(tipo, item);
  },

  criar: async (tipo, body) => {
    const item = await apiJson(`/admin/${tipo}`, {
      method: 'POST',
      body: JSON.stringify(paraApi(tipo, body)),
    }, 'Erro ao criar');
    return normalizar(tipo, item);
  },

  atualizar: async (tipo, id, body) => {
    const item = await apiJson(`/admin/${tipo}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(paraApi(tipo, body)),
    }, 'Erro ao atualizar');
    return normalizar(tipo, item);
  },

  excluir: async (tipo, id) => {
    await apiJson(`/admin/${tipo}/${id}`, { method: 'DELETE' }, 'Erro ao excluir');
  },

  // operacao: 'entrada' | 'saida' | 'ajuste' | 'estorno'. No ajuste, quantidade = novo saldo da variação.
  movimentarEstoque: async (variacaoId, operacao, body) => {
    return apiJson(`/admin/estoque/variacoes/${variacaoId}/${operacao}`, {
      method: 'POST',
      body: JSON.stringify(body),
    }, 'Erro ao registrar movimentação');
  },
};

/* ============================================
   PEDIDOS (endpoints próprios do painel)
   Sempre vinculados a um cliente; o usuário logado fica registrado como vendedor/responsável.
   ============================================ */
const pedidosApi = {
  listar: async () => ((await apiJson('/admin/pedidos', {}, 'Erro ao listar pedidos')) || [])
    .map(normalizarPedido)
    .sort((a, b) => new Date(b.criadoEm) - new Date(a.criadoEm)),
  buscar: async (id) => normalizarPedido(await apiJson(`/admin/pedidos/${id}`, {}, 'Pedido não encontrado')),
  // body: { clienteId, formaRecebimento, cupom, observacao, itens: [{ produtoId, variacaoId, quantidade }] }
  criar: (body) => apiJson('/admin/pedidos', { method: 'POST', body: JSON.stringify(body) }, 'Erro ao criar pedido'),
  // body: { status, observacao, motivoCancelamento }
  alterarStatus: (id, body) => apiJson(`/admin/pedidos/${id}/status`, { method: 'PUT', body: JSON.stringify(body) }, 'Erro ao alterar status'),
};

/** Valida um cupom com a regra do servidor (mesma usada na criação do pedido). */
async function validarCupom(codigo, subtotal) {
  return apiJson('/cupons/validar', {
    method: 'POST',
    body: JSON.stringify({ codigo, subtotal }),
  }, 'Cupom inválido');
}

/* ============================================
   STATUS DE PEDIDO — rótulos e cores compartilhados
   ============================================ */
const STATUS_PEDIDO = {
  RASCUNHO: 'Rascunho',
  PEDIDO_GERADO: 'Pedido gerado',
  EM_ANALISE: 'Em análise',
  AGUARDANDO_CONFIRMACAO: 'Aguardando confirmação',
  CONFIRMADO: 'Confirmado',
  SEPARANDO_PRODUTOS: 'Separando produtos',
  PRONTO_PARA_RETIRADA: 'Pronto para retirada',
  EM_TRANSPORTE: 'Em transporte',
  ENTREGUE: 'Entregue',
  CANCELADO: 'Cancelado',
};
function rotuloStatus(status) { return STATUS_PEDIDO[status] || status || '—'; }
function classeStatus(status) {
  if (status === 'CANCELADO') return 'badge-inativo';
  if (status === 'ENTREGUE') return 'badge-ativo';
  if (['RASCUNHO', 'PEDIDO_GERADO', 'EM_ANALISE', 'AGUARDANDO_CONFIRMACAO'].includes(status)) return 'badge-esgotado';
  return 'badge-info';
}

/* ============================================
   MENSAGENS PADRONIZADAS (mesmo texto de MensagensLoja.java no backend)
   ============================================ */
const MensagensLoja = {
  nomeItem(nome, variacaoDescricao) {
    return variacaoDescricao ? `${nome} (${variacaoDescricao})` : nome;
  },
  estoqueInsuficiente(nome, variacaoDescricao, disponivel, solicitado) {
    const item = this.nomeItem(nome, variacaoDescricao);
    if (disponivel <= 0) return `Produto sem estoque: ${item}. Remova o item ou escolha outra opção.`;
    return `Estoque insuficiente para ${item}: disponível ${disponivel} ${disponivel === 1 ? 'unidade' : 'unidades'}, solicitado ${solicitado}.`;
  },
  selecioneVariacao(nome) {
    return `Selecione tamanho, cor ou modelo de ${nome} antes de finalizar.`;
  },
  produtoSemVariacao(nome) {
    return `O produto ${nome} está indisponível para venda: não possui tamanho/cor com estoque cadastrado.`;
  },
  produtoPrecisaVariacao: 'Cadastre pelo menos uma variação (tamanho/cor/modelo): o estoque é controlado por variação.',
};

/* O estoque fica nas variações do produto; o saldo do produto é a soma delas. */
function estoqueProduto(produto) {
  return (produto.variacoes || []).reduce((s, v) => s + (v.estoqueAtual || 0), 0);
}
