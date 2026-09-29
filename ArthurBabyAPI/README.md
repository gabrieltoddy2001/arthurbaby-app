# ArthurBaby API

Backend Spring Boot para catalogo, clientes, favoritos, estoque e ordens de pedido.

Requer JDK 21 (Spring Boot 3.5).

## Executar com MySQL (padrao)

O perfil `mysql` e ativado por padrao (`application.properties`). O banco `arthurbaby` e criado
automaticamente se nao existir. Configure a senha do MySQL e o segredo JWT no ambiente antes de iniciar:

- Host: `localhost:3306`
- Usuario: `root`
- Senha: variavel `DB_PASSWORD`
- Segredo JWT: variavel `JWT_SECRET` (use uma chave aleatoria com pelo menos 32 caracteres)

```bash
mvn spring-boot:run
```

Se preferir, use o script `database/arthurbaby_schema.sql` para criar as tabelas manualmente
antes de subir a aplicacao (veja o cabecalho do arquivo para detalhes). Isso e opcional: com
`ddl-auto=update` a propria aplicacao cria/atualiza as tabelas ao iniciar.

Variaveis de conexao opcionais:

```bash
DB_URL=jdbc:mysql://localhost:3306/arthurbaby?createDatabaseIfNotExist=true
DB_USER=root
DB_PASSWORD=sua-senha-do-mysql
JWT_SECRET=sua-chave-aleatoria-com-pelo-menos-32-caracteres
```

## Executar em teste rapido com H2 (em memoria, sem MySQL)

```bash
mvn spring-boot:run -DSPRING_PROFILES_ACTIVE=default
```

Swagger: http://localhost:8080/swagger-ui.html

Console H2: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:arthurbaby`
- User: `sa`
- Password: vazio

## Usuarios iniciais

- Admin: `admin@arthurbaby.com.br` / `123456`
- Vendedor: `vendedor@arthurbaby.com.br` / `123456`
- Cliente: `ana.souza@email.com` / `123456`

## Fluxo minimo de teste

```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"login\":\"admin@arthurbaby.com.br\",\"senha\":\"123456\"}"
```

Use o campo `token` retornado:

No Swagger, clique em **Authorize** e informe o token no esquema `bearerAuth`.
Se a tela abrir, mas as chamadas da API retornarem 403, o usuario autenticado nao tem permissao
para aquela rota ou o token nao foi informado.

```bash
curl http://localhost:8080/api/categorias -H "Authorization: Bearer SEU_TOKEN"
curl http://localhost:8080/api/produtos -H "Authorization: Bearer SEU_TOKEN"
```

Criar categoria:

```bash
curl -X POST http://localhost:8080/api/categorias ^
  -H "Authorization: Bearer SEU_TOKEN" -H "Content-Type: application/json" ^
  -d "{\"nome\":\"Saida de Maternidade\",\"descricao\":\"Conjuntos especiais\",\"ordemExibicao\":11,\"status\":\"ATIVA\"}"
```

## Fluxo do cliente (cadastro, perfil e enderecos)

```bash
curl -X POST http://localhost:8080/api/auth/cadastro ^
  -H "Content-Type: application/json" ^
  -d "{\"nomeCompleto\":\"Maria Silva\",\"email\":\"maria@teste.com\",\"cpf\":\"111.444.777-35\",\"telefone\":\"(71) 99999-0000\",\"senha\":\"123456\",\"aceiteTermoUso\":true,\"aceiteLgpd\":true,\"endereco\":{\"cep\":\"40020-455\",\"logradouro\":\"Av. Sete de Setembro\",\"numero\":\"548\",\"bairro\":\"Centro\",\"cidade\":\"Salvador\",\"uf\":\"BA\"}}"

curl -X POST http://localhost:8080/api/auth/recuperar-senha ^
  -H "Content-Type: application/json" -d "{\"email\":\"maria@teste.com\"}"
# Responde 200 {"mensagem": "Se o e-mail estiver cadastrado, voce recebera um codigo."} -- nunca o token.
# O token (tabela password_reset_token, valido por 1h, uso unico) e logado no console: [RECUPERAR-SENHA][DEV].

curl -X POST http://localhost:8080/api/auth/redefinir-senha ^
  -H "Content-Type: application/json" -d "{\"token\":\"TOKEN_DO_LOG\",\"novaSenha\":\"novaSenha123\"}"

curl http://localhost:8080/api/clientes/SEU_ID -H "Authorization: Bearer SEU_TOKEN"
curl -X PUT http://localhost:8080/api/clientes/SEU_ID -H "Authorization: Bearer SEU_TOKEN" ^
  -H "Content-Type: application/json" -d "{\"telefone\":\"(71) 98888-1111\"}"

curl http://localhost:8080/api/clientes/SEU_ID/enderecos -H "Authorization: Bearer SEU_TOKEN"
curl -X POST http://localhost:8080/api/clientes/SEU_ID/enderecos -H "Authorization: Bearer SEU_TOKEN" ^
  -H "Content-Type: application/json" -d "{\"cep\":\"41750-000\",\"logradouro\":\"Rua Nova\",\"numero\":\"10\",\"bairro\":\"Piata\",\"cidade\":\"Salvador\",\"uf\":\"BA\",\"principal\":true}"
