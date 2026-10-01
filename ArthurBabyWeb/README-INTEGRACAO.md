# ArthurBaby — Painel Admin (Frontend)

Painel administrativo web do projeto ArthurBaby. Desenvolvido em **HTML5 + CSS3 + JavaScript puro**, com **camada de dados abstrata** para integração futura com o backend.

---

## Visão geral

- **Tecnologias:** HTML5, CSS3, JavaScript (Vanilla).
- **Persistência atual:** `localStorage` (mock em memória).
- **Servidor de desenvolvimento:** Live Server (VSCode).
- **Integração prevista:** API REST do backend Spring Boot.

---

## Estrutura do projeto
ArthurBabyWeb/
├── index.html → redireciona pro login/dashboard
├── login.html → tela de login
├── dashboard.html → painel com indicadores
├── categorias.html → CRUD de categorias
├── marcas.html → CRUD de marcas
├── variacoes.html → CRUD de tamanhos/cores/modelos
├── produtos.html → listagem de produtos
├── produto-form.html → criar/editar produto (com imagens e variações)
├── estoque.html → controle de estoque
├── pedidos.html → listagem de pedidos
├── pedido-detalhe.html → detalhe do pedido + mudança de status
├── clientes.html → gestão de clientes
├── usuarios.html → gestão de usuários internos
├── relatorios.html → relatórios (mais vendidos, estoque, faturamento)
├── configuracoes.html → configurações da loja
├── auditoria.html → log de auditoria
├── css/
│ └── style.css → design system completo
└── js/
├── mock-data.js → banco de dados fictício (localStorage)
├── api.js → camada de dados (MOCK / REAL)
├── auth.js → login e proteção de páginas
├── layout.js → sidebar + header
├── tabela-ordenavel.js → ordenação genérica de tabelas
└── [tela].js → lógica de cada tela


---

## Como rodar

1. Abrir a pasta `ArthurBabyWeb` no VSCode.
2. Clicar com botão direito em `login.html` → **Open with Live Server**.
3. Login com:
   - **E-mail:** `admin@arthurbaby.com.br`
   - **Senha:** `123456`

**Não precisa do backend rodando.** O mock funciona sozinho.

---

## Como o mock funciona

O arquivo **`js/api.js`** é a **única camada que precisa ser substituída** para integração com o backend real.

Ele expõe uma **API abstrata**:

```javascript
// Listar
adminApi.listar('produtos')        → retorna lista de produtos
adminApi.listar('categorias')      → retorna lista de categorias

// Buscar
adminApi.buscar('produtos', 1)     → retorna produto com id=1

// Criar
adminApi.criar('produtos', {...})  → cria produto

// Atualizar
adminApi.atualizar('produtos', 1, {...}) → atualiza produto id=1

// Excluir
adminApi.excluir('produtos', 1)    → remove produto id=1