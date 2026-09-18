# Ajustes no Front Android — ArthurBabyApp

Lista de mudanças necessárias no app para **bater com o backend** atual.
Cada item indica o arquivo e o que mudar.

---

## 🔴 CRÍTICO — Ajustes que impedem a integração

### 1. Adaptar `AuthResponse` (login/cadastro)

**O que o back devolve:**
```json
{
  "token": "...",
  "usuarioId": 2,
  "nome": "Cliente Teste",
  "perfil": "CLIENTE"
}
```

**O que o front esperava:**
```json
{
  "token": "...",
  "usuario": { "id": 2, "nome": "...", "email": "...", "cpf": "...", "telefone": "..." }
}
```

**Ação:**
- Criar classe `AuthResponse` que reflita o back:
```java
public class AuthResponse {
    public String token;
    public Long usuarioId;
    public String nome;
    public String perfil;
}
```
- No `LoginActivity`, salvar apenas `token`, `usuarioId`, `nome`, `perfil` no `TokenStorage`
- **Não esperar** `email`, `cpf`, `telefone` no retorno do login (buscar depois em `/api/auth/me` se necessário)

---

### 2. Enviar `nomeCompleto` em vez de `nome` no cadastro

**Arquivo:** `CadastroActivity.java`

**O que o back espera:**
```json
{
  "nomeCompleto": "Maria Silva",
  "email": "...",
  "cpf": "...",
  "telefone": "...",
  "senha": "...",
  "aceiteTermoUso": true,
  "aceiteLgpd": true
}
```

**Ação:**
- No JSON de cadastro, usar chave `nomeCompleto` (não `nome`)

---

### 3. Ajustar Favoritos

**O back tem:**
- `GET /api/favoritos/cliente/{clienteId}` (com ID na URL)
- `POST /api/favoritos` com corpo `{ clienteId, produtoId }`
- `DELETE /api/favoritos` com corpo `{ clienteId, produtoId }`

**O front esperava:**
- `GET /api/favoritos` (sem ID, usando token)
- `POST /api/favoritos/{produtoId}`
- `DELETE /api/favoritos/{produtoId}`

**Ação:**
- No `FavoritoRepository` (front), sempre enviar `clienteId` (do `TokenStorage`) e `produtoId` no corpo
- Trocar as chamadas

---

### 4. Ajustar criação de Pedido

**O back espera:**
```json
{
  "clienteId": 2,
  "formaRecebimento": "ENTREGA",
  "desconto": 17.98,
  "frete": 15.00,
  "observacao": "...",
  "itens": [
    { "produtoId": 1, "variacaoId": 1, "quantidade": 2, "desconto": 0 }
  ]
}
```

**Ação:**
- Enviar `clienteId` no corpo (pegar do `TokenStorage`)
- Manter `formaRecebimento`, `desconto`, `frete`, `observacao`, `itens`
- **Atenção ao bug do desconto no back** (ver pendência #6) — por enquanto, envie `desconto: 0` em cada item e o desconto global em `desconto`

---

### 5. Buscar pedido por `numeroPedido`

**O back devolve:** `numeroPedido` (ex: `AB20250918143025`)
**O front esperava:** `numero`

**Ação:**
- No `Pedido.java` (front), mudar o campo `numero` para `numeroPedido`
- Usar `GET /api/pedidos/numero/{numeroPedido}` (quando o back implementar)

---

### 6. Trocar `motivo` por `motivoCancelamento` no status

**O back espera:**
```json
{
  "status": "CANCELADO",
  "motivoCancelamento": "Cliente desistiu",
  "usuarioId": 1
}
```

**Ação:**
- No front, enviar `motivoCancelamento` (não `motivo`)
- **Sempre enviar `usuarioId`** (pegar do `TokenStorage`)

---

### 7. Trocar `busca` por `q` na pesquisa

**O front enviava:** `GET /api/produtos?busca=...`
**O back espera:** `GET /api/produtos?q=...`

**Ação:**
- No `PesquisaFragment`, chamar com `q` em vez de `busca`

---

## 🟡 IMPORTANTE — Ajustes que melhoram a integração

### 8. Categoria sem subcategorias (por enquanto)

**O back retorna:** categoria plana (sem subcategorias, sem ícone)

**Ação:**
- Manter o `MockData` de subcategorias **até o back implementar** (pendência #7)
- Ou exibir apenas categorias planas por enquanto

---

### 9. Produto sem variações, imagens e avaliação

**O back retorna:** produto com `nome`, `preco`, `descricao`, `categoria`, `marca`

**Ação:**
- Substituir o placeholder de imagem por `Glide.with(...).load(null)` → mostra cor de fundo
- Deixar variações mockadas até o back implementar (pendência #8)
- Esconder estrelas se `avaliacao` não vier

---

### 10. Remover mock do ViaCEP (opcional)

**Observação:** o app usa ViaCEP (API externa) diretamente, **não é o back**. Isso pode continuar.

**Ação:** nenhuma.

---

## 🟢 DESEJÁVEL — Ajustes futuros

### 11. Adicionar `GET /api/auth/me`

**O back não tem endpoint para buscar dados do usuário logado.**

**Ação (quando o back implementar):**
- Criar método no `TokenStorage` para atualizar dados do usuário logado

---

### 12. Carrinho persistente

**Hoje o carrinho é em memória. Não muda nada na integração com o back atual.**

**Ação:** nenhuma por enquanto.

---

## 📋 Checklist de mudanças no app

| # | O que | Onde | Status |
|---|---|---|---|
| 1 | Adaptar `AuthResponse` | `network/AuthResponse.java` (novo) | ⏳ |
| 2 | `LoginActivity` — parse do back | `LoginActivity.java` | ⏳ |
| 3 | `CadastroActivity` — `nomeCompleto` | `CadastroActivity.java` | ⏳ |
| 4 | Favoritos — `clienteId` no corpo | `FavoritoRepository.java` (front) | ⏳ |
| 5 | Pedido — `clienteId` no corpo | `CarrinhoFragment.java` | ⏳ |
| 6 | `Pedido.numeroPedido` | `models/Pedido.java` | ⏳ |
| 7 | Cancelamento — `motivoCancelamento` | `DetalhePedidoFragment.java` | ⏳ |
| 8 | Pesquisa — parâmetro `q` | `PesquisaFragment.java` | ⏳ |
| 9 | Esconder estrelas se sem avaliação | `ProdutoAdapter.java` | ⏳ |
| 10 | Esconder subcategorias mock | `HomeFragment.java`, `CategoriasFragment.java` | ⏳ |

---

*Documento gerado em 18/09/2025 — ajustes necessários no app Android para integração com o backend.*