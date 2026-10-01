# Pendências do Backend — Documento Consolidado Final

Documento único com **todas as pendências do backend** identificadas após
a integração completa do app Android e do painel admin web.

> **Status:** o time de back já resolveu a maioria das pendências anteriores
> (cupom, recuperar senha, endereços, favoritos, editar perfil).
> Este documento lista **apenas o que ainda falta**.

---

## 📊 Resumo executivo

| # | Pendência | Prioridade | Impacto |
|---|---|---|---|
| 1 | LazyInitializationException definitivo (DTOs) | 🔴 Crítica | App + Painel Admin |
| 2 | Campo `icone` nas categorias | 🔴 Crítica | Ícones coloridos no app |
| 3 | Formatação de datas no JSON | 🟡 Importante | Parse de datas no app |
| 4 | Seed incompleto (modelos, imagens, avaliação) | 🟡 Importante | Detalhe do produto vazio |
| 5 | Normalização de e-mail no login/cadastro | 🟢 Desejável | Duplicidade de contas |

---

# 🔴 1. LazyInitializationException — solução definitiva

## Problema

Vários endpoints retornam **HTTP 500** com o erro:

```text
org.springframework.http.converter.HttpMessageNotWritableException:
Could not write JSON: failed to lazily initialize a collection of role:
br.com.arthurbaby.entity.Produto.imagens: could not initialize proxy - no Session
```

## Endpoints afetados

- ❌ `GET /api/admin/produtos` — listar produtos (painel admin)
- ❌ `GET /api/admin/{tipo}` — qualquer listagem de entidade com relacionamentos
- ❌ `POST /api/admin/produtos/completo` — pode dar erro ao retornar
- ⚠️ `GET /api/favoritos/cliente/{id}` — resolvido com URL nova? precisa confirmar
- ⚠️ `GET /api/pedidos/{id}` — provavelmente mesmo problema

## Causa

O `AdminCrudController` retorna **entidades JPA diretamente**, e essas entidades
têm relacionamentos LAZY (`@OneToMany`). Quando o Jackson serializa para JSON,
a sessão do JPA já fechou.

## Solução definitiva

Criar **DTOs** para todos os endpoints do `AdminCrudController`:

```java
public record ProdutoAdminResponse(
    Long id, String codigo, String sku, String nome, String descricao,
    BigDecimal preco, BigDecimal precoPromocional, boolean promocao,
    boolean destaque, String status,
    Long categoriaId, String categoriaNome,
    Long marcaId, String marcaNome,
    int avaliacao,
    List<ImagemResponse> imagens,
    List<VariacaoResponse> variacoes
) {}

public record ImagemResponse(Long id, String url, String descricao, boolean principal) {}

public record VariacaoResponse(
    Long id, String sku, String tamanho, String cor, String modelo,
    BigDecimal preco, int estoqueAtual
) {}
```

E no controller, mapear:

```java
@GetMapping("/{tipo}")
public List<?> listar(@PathVariable String tipo) {
    if ("produtos".equals(tipo)) {
        return produtoRepository.findAll().stream()
            .map(this::toProdutoResponse)
            .toList();
    }
    // ... etc
}
```

## Solução temporária aplicada

Se ainda estiver em uso:

```properties
spring.jpa.open-in-view=true
```

⚠️ **NÃO usar em produção.**

---

# 🔴 2. Campo `icone` nas categorias

## Problema

O app mobile mostra **ícones coloridos por categoria** (urso, chupeta, mamadeira, etc).
O app tem os desenhos **hardcoded** e precisa saber **qual ícone** usar para cada categoria.

Hoje, o app mapeia pelo **nome da categoria**, o que é frágil:

- Se o admin renomear "Enxoval" para "Enxoval Completo", o ícone quebra
- Categorias novas não têm ícone

## Solução

Adicionar campo `icone` (String) na entidade `Categoria`:

```java
@Entity
public class Categoria {
    // ... campos existentes
    private String icone;  // ← NOVO
}
```

E retornar no `GET /api/categorias`:

```json
{
  "id": 1,
  "nome": "Enxoval",
  "icone": "enxoval",
  "subcategorias": []
}
```

## Valores esperados de `icone`

- `enxoval`
- `roupas_para_bebes`
- `roupas_infantis`
- `acessorios`
- `higiene_e_cuidados`
- `alimentacao`
- `quarto_do_bebe`
- `presentes`
- `kits`
- `promocoes`

## Seed sugerido

```sql
UPDATE categoria SET icone = 'enxoval' WHERE nome = 'Enxoval';
UPDATE categoria SET icone = 'roupas_para_bebes' WHERE nome = 'Roupas para Bebes';
UPDATE categoria SET icone = 'roupas_infantis' WHERE nome = 'Roupas Infantis';
UPDATE categoria SET icone = 'acessorios' WHERE nome = 'Acessorios';
UPDATE categoria SET icone = 'higiene_e_cuidados' WHERE nome = 'Higiene e Cuidados';
UPDATE categoria SET icone = 'alimentacao' WHERE nome = 'Alimentacao';
UPDATE categoria SET icone = 'quarto_do_bebe' WHERE nome = 'Quarto do Bebe';
UPDATE categoria SET icone = 'presentes' WHERE nome = 'Presentes';
UPDATE categoria SET icone = 'kits' WHERE nome = 'Kits';
UPDATE categoria SET icone = 'promocoes' WHERE nome = 'Promocoes';
```

---

# 🟡 3. Formatação de datas no JSON

## Problema

O back retorna datas em **formato ISO com nanossegundos**:

```json
{
  "data": "2026-09-23T11:02:00.9942499"
}
```

**7 dígitos** na fração de segundo. O Android (Java) + Gson aceita até
**3 dígitos** (milissegundos). O app precisou fazer **parse manual** para não quebrar.

## Solução

Configurar o Jackson no `application.properties`:

```properties
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss.SSS
spring.jackson.serialization.write-dates-as-timestamps=false
```

Ou usar `@JsonFormat` nos DTOs:

```java
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
private LocalDateTime data;
```

---

# 🟡 4. Seed incompleto

## Problema

O `DataInitializer` cadastra **cor** e **tamanho** para os produtos, mas **não cadastra**:

### 4.1 — Modelos

**Problema:** a tabela `modelo` existe, mas está vazia. As variações não vinculam modelo.

**Impacto:** o app mobile não mostra a seção "Modelo" no detalhe do produto.

**Solução no `DataInitializer`:**

```java
if (modelos.count() == 0) {
    for (String nome : List.of("Padrão", "Premium", "Deluxe")) {
        Modelo m = new Modelo();
        m.setNome(nome);
        modelos.save(m);
    }
}

// E nas variações:
v.setModelo(modelos.findAll().get(0));
```

### 4.2 — Imagens do produto

**Problema:** a tabela `produto_imagem` está vazia.

**Impacto:** o app mobile mostra o **placeholder rosa** em vez da foto do produto.

**Solução no `DataInitializer`:**

```java
if (imagens.count() == 0) {
    ProdutoImagem img = new ProdutoImagem();
    img.setProduto(p);
    img.setUrl("https://placehold.co/400x400/ED83A4/FFFFFF?text=Produto");
    img.setPrincipal(true);
    img.setOrdemExibicao(1);
    imagens.save(img);
}
```

### 4.3 — Avaliação do produto

**Problema:** a coluna `avaliacao` existe em `produto`, mas não está preenchida.

**Solução:** definir `p.setAvaliacao(5)` ao criar o produto.

---

# 🟢 5. Normalização de e-mail no login/cadastro

## Problema

`TESTE@x.com` e `teste@x.com` são tratados como **usuários diferentes**.

## Solução

No `AuthService`:

```java
public AuthResponse login(LoginRequest request) {
    String login = request.login().toLowerCase().trim();
    Usuario usuario = usuarios.findByEmail(login)
            .or(() -> usuarios.findByCpf(login))
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    // ...
}

public AuthResponse cadastrar(CadastroRequest request) {
    String email = request.email().toLowerCase().trim();
    if (usuarios.existsByEmail(email))
        throw new IllegalArgumentException("E-mail já cadastrado");
    usuario.setEmail(email);
    // ...
}
```

---

## 🎯 Resumo de prioridades

| **#** | **Pendência** | **Prioridade** | **Tempo estimado** |
| :--- | :--- | :--- | :--- |
| 1 | LazyInit definitivo (DTOs) | 🔴 Crítica | 1-2h |
| 2 | Campo `icone` nas categorias | 🔴 Crítica | 15 min |
| 3 | Formatação de datas | 🟡 Importante | 10 min |
| 4 | Seed (modelos, imagens, avaliação) | 🟡 Importante | 30 min |
| 5 | Normalização de e-mail | 🟢 Desejável | 10 min |

**Total:** ~2-3 horas de trabalho.

---

## 📌 Como testar cada um

### Após #1 (DTOs)

```bash
curl -X GET http://localhost:8080/api/admin/produtos \
  -H "Authorization: Bearer SEU_TOKEN"

# Deve retornar 200 com lista de produtos
```

### Após #2 (icone)

```bash
curl -X GET http://localhost:8080/api/categorias

# Deve retornar categorias com campo "icone"
```

### Após #3 (datas)

```bash
curl -X GET http://localhost:8080/api/pedidos/cliente/8 \
  -H "Authorization: Bearer SEU_TOKEN"

# Datas devem estar no formato "2026-09-23T11:02:00.994"
```

### Após #4 (seed)

- Verifique no MySQL: `SELECT * FROM modelo;` (deve ter 3 registros)
- `SELECT * FROM produto_imagem;` (deve ter pelo menos 1 registro)
- `SELECT avaliacao FROM produto;` (não deve ser 0)

### Após #5 (email)

- Cadastre com `TESTE@x.com`
- Tente logar com `teste@x.com` → deve funcionar

---

*Documento consolidado em 01/10/2026 — Pendências finais após integração completa.*
