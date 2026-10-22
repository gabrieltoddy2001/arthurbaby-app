# Especificação de Back-end — Sprint de Melhorias do App

Este documento descreve as regras de negócio, alterações no banco de dados, mapeamentos de entidades JPA/Spring e os novos endpoints necessários para dar suporte às novas funcionalidades do aplicativo Android.

> **Nota de Integração:** As telas, animações, botões e a interface do front-end já estão implementadas e aguardando apenas a disponibilidade destes endpoints para integração.

> ✅ **Status (22/10/2026):** as 3 tasks foram implementadas na branch `Ajuste-Backend`. Veja a seção
> [Resultado da implementação](#-resultado-da-implementação) no final para os detalhes e diferenças em relação à sugestão.

---

## 📊 Resumo Executivo das Tasks

| # | Funcionalidade | Tipo de Alteração | Endpoint(s) Envolvido(s) | Prioridade | Status |
|---|---|---|---|---|---|
| 1 | **Alterar Senha (Logado)** | Novo Endpoint | `PUT /api/clientes/{id}/senha` / `PUT /api/auth/alterar-senha` | 🔴 Alta | ✅ |
| 2 | **Meus Cupons** | Novo Endpoint | `GET /api/cupons/ativos` | 🟡 Média | ✅ |
| 3 | **Avaliar Produto** | Tabela + DTOs + Endpoints | `POST /api/produtos/{id}/avaliacoes`<br>`GET /api/produtos/{id}/avaliacoes` | 🟡 Média | ✅ |

---

# 🔴 Task 1: Alterar Senha (Usuário Logado)

## Contexto
O app já possui a funcionalidade de "Recuperar Senha" (envio de token de redefinição por e-mail). Esta nova task provê um endpoint seguro para que o cliente **já autenticado** altere sua senha a partir da tela de Perfil.

## Endpoint
- **URL:** `PUT /api/clientes/{id}/senha` ou `PUT /api/auth/alterar-senha`
- **Autenticação:** Requerida (Bearer JWT)

## Payload e Respostas

### Request Body (`application/json`)
```json
{
  "senhaAtual": "admin123",
  "novaSenha": "novaSenha123"
}
```

### Response Success (200 OK)
```json
{
  "mensagem": "Senha alterada com sucesso."
}
```

### Response Error (400 Bad Request — Senha Incorreta)
```json
{
  "erro": "Senha atual incorreta"
}
```

### Response Error (400 Bad Request — Requisitos Não Atendidos)
```json
{
  "erro": "A nova senha deve possuir no mínimo 4 caracteres."
}
```

---

## Regras de Negócio
1. **Validação de Usuário:** O `id` do cliente deve ser validado no banco de dados e/ou extraído diretamente do contexto do JWT via `@AuthenticationPrincipal`.
2. **Validação de Senha Atual:** Comparar a `senhaAtual` recebida no payload com o hash armazenado no banco utilizando `passwordEncoder.matches(senhaAtual, usuario.getSenha())`.
3. **Erros de Validação:** Caso a senha atual não coincida, interromper a execução e retornar `HTTP 400 Bad Request` com a mensagem `"Senha atual incorreta"`.
4. **Critérios Mínimos:** Validar se a `novaSenha` atende aos critérios do sistema (ex: mínimo 4 caracteres, não nula e não vazia).
5. **Persistência Segura:** Criptografar a nova senha usando `passwordEncoder.encode(novaSenha)` antes de atualizar a entidade no banco de dados.

---

# 🟡 Task 2: Meus Cupons

## Contexto
Exibir uma listagem com os cupons válidos e disponíveis no aplicativo para consulta do cliente antes do checkout.

## Endpoint
- **URL:** `GET /api/cupons/ativos`
- **Autenticação:** Público ou Autenticado (conforme política da aplicação)

### Response Success (200 OK)
```json
[
  {
    "codigo": "ARTHUR10",
    "descricao": "10% de desconto em todo o site",
    "tipo": "PERCENTUAL",
    "valor": 10.0,
    "validoAte": "2026-12-31T23:59:59"
  },
  {
    "codigo": "FRETEGRATIS",
    "descricao": "Frete grátis na primeira compra",
    "tipo": "FRETE_GRATIS",
    "valor": 0,
    "validoAte": null
  }
]
```

## Regras de Negócio
1. **Filtro de Status:** Retornar exclusivamente registros onde `ativo = true`.
2. **Filtro de Data Limite:** Ignorar cupons cuja data `validoAte` seja anterior ao timestamp atual (`validoAte < NOW()`). Se `validoAte` for `NULL`, o cupom não possui expiração por data.
3. **Filtro de Data Inicial:** Ignorar cupons cuja data `validoDe` seja posterior ao timestamp atual (`validoDe > NOW()`).

---

# 🟡 Task 3: Avaliar Produto

## Contexto
Permitir que clientes logados avaliem produtos comprados enviando nota (1 a 5 estrelas) e comentário. O sistema armazena o histórico de avaliações e calcula dinamicamente a média das notas para exibição no detalhe do produto.

## 3.1 Alterações no Banco de Dados (DDL)

```sql
CREATE TABLE avaliacao (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    produto_id BIGINT UNSIGNED NOT NULL,
    cliente_id BIGINT UNSIGNED NOT NULL,
    nota INT NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario TEXT,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avaliacao_produto FOREIGN KEY (produto_id) REFERENCES produto(id) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacao_cliente FOREIGN KEY (cliente_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT uk_cliente_produto UNIQUE (cliente_id, produto_id)
);
```

## 3.2 Endpoints

### 3.2.1 Criar Avaliação
- **URL:** `POST /api/produtos/{produtoId}/avaliacoes`
- **Autenticação:** Requerida (Bearer JWT)

#### Request Body (`application/json`)
```json
{
  "clienteId": 8,
  "nota": 5,
  "comentario": "Tecido muito macio, amei!"
}
```

#### Response Success (201 Created)
```json
{
  "id": 1,
  "nota": 5,
  "comentario": "Tecido muito macio, amei!",
  "clienteNome": "Maria Silva",
  "criadoEm": "2026-10-19T10:00:00"
}
```

### 3.2.2 Listar Avaliações por Produto
- **URL:** `GET /api/produtos/{produtoId}/avaliacoes`
- **Autenticação:** Público
- **Resposta:** lista no mesmo formato acima, da mais recente para a mais antiga.

## 3.3 Regras de Negócio
1. **Validação de Existência:** Validar se `clienteId` e `produtoId` existem no banco.
2. **Validação do Intervalo da Nota:** Garantir que a nota esteja estritamente entre `1` e `5`.
3. **Unicidade de Avaliação:** Um cliente pode avaliar o mesmo produto apenas **uma única vez** (garantido por `UNIQUE KEY` e verificação no Service).
4. **Validação de Compra Anterior (Recomendada):** Verificar se o cliente possui algum pedido no status `ENTREGUE` contendo o `produtoId` antes de permitir o envio da avaliação.

## 3.4 Atualização de Endpoint Existente: `GET /api/produtos/{id}`

O campo `avaliacao` (já existente no DTO de detalhe do produto) deve ser recalculado dinamicamente no banco através da média das notas:

- **Com avaliações:** Retornar a média com precisão decimal (ex: `4.5`).
- **Sem avaliações:** Retornar `0.0`.

---

## 📋 Checklist de Implementação Backend

- [x] **Task 1: Alterar Senha**
    - [x] Criar DTO `AlterarSenhaRequest` (validação feita no service — o projeto não usa `spring-boot-starter-validation`).
    - [x] Implementar verificação de senha atual com `PasswordEncoder`.
    - [x] Criar testes unitários para cenários de sucesso, senha incorreta e validação de tamanho.

- [x] **Task 2: Meus Cupons**
    - [x] Adicionar query customizada no `CupomRepository` filtrando cupons ativos e vigentes.
    - [x] Mapear o DTO de resposta `CupomResponse`.
    - [x] Testar cenários com cupons expirados, inativos e sem data limite (`NULL`).

- [x] **Task 3: Avaliar Produto**
    - [x] Tabela `avaliacao` (criada pelo Hibernate ao subir a API; DDL também em `database/arthurbaby_schema.sql`).
    - [x] Mapear a entidade `Avaliacao` e repository correspondente.
    - [x] Criar endpoints `POST` e `GET` de avaliações.
    - [x] Atualizar `GET /api/produtos/{id}` para devolver a média calculada via `AVG()`.

---

## ✅ Resultado da implementação

| Item | Onde |
|---|---|
| Alterar senha | `ClienteService#alterarSenha`, `AuthController` (`PUT /api/auth/alterar-senha`, id do token) e `ClienteController` (`PUT /api/clientes/{id}/senha`) |
| Cupons ativos | `CupomRepository#findCuponsAtivos`, `CupomService#listarAtivos`, `CupomController` (`GET /api/cupons/ativos`, público) |
| Avaliações | `entity/Avaliacao`, `AvaliacaoRepository`, `AvaliacaoService`, `AvaliacaoController` |
| Média no detalhe | `CatalogoService#detalhe` |
| MySQL (ids `BIGINT UNSIGNED`) | `SchemaMigration` alinha `avaliacao.produto_id/cliente_id` e cria as FKs com `ON DELETE CASCADE` |

Decisões e observações:

- **Os dois endpoints de senha existem.** Ambos devolvem `{"mensagem": "Senha alterada com sucesso."}`. No `/api/clientes/{id}/senha` vale a mesma regra de acesso dos outros endpoints de cliente (cliente só altera a própria conta).
- **Erros** seguem o padrão da API: `{"erro": "...", "codigo": 400}`.
- **Cupons**: a lista vem ordenada pelos que expiram primeiro; os sem data limite ficam por último. `valor` sai `0` quando o cupom não tem valor (ex.: `FRETE_GRATIS`). O subtotal mínimo não filtra a lista, porque depende do carrinho; ele continua sendo conferido em `POST /api/cupons/validar` e na criação do pedido.
- **Avaliação**: `clienteId` no corpo é opcional; se vier, precisa ser o do token (senão `403`). Outros erros: nota fora de 1–5 → `400`; já avaliou → `400 "Voce ja avaliou este produto"`; sem pedido `ENTREGUE` com o produto → `400 "So e possivel avaliar produtos de pedidos entregues"`; produto inexistente → `404`.
- **Média**: o detalhe calcula a média na hora. Ao criar uma avaliação, a média também é gravada em `produto.avaliacao`, para a listagem `GET /api/produtos` (vitrine) mostrar o mesmo valor.
- **Datas** de `validoAte` e `criadoEm` saem sem milissegundos (`2026-10-19T10:00:00`), conforme este contrato. O restante da API continua com `.SSS`.
- ⚠️ **Atenção no app:** hoje `network/dto/ProdutoResponse.avaliacao` é `int`. Com médias como `4.5`, o Gson falha ao converter. É preciso trocar para `double` (ou `Double`) antes de integrar.