curl -X DELETE http://localhost:8080/api/clientes/SEU_ID/enderecos/ENDERECO_ID -H "Authorization: Bearer SEU_TOKEN"
```

> Um cliente so acessa/edita o proprio `/api/clientes/{id}` (e seus enderecos); usuarios
> `ADMINISTRADOR`/`VENDEDOR` podem acessar qualquer cliente. Tentativas de acessar dados de
> outro cliente retornam `403`.

Criar pedido:

```bash
curl -X POST http://localhost:8080/api/pedidos ^
  -H "Authorization: Bearer SEU_TOKEN" -H "Content-Type: application/json" ^
  -d "{\"clienteId\":2,\"formaRecebimento\":\"RETIRADA_LOJA\",\"observacao\":\"Separar para retirada\",\"itens\":[{\"produtoId\":1,\"variacaoId\":1,\"quantidade\":1}]}"
```

## Catalogo e pedidos

- `GET /api/categorias` devolve a arvore de categorias ativas: `[{id, nome, icone, subcategorias:[{id, nome}]}]`.
- `GET /api/produtos?promocao=true` filtra somente produtos em promocao (combina com `q`, `categoriaId`, `precoMin`, `precoMax`).
- `GET /api/produtos/{id}` devolve o detalhe: `marca` (nome), `avaliacao`, `imagens:[{url, principal}]` e
  `variacoes:[{id, sku, tamanho, cor, modelo, preco, estoqueAtual}]` (somente variacoes ativas).

Pedidos (todas as respostas usam `PedidoResponse`: `numero`, `status`, `data`, `cliente`, `itens`, `subtotal`,
`cupom`, `desconto`, `frete`, `total`, `historico`...):

```bash
# Previa do cupom (publico) -> {valido, codigo, tipo, descricao, desconto, freteGratis, motivo}
curl -X POST http://localhost:8080/api/cupons/validar -H "Content-Type: application/json" ^
  -d "{\"codigo\":\"ARTHUR10\",\"subtotal\":149.90}"

# Criar pedido com cupom (desconto e frete sao calculados pelo servidor)
curl -X POST http://localhost:8080/api/pedidos ^
  -H "Authorization: Bearer SEU_TOKEN" -H "Content-Type: application/json" ^
  -d "{\"clienteId\":2,\"formaRecebimento\":\"ENTREGA\",\"cupom\":\"ARTHUR10\",\"itens\":[{\"produtoId\":1,\"variacaoId\":1,\"quantidade\":2}]}"

# Buscar por numero do pedido (ex.: AB20250918143025)
curl http://localhost:8080/api/pedidos/numero/NUMERO_DO_PEDIDO -H "Authorization: Bearer SEU_TOKEN"

# Cancelar (aceita "motivo" ou "motivoCancelamento"; devolve os itens ao estoque)
curl -X PUT http://localhost:8080/api/pedidos/NUMERO_DO_PEDIDO/cancelar ^
  -H "Authorization: Bearer SEU_TOKEN" -H "Content-Type: application/json" -d "{\"motivo\":\"Cliente desistiu\"}"
```

Valores do pedido: **tudo e recalculado no servidor**; os campos `desconto` e `frete` enviados pelo app sao ignorados.

- `subtotal = soma(valorUnitario x quantidade)`; o desconto e global (nivel do pedido) e so existe via cupom.
- Cupons ficam na tabela `cupom` (admin gerencia via `/api/admin/...` ou SQL): `PERCENTUAL` (valor = %),
  `VALOR_FIXO` (valor = R$) e `FRETE_GRATIS`; opcionais `valorMinimo`, `valorMaximoDesconto`, `validoDe`, `validoAte`, `ativo`.
  Seed: `ARTHUR10` (10%), `FRETEGRATIS`, `BEMVINDO` (R$ 15). Cupom inexistente/nao aplicavel no pedido retorna `400`.
- Frete: retirada na loja = 0; entrega = R$ 15,00, gratis a partir de R$ 200,00 de subtotal ou com cupom `FRETE_GRATIS`
  (configuravel em `app.frete.valor-entrega` e `app.frete.gratis-a-partir-de`).
- `total = subtotal - desconto + frete`, nunca negativo. Cliente so cria pedido para si mesmo (`403`).

Cancelamento: exige motivo, nao permite cancelar pedido ja cancelado ou entregue, registra o historico e estorna
o estoque (`MovimentacaoEstoque` do tipo `ESTORNO`). Cliente so ve/cancela os proprios pedidos (`403` caso contrario).

## Outros endpoints

- `GET /api/auth/me` (token): dados do usuario logado `{id, nomeCompleto, email, cpf, telefone, perfil, status}`.
- Favoritos (token, cliente so acessa os proprios): `GET /api/favoritos/cliente/{id}` -> `[{id, produto:{...,marca, imagens}, criadoEm}]`,
  `POST /api/favoritos`, `DELETE /api/favoritos` (corpo) ou `DELETE /api/favoritos/cliente/{id}/produto/{produtoId}` (sem corpo, para Retrofit).
- `POST /api/admin/produtos/completo`: cria produto + imagens + variacoes numa transacao; estoque inicial vira movimentacao `ENTRADA`.
- Estoque manual (admin/vendedor), corpo `{quantidade, observacao}`:
  `POST /api/admin/estoque/variacoes/{variacaoId}/entrada` (soma), `/ajuste` (define o total) e `/estorno` (devolve).

Regra geral: controllers nunca devolvem entidades JPA; o CRUD generico `/api/admin/{tipo}` converte para JSON
dentro da transacao e nunca expoe a senha (hash) do usuario. Erros sempre saem como `{ "erro": ..., "codigo": ... }`
(400, 401, 403, 404, 405, 409, 415, 500).

Migracoes automaticas no boot (`SchemaMigration`): converte o cupom antigo (`percentual_desconto`) para `tipo/valor`
e alinha a FK de `password_reset_token` com `usuario.id BIGINT UNSIGNED` em bancos criados pelo `arthurbaby_bd.sql`.
