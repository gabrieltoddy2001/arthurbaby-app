/* ============================================
   api.js — camada de dados (INTEGRADA com o backend)
   Substitui o mock por chamadas HTTP reais.
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

  const resp = await fetch(url, { ...options, headers });

  if (resp.status === 401) {
    limparSessao();
    window.location.href = 'login.html';
    throw new Error('Sessão expirada');
  }

  return resp;
}

/* ============================================
   LOGIN (real)
   ============================================ */
async function login(email, senha) {
  const resp = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ login: email, senha }),
  });

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
   CRUD ADMIN (real)
   ============================================ */
const adminApi = {
  listar: async (tipo) => {
    const resp = await apiFetch(`/admin/${tipo}`);
    if (!resp.ok) throw new Error(`Erro ao listar ${tipo}`);
    return resp.json();
  },

  buscar: async (tipo, id) => {
    const resp = await apiFetch(`/admin/${tipo}/${id}`);
    if (!resp.ok) throw new Error('Não encontrado');
    return resp.json();
  },

  criar: async (tipo, body) => {
    const resp = await apiFetch(`/admin/${tipo}`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    if (!resp.ok) {
      let msg = 'Erro ao criar';
      try {
        const erro = await resp.json();
        msg = erro.erro || msg;
      } catch (_) {}
      throw new Error(msg);
    }
    return resp.json();
  },

  atualizar: async (tipo, id, body) => {
    const resp = await apiFetch(`/admin/${tipo}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(body),
    });
    if (!resp.ok) {
      let msg = 'Erro ao atualizar';
      try {
        const erro = await resp.json();
        msg = erro.erro || msg;
      } catch (_) {}
      throw new Error(msg);
    }
    return resp.json();
  },

  excluir: async (tipo, id) => {
    const resp = await apiFetch(`/admin/${tipo}/${id}`, { method: 'DELETE' });
    if (!resp.ok) throw new Error('Erro ao excluir');
  },

  // operacao: 'entrada' | 'saida' | 'ajuste' | 'estorno'. No ajuste, quantidade = novo saldo da variação.
  movimentarEstoque: async (variacaoId, operacao, body) => {
    const resp = await apiFetch(`/admin/estoque/variacoes/${variacaoId}/${operacao}`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    if (!resp.ok) {
      let msg = 'Erro ao registrar movimentação';
      try {
        const erro = await resp.json();
        msg = erro.erro || msg;
      } catch (_) {}
      throw new Error(msg);
    }
    return resp.json();
  },
};

/* O estoque fica nas variações do produto; o saldo do produto é a soma delas. */
function estoqueProduto(produto) {
  return (produto.variacoes || []).reduce((s, v) => s + (v.estoqueAtual || 0), 0);
}