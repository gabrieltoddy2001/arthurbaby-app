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
  
  function logout() {
    limparSessao();
    window.location.href = 'login.html';
  }
  
  function redirecionarSeLogado() {
    if (getToken() && getUsuario()) {
      window.location.href = 'dashboard.html';
    }
  }