# ArthurBaby API

Backend Spring Boot para catalogo, clientes, favoritos, estoque e ordens de pedido.

## Executar com MySQL (padrao)

O perfil `mysql` e ativado por padrao (`application.properties`). Basta ter um MySQL local
rodando com as credenciais abaixo (o banco `arthurbaby` e criado automaticamente se nao existir):

- Host: `localhost:3306`
- Usuario: `root`
- Senha: `Admin321`

```bash
mvn spring-boot:run
```

Se preferir, use o script `database/arthurbaby_schema.sql` para criar as tabelas manualmente
antes de subir a aplicacao (veja o cabecalho do arquivo para detalhes). Isso e opcional: com
`ddl-auto=update` a propria aplicacao cria/atualiza as tabelas ao iniciar.

Variaveis opcionais:

```bash
DB_URL=jdbc:mysql://localhost:3306/arthurbaby?createDatabaseIfNotExist=true
DB_USER=root
DB_PASSWORD=Admin321
JWT_SECRET=troque-por-uma-chave-grande-em-producao
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

- Admin: `admin@arthurbaby.com.br` / `admin123`
- Cliente: `cliente@teste.com` / `cliente123`

## Fluxo minimo de teste

```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"login\":\"admin@arthurbaby.com.br\",\"senha\":\"admin123\"}"
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
# O token de redefinicao e logado no console da aplicacao (simulacao de e-mail em dev).

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
# Criar pedido com cupom (ARTHUR10 = 10% sobre o subtotal)
curl -X POST http://localhost:8080/api/pedidos ^
  -H "Authorization: Bearer SEU_TOKEN" -H "Content-Type: application/json" ^
  -d "{\"clienteId\":2,\"formaRecebimento\":\"ENTREGA\",\"cupom\":\"ARTHUR10\",\"frete\":15.00,\"itens\":[{\"produtoId\":1,\"variacaoId\":1,\"quantidade\":2}]}"

# Buscar por numero do pedido (ex.: AB20250918143025)
curl http://localhost:8080/api/pedidos/numero/NUMERO_DO_PEDIDO -H "Authorization: Bearer SEU_TOKEN"

# Cancelar (aceita "motivo" ou "motivoCancelamento"; devolve os itens ao estoque)
curl -X PUT http://localhost:8080/api/pedidos/NUMERO_DO_PEDIDO/cancelar ^
  -H "Authorization: Bearer SEU_TOKEN" -H "Content-Type: application/json" -d "{\"motivo\":\"Cliente desistiu\"}"
```

Regras de desconto: o desconto e **global** (nivel do pedido); os itens nao tem desconto proprio.
`subtotal = soma(valorUnitario x quantidade)` e `total = subtotal - desconto + frete`.
Com `cupom`, o desconto e calculado pelo servidor sobre o subtotal (o `desconto` enviado e ignorado);
cupom inexistente, inativo ou expirado retorna `400`. Sem cupom, vale o `desconto` informado
(nao pode ser negativo nem maior que o subtotal).

Cancelamento: exige motivo, nao permite cancelar pedido ja cancelado ou entregue, registra o historico e estorna
o estoque (`MovimentacaoEstoque` do tipo `ESTORNO`). Cliente so ve/cancela os proprios pedidos (`403` caso contrario).
