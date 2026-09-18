# Pendências do Backend — ArthurBabyAPI

Documento gerado a partir da análise do front-end Android (app cliente).
Lista o que **falta implementar** ou **está diferente** do que o app espera.

> Baseado na análise de: Controllers, Services, DTOs, Entities, Security, application.properties

---

## 🔴 CRÍTICO — O app não funciona sem isso

### 1. Cadastro não salva endereço

**Problema:** o `CadastroRequest` não tem campo `endereco`. O app coleta endereço completo
(CEP, logradouro, número, complemento, bairro, cidade, UF) e envia, mas o back ignora.

**Solução esperada:**
- Adicionar campo `endereco` no `CadastroRequest`:
```java
public record EnderecoRequest(
    String cep, String logradouro, String numero, String complemento,
    String bairro, String cidade, String uf
) {}

public record CadastroRequest(
    String nomeCompleto, String email, String cpf, String telefone, String senha,
    boolean aceiteTermoUso, boolean aceiteLgpd,
    EnderecoRequest endereco  // ← novo campo
) {}
```
- No `AuthService.cadastrar()`, salvar o endereço associado ao usuário.

---

### 2. Falta endpoint de recuperar senha

**O app usa:** `POST /api/auth/recuperar-senha`
**O back tem:** ❌ não existe

**Solução esperada:**
- Criar endpoint que recebe `{ "email": "..." }`
- Gerar token de reset
- Enviar e-mail (ou simular em dev)
- Retornar `204 No Content`

---

### 3. Falta campo `cupom` no pedido

**O app envia:** `{ "cupom": "ARTHUR10", "desconto": 17.98, ... }`
**O back espera:** apenas `desconto` (sem validar cupom)

**Solução esperada:**
- Adicionar campo `cupom` no `PedidoRequest`
- Criar tabela/regra de cupons válidos (ex: `ARTHUR10` = 10% off)
- Validar e calcular o desconto no `PedidoService`

---

### 4. Falta endpoint de cancelamento separado

**O app usa:** `PUT /api/pedidos/{numero}/cancelar`
**O back tem:** `PUT /api/pedidos/{id}/status` com `status=CANCELADO`

**Solução esperada:**
- Ou criar endpoint dedicado
- Ou documentar que o app deve usar o endpoint genérico com `motivoCancelamento`

---

### 5. Buscar pedido por número, não por ID

**O app usa:** `GET /api/pedidos/{numero}` (ex: `GET /api/pedidos/AB20250918143025`)
**O back tem:** `GET /api/pedidos/{id}` (busca por ID numérico)

**Solução esperada:**
- Adicionar no `PedidoRepository`:
```java
Optional<Pedido> findByNumeroPedido(String numeroPedido);
```
- Adicionar endpoint `GET /api/pedidos/numero/{numeroPedido}`

---

### 6. Bug no cálculo de desconto

**Problema:** no `PedidoService.criar()`:
```java
BigDecimal totalItem = valorUnitario.multiply(BigDecimal.valueOf(quantidade)).subtract(descontoItem);
// ...
pedido.setTotal(subtotal.subtract(pedido.getDesconto()).add(pedido.getFrete()));
```

O desconto é aplicado **por item** e depois **de novo** no total. Pode duplicar.

**Solução esperada:**
- Definir claramente: ou o desconto é por item, ou é global
- Se global (cupom), o item **não deve ter desconto**
- Se por item, o total **não deve subtrair de novo**

---

## 🟡 IMPORTANTE — Afeta a experiência do app

### 7. Subcategorias não retornam no endpoint de categorias

**O que o app espera:**
```json
[
  {
    "id": 1,
    "nome": "Enxoval",
    "icone": "enxoval",
    "subcategorias": [
      { "id": 11, "nome": "Kit Berço" }
    ]
  }
]
```

**O back retorna:** lista plana, sem subcategorias nem campo `icone`.

**Solução esperada:**
- Criar um `CategoriaResponse` com lista de subcategorias
- Montar a árvore no `PublicCatalogController`
- Adicionar campo `icone` (String) na entidade `Categoria`

---

### 8. Faltam variações, imagens e avaliação no detalhe do produto

**O que o app espera** em `GET /api/produtos/{id}`:
```json
{
  "id": 1,
  "nome": "...",
  "imagens": [{ "url": "...", "principal": true }],
  "variacoes": [
    { "id": 10, "tamanho": "M", "cor": "Rosa", "modelo": "Padrão", "estoqueAtual": 5 }
  ],
  "avaliacao": 5.0,
  "marca": "ArthurBaby"
}
```

**O back retorna:** sem imagens, sem variações, sem avaliação.

**Solução esperada:**
- Criar `ProdutoDetalheResponse` que inclui imagens, variações, avaliação
- Adicionar campo `avaliacao` (BigDecimal) na entidade `Produto` (ou vir de outra tabela)

---

### 9. Faltam endpoints de endereço

**O app usa:**
- `GET /api/clientes/{clienteId}/enderecos`
- `POST /api/clientes/{clienteId}/enderecos`
- `DELETE /api/clientes/{clienteId}/enderecos/{enderecoId}`

**O back tem:** ❌ não existe `EnderecoController`

**Solução esperada:**
- Criar `EnderecoController` com as 3 rotas acima
- Adicionar no `EnderecoRepository`:
```java
List<Endereco> findByUsuarioId(Long usuarioId);
```

---

### 10. Filtro `promocao=true` não existe

**O app usa:** `GET /api/produtos?promocao=true`
**O back tem:** filtros de `q`, `categoriaId`, `precoMin`, `precoMax` (sem promoção)

**Solução esperada:**
- Adicionar filtro `promocao` no `PublicCatalogController`:
```java
if (Boolean.TRUE.equals(promocao)) {
    spec = spec.and((root, query, cb) -> cb.isTrue(root.get("promocao")));
}
```

---

## 🟢 DESEJÁVEL — Melhora robustez

### 11. Tratamento de erros incompleto

**Hoje o `ApiExceptionHandler` só trata:**
- `IllegalArgumentException` → 400
- `MethodArgumentNotValidException` → 400

**Faltam:**
- `NoSuchElementException` → 404
- `AccessDeniedException` → 403
- Erros genéricos → 500

**Solução esperada:**
- Adicionar handlers no `ApiExceptionHandler`

---

### 12. Validação de CPF no backend

**O front valida CPF (dígitos verificadores), mas o back aceita qualquer string.**

**Solução esperada:**
- Adicionar validação no `AuthService.cadastrar()`:
```java
if (!validarCpf(request.cpf())) throw new IllegalArgumentException("CPF inválido");
```

---

### 13. Normalização de e-mail

**Problema:** `TESTE@x.com` e `teste@x.com` viram usuários diferentes.

**Solução esperada:**
- No login e cadastro: `email.toLowerCase().trim()`

---

### 14. `findByCategoriaPaiId` no CategoriaRepository

**Necessário para listar subcategorias de uma categoria.**

```java
List<Categoria> findByCategoriaPaiId(Long categoriaPaiId);
```

---

### 15. Entrada/ajuste/estorno de estoque

**Hoje o `EstoqueService` só tem `baixarEstoque()`.**

**Falta:**
- `adicionarEstoque()` — para entrada
- `ajustarEstoque()` — para correção manual
- `estornarEstoque()` — ao cancelar pedido

---

## 📋 Resumo executivo

| # | Item | Prioridade | Complexidade |
|---|---|---|---|
| 1 | Endereço no cadastro | 🔴 Crítico | Baixa |
| 2 | Recuperar senha | 🔴 Crítico | Média |
| 3 | Cupom no pedido | 🔴 Crítico | Média |
| 4 | Cancelamento separado | 🔴 Crítico | Baixa |
| 5 | Buscar por `numeroPedido` | 🔴 Crítico | Baixa |
| 6 | Bug do desconto duplicado | 🔴 Crítico | Baixa |
| 7 | Subcategorias no response | 🟡 Importante | Média |
| 8 | Variações/imagens/avaliação | 🟡 Importante | Média |
| 9 | Endpoints de endereço | 🟡 Importante | Média |
| 10 | Filtro `promocao=true` | 🟡 Importante | Baixa |
| 11 | Tratamento 401/403/404/500 | 🟢 Desejável | Baixa |
| 12 | Validação de CPF | 🟢 Desejável | Baixa |
| 13 | Normalizar e-mail | 🟢 Desejável | Baixa |
| 14 | `findByCategoriaPaiId` | 🟢 Desejável | Baixa |
| 15 | Entrada/ajuste/estorno estoque | 🟢 Desejável | Média |

---

*Documento gerado em 18/09/2025 — análise do front Android vs backend Spring Boot atual.*