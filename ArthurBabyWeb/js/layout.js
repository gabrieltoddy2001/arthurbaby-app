/* ============================================
   layout.js — sidebar + header padrão
   Ícones SVG inline (sem emojis)
   ============================================ */

// Ícones SVG (Lucide Icons - open source, MIT) com stroke-width calibrado
const ICONES = {
  dashboard: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>',
  categorias: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>',
  marcas: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>',
  
  // NOVO ÍCONE DE ATRIBUTOS (Camadas) - Muito mais moderno
  variacoes: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>',
  
  produtos: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>',
  estoque: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>',
  pedidos: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>',
  clientes: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
  usuarios: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>',
  relatorios: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="20" x2="12" y2="10"/><line x1="18" y1="20" x2="18" y2="4"/><line x1="6" y1="20" x2="6" y2="16"/><polyline points="22 6 13 15 9 11 2 18"/></svg>',
  configuracoes: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>',
  auditoria: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>',
  novoPedido: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>',
  cupons: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>',
  menu: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="18" x2="21" y2="18"/></svg>',
  sair: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>',
};

function montarLayout(tituloPagina, paginaAtiva) {
  const usuario = exigirLogin();
  if (!usuario) return null;

  const menu = [
    { secao: 'Geral', somenteAdmin: true },
    { href: 'dashboard.html',    label: 'Dashboard',   icone: 'dashboard', chave: 'dashboard', somenteAdmin: true },

    { secao: 'Cadastros' },
    { href: 'categorias.html',   label: 'Categorias',  icone: 'categorias', chave: 'categorias' },
    { href: 'marcas.html',       label: 'Marcas',      icone: 'marcas', chave: 'marcas' },
    { href: 'variacoes.html',    label: 'Atributos',   icone: 'variacoes', chave: 'variacoes' },
    { href: 'produtos.html',     label: 'Produtos',    icone: 'produtos', chave: 'produtos' },

    { secao: 'Operações' },
    { href: 'estoque.html',      label: 'Estoque',     icone: 'estoque', chave: 'estoque' },
    { href: 'pedido-novo.html',  label: 'Novo pedido', icone: 'novoPedido', chave: 'pedido-novo' },
    { href: 'pedidos.html',      label: 'Pedidos',     icone: 'pedidos', chave: 'pedidos' },
    { href: 'clientes.html',     label: 'Clientes',    icone: 'clientes', chave: 'clientes' },
    { href: 'cupons.html',       label: 'Cupons',      icone: 'cupons', chave: 'cupons' },

    { secao: 'Sistema', somenteAdmin: true },
    { href: 'usuarios.html',     label: 'Usuários internos', icone: 'usuarios', chave: 'usuarios', somenteAdmin: true },
    { href: 'relatorios.html',   label: 'Relatórios',  icone: 'relatorios', chave: 'relatorios', somenteAdmin: true },
    { href: 'configuracoes.html',label: 'Configurações', icone: 'configuracoes', chave: 'configuracoes', somenteAdmin: true },
    { href: 'auditoria.html',    label: 'Auditoria',   icone: 'auditoria', chave: 'auditoria', somenteAdmin: true },
  ];

  // Vendedor não acessa as telas de Geral e Sistema (nem pelo endereço direto)
  if (!ehAdmin(usuario) && PAGINAS_SOMENTE_ADMIN.includes(paginaAtiva)) {
    window.location.replace(paginaInicial(usuario));
    return null;
  }

  const sidebar = menu.filter(item => !item.somenteAdmin || ehAdmin(usuario)).map(item => {
    if (item.secao) return `<div class="secao">${item.secao}</div>`;
    const svg = ICONES[item.icone] || '';
    return `<a href="${item.href}" class="${item.chave === paginaAtiva ? 'ativo' : ''}">
      ${svg}<span>${item.label}</span>
    </a>`;
  }).join('');

  const html = `
    <aside class="sidebar">
      <a href="${paginaInicial(usuario)}" class="sidebar-logo" title="Lojão Arthur Baby">
        <img src="assets/logo-arthurbaby.png" alt="Lojão Arthur Baby">
      </a>
      ${sidebar}
      <a href="#" class="sair" onclick="logout(); return false;">
        ${ICONES.sair}<span>Sair</span>
      </a>
    </aside>
    <main class="main">
      <div class="header">
        <div class="header-titulo">
          <button type="button" class="btn-menu" aria-label="Abrir menu" onclick="alternarMenu()">${ICONES.menu}</button>
          <h1>${tituloPagina}</h1>
        </div>
        <span class="usuario">${usuario.nome} (${usuario.perfil})</span>
      </div>
      <div id="conteudo"></div>
    </main>
    <div class="sidebar-fundo" onclick="alternarMenu(false)"></div>
  `;

  document.querySelector('.app').innerHTML = html;
  document.title = `${tituloPagina} — ArthurBaby Admin`;
  garantirFavicon();
  return usuario;
}

/* Menu lateral no celular/tablet (a sidebar fica escondida abaixo de 900px) */
function alternarMenu(abrir) {
  const sidebar = document.querySelector('.sidebar');
  if (!sidebar) return;
  const aberta = abrir ?? !sidebar.classList.contains('aberta');
  sidebar.classList.toggle('aberta', aberta);
  document.body.classList.toggle('menu-aberto', aberta);
}

function garantirFavicon() {
  if (document.querySelector('link[rel="icon"]')) return;
  const link = document.createElement('link');
  link.rel = 'icon';
  link.type = 'image/png';
  link.href = 'assets/favicon.png';
  document.head.appendChild(link);
}

/* Redefine a senha de um usuário (cliente ou interno). A API grava o hash BCrypt. */
async function redefinirSenhaUsuario(u, aoConcluir) {
  const nova = await pedirTexto(POLITICA_SENHA, { titulo: `Nova senha para "${u.nome || u.nomeCompleto}"`, tipo: 'password', textoOk: 'Redefinir' });
  if (nova === null) return;
  const erroSenha = erroPoliticaSenha(nova);
  if (erroSenha) { alert(erroSenha); return; }
  try {
    // A API registra a auditoria (RESETAR_SENHA) com o usuário do token; o navegador não grava logs.
    await adminApi.atualizar('usuarios', u.id, { ...u, senha: nova });
    alert('Senha redefinida. Informe a nova senha ao usuário por um canal seguro.');
    if (aoConcluir) aoConcluir();
  } catch (err) {
    alert('Erro ao redefinir senha: ' + err.message);
  }
}

/* ============================================
   JANELAS DO PAINEL (no lugar de confirm/prompt/alert)
   Alguns navegadores e visualizadores embutidos bloqueiam as janelas nativas: o confirm()
   devolvia "não" na hora e botões como "Bloquear" pareciam não fazer nada.
   ============================================ */
function abrirJanela({ titulo, mensagem, campo, textoOk = 'Confirmar', textoCancelar = 'Cancelar', perigo = false }) {
  return new Promise(resolve => {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
      <div class="modal" role="dialog" aria-modal="true" style="max-width:440px;">
        <h2></h2>
        <p class="janela-msg" style="white-space:pre-line;color:var(--texto-secundario);line-height:1.5;"></p>
        ${campo ? `<div class="field" style="margin-top:1rem;"><input class="janela-campo" autocomplete="off"></div>` : ''}
        <div class="modal-acoes">
          ${textoCancelar ? '<button type="button" class="btn btn-ghost janela-cancelar"></button>' : ''}
          <button type="button" class="btn ${perigo ? 'btn-danger' : 'btn-primary'} janela-ok"></button>
        </div>
      </div>`;
    overlay.querySelector('h2').textContent = titulo || 'Confirmar';
    overlay.querySelector('.janela-msg').textContent = mensagem || '';
    overlay.querySelector('.janela-ok').textContent = textoOk;
    const btnCancelar = overlay.querySelector('.janela-cancelar');
    if (btnCancelar) btnCancelar.textContent = textoCancelar;
    const input = overlay.querySelector('.janela-campo');
    if (input) {
      input.type = campo.tipo || 'text';
      input.placeholder = campo.placeholder || '';
    }

    const fechar = (valor) => {
      document.removeEventListener('keydown', teclas);
      overlay.remove();
      resolve(valor);
    };
    const confirmar = () => fechar(input ? input.value : true);
    const cancelar = () => fechar(input ? null : false);
    const teclas = (e) => {
      if (e.key === 'Escape') cancelar();
      if (e.key === 'Enter') { e.preventDefault(); confirmar(); }
    };

    overlay.querySelector('.janela-ok').addEventListener('click', confirmar);
    if (btnCancelar) btnCancelar.addEventListener('click', cancelar);
    overlay.addEventListener('click', (e) => { if (e.target === overlay) cancelar(); });
    document.addEventListener('keydown', teclas);
    document.body.appendChild(overlay);
    (input || overlay.querySelector('.janela-ok')).focus();
  });
}

/** Pergunta sim/não. Ex.: if (!(await confirmarAcao('Bloquear o cliente?', { textoOk: 'Bloquear', perigo: true }))) return; */
function confirmarAcao(mensagem, opcoes = {}) {
  return abrirJanela({ titulo: opcoes.titulo || 'Confirmar', mensagem, ...opcoes });
}

/** Pede um texto (ou senha). Devolve o texto digitado, ou null se cancelar. */
function pedirTexto(mensagem, opcoes = {}) {
  return abrirJanela({ titulo: opcoes.titulo || 'Informe', mensagem, campo: { tipo: opcoes.tipo, placeholder: opcoes.placeholder }, textoOk: opcoes.textoOk || 'OK', ...opcoes });
}

/** Aviso simples com botão OK. Também substitui o alert() do navegador em todo o painel. */
function avisar(mensagem, titulo = 'Aviso') {
  return abrirJanela({ titulo, mensagem, textoOk: 'OK', textoCancelar: null });
}
window.alert = (mensagem) => { avisar(String(mensagem ?? '')); };

/* Formatação compartilhada */
function formatarMoeda(v) {
  return Number(v || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}
/** Valor para campos de dinheiro (input number): 39.9 → "39.90"; vazio continua vazio. */
function valorDinheiro(v) {
  return v === null || v === undefined || v === '' || isNaN(Number(v)) ? '' : Number(v).toFixed(2);
}
/** Valor sem o "R$", com separador de milhar: 1283.2 → "1.283,20". */
function formatarPreco(v) {
  return Number(v || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}