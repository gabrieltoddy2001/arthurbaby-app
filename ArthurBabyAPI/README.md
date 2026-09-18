# ArthurBaby API

Backend Spring Boot para catalogo, clientes, favoritos, estoque e ordens de pedido.

## Executar em teste rapido com H2

```bash
mvn spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui.html

Console H2: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:arthurbaby`
- User: `sa`
- Password: vazio

## Executar com MySQL

Crie o banco ou deixe a URL criar automaticamente:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Variaveis opcionais:

```bash
DB_URL=jdbc:mysql://localhost:3306/arthurbaby?createDatabaseIfNotExist=true
DB_USER=root
DB_PASSWORD=Admin321
JWT_SECRET=troque-por-uma-chave-grande-em-producao
```

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

Criar pedido:

```bash
curl -X POST http://localhost:8080/api/pedidos ^
  -H "Authorization: Bearer SEU_TOKEN" -H "Content-Type: application/json" ^
  -d "{\"clienteId\":2,\"formaRecebimento\":\"RETIRADA_LOJA\",\"observacao\":\"Separar para retirada\",\"itens\":[{\"produtoId\":1,\"variacaoId\":1,\"quantidade\":1}]}"
```
