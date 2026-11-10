/* ============================================
   auth.js — proteção de páginas
   ============================================ */

   function exigirLogin() {
    const token = getToken();
    const usuario = getUsuario();
    if (!token || !usuario) {
      window.location.href = 'login.html';
      return null;
    }
    if (usuario.perfil !== 'ADMINISTRADOR' && usuario.perfil !== 'VENDEDOR') {
      limparSessao();
      alert('Acesso restrito a administradores e vendedores.');
      window.location.href = 'login.html';
      return null;
    }
    return usuario;
  }
  
  /* Páginas que o perfil VENDEDOR não pode ver (seções Geral e Sistema do menu) */
  const PAGINAS_SOMENTE_ADMIN = ['dashboard', 'usuarios', 'relatorios', 'configuracoes', 'auditoria'];

  /* Tela aberta após o login: o vendedor não vê o Dashboard, então começa em Pedidos */
  function paginaInicial(usuario = getUsuario()) {
    return ehAdmin(usuario) ? 'dashboard.html' : 'pedidos.html';
  }

  function ehAdmin(usuario = getUsuario()) {
    return usuario?.perfil === 'ADMINISTRADOR';
  }

  function logout() {
    limparSessao();
    window.location.href = 'login.html';
  }
  
  function redirecionarSeLogado() {
    if (getToken() && getUsuario()) {
      window.location.href = paginaInicial();
    }
  }