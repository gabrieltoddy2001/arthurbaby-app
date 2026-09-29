/* ============================================
   api.js — camada de dados (modo MOCK)
   Pra plugar no backend real depois, é só
   trocar MOCK para false e usar as chamadas HTTP.
   ============================================ */

   const MOCK = true;
   const API_BASE = 'http://localhost:8080/api';
   
   /* ----- Sessão ----- */
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
   
   /* ----- Login (mock) ----- */
   async function login(email, senha) {
     await new Promise(r => setTimeout(r, 300));
     if (email === 'admin@arthurbaby.com.br' && senha === '123456') {
       return { token: 'mock-token-' + Date.now(), usuarioId: 1, nome: 'Administrador ArthurBaby', perfil: 'ADMINISTRADOR' };
     }
     if (email === 'vendedor@arthurbaby.com.br' && senha === '123456') {
       return { token: 'mock-token-' + Date.now(), usuarioId: 2, nome: 'Carlos Almeida', perfil: 'VENDEDOR' };
     }
     throw new Error('E-mail ou senha inválidos');
   }
   
   /* ----- CRUD admin (mock) ----- */
   const adminApi = {
    listar: async (tipo) => {
      await new Promise(r => setTimeout(r, 150));
      return [...(MOCK_DB[tipo] || [])];
    },
    buscar: async (tipo, id) => {
      await new Promise(r => setTimeout(r, 150));
      const item = (MOCK_DB[tipo] || []).find(x => x.id === id);
      if (!item) throw new Error('Não encontrado');
      return { ...item };
    },
    criar: async (tipo, body) => {
      await new Promise(r => setTimeout(r, 150));
      const novo = { id: mockNovoId(), ...body };
      if (!MOCK_DB[tipo]) MOCK_DB[tipo] = [];
      MOCK_DB[tipo].push(novo);
      salvarMock();  // ← NOVO: persiste no localStorage
      return novo;
    },
    atualizar: async (tipo, id, body) => {
      await new Promise(r => setTimeout(r, 150));
      const lista = MOCK_DB[tipo] || [];
      const idx = lista.findIndex(x => x.id === id);
      if (idx < 0) throw new Error('Não encontrado');
      lista[idx] = { ...lista[idx], ...body, id };
      salvarMock();  // ← NOVO: persiste no localStorage
      return lista[idx];
    },
    excluir: async (tipo, id) => {
      await new Promise(r => setTimeout(r, 150));
      if (MOCK_DB[tipo]) {
        MOCK_DB[tipo] = MOCK_DB[tipo].filter(x => x.id !== id);
        salvarMock();  // ← NOVO: persiste no localStorage
      }
    },
  };