# ArthurBaby — Painel Admin (Web)

Painel administrativo em HTML, CSS e JavaScript puro, sem build. Consome a API do projeto (`ArthurBabyAPI`).

## Rodar localmente

Sirva a pasta com qualquer servidor estático na porta 5500 ou 5501 (são as liberadas no CORS da API), por exemplo o Live Server do VS Code.

O endereço da API é escolhido sozinho em `js/api.js`:

- aberto em `localhost` / `127.0.0.1` → API local em `http://localhost:8080`
- publicado (Vercel) → API no Render, `https://arthurbaby-api.onrender.com`

Para usar o painel local contra a API do Render, rode no console do navegador:

```js
localStorage.setItem('ab_api', 'nuvem')   // para voltar: localStorage.removeItem('ab_api')
```

## Estrutura

```
*.html            uma página por tela (login, dashboard, produtos, pedidos, clientes, cupons...)
css/style.css     estilos do painel (cores, tabelas, modais, menu)
js/api.js         chamadas HTTP, sessão e endereço da API
js/auth.js        login e proteção das páginas
js/layout.js      menu lateral, cabeçalho, janelas de confirmação/aviso e formatação de valores
js/<tela>.js      lógica de cada tela
assets/           logo e favicon
```

## Observações

- Confirmações, avisos e campos de texto usam as janelas do próprio painel (`confirmarAcao`, `pedirTexto`, `avisar` em `js/layout.js`), não as do navegador, que alguns navegadores bloqueiam.
- Vendedores não veem Dashboard, Relatórios, Configurações, Usuários e Auditoria.
