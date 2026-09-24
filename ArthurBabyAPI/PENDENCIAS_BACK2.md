# Pendências do Backend 2

Documento único com **todas as pendências do backend** identificadas após a integração completa do app Android.

## 📊 Resumo executivo

| \# | Pendência | Prioridade | Impacto | 
 | ----- | ----- | ----- | ----- | 
| 1 | Erro 500 nos favoritos (LazyInitialization) | 🔴 Crítica | Favoritos quebrados | 
| 2 | Bug do recuperar senha (retorna bcrypt) | 🔴 Crítica | Recuperação de senha quebrada | 
| 3 | Sistema de cupons dinâmico | 🔴 Crítica | Cupom hardcoded no front | 
| 4 | Recalcular frete/desconto/total no back | 🔴 Crítica | Falha de segurança financeira | 
| 5 | Seed incompleto (modelos, imagens, avaliação) | 🟡 Importante | Detalhe do produto incompleto | 
| 6 | Criar `GET /api/auth/me` | 🟡 Importante | Perfil incompleto | 
| 7 | Tratamento de erros 401/403/404/500 | 🟡 Importante | UX ruim em erros | 
| 8 | Endpoint de cadastro completo de produto | 🟢 Desejável | Painel admin futuro | 
| 9 | Normalizar e-mail no login/cadastro | 🟢 Desejável | Duplicidade de contas | 
| 10 | Endpoints de estoque (entrada/ajuste/estorno) | 🟢 Desejável | Painel admin futuro | 

# 🔴 1. Erro 500 nos favoritos — `LazyInitializationException`

## Problema

`GET /api/favoritos/cliente/{id}` e `POST /api/favoritos` retornam **HTTP 500**.

**Stack trace:**

```
org.springframework.http.converter.HttpMessageNotWritableException: 
Could not write JSON: failed to lazily initialize a collection of role: 
br.com.arthurbaby.entity.Usuario.enderecos: could not initialize proxy - no Session

```

**Cadeia do erro:**

```
List<Favorito> → Favorito.cliente (Usuario) → Usuario.enderecos (LAZY) → ❌

```

## Causa

O `FavoritoController` retorna a **entidade `Favorito` diretamente**. Ao serializar para JSON, o Jackson tenta acessar `Favorito.cliente.enderecos`, que é `LAZY`, e a sessão do JPA já fechou.

```
@GetMapping("/cliente/{clienteId}")
public List<Favorito> listar(@PathVariable Long clienteId) {
    return favoritos.findByClienteId(clienteId);
}

@PostMapping
public Favorito adicionar(@RequestBody FavoritoRequest request) {
    return favoritos.save(favorito);
}

```

## Solução — Usar DTO

Criar um `FavoritoResponse` que expõe apenas o necessário:

```
public record FavoritoResponse(Long id, ProdutoResumo produto) {}

public record ProdutoResumo(
    Long id, 
    String nome, 
    String descricao,
    BigDecimal preco, 
    BigDecimal precoPromocional,
    boolean promocao, 
    boolean destaque,
    String categoriaNome, 
    String marca
) {}

```

Alterar o controller:

```
@GetMapping("/cliente/{clienteId}")
public List<FavoritoResponse> listar(@PathVariable Long clienteId) {
    return favoritos.findByClienteId(clienteId).stream()
        .map(f -> new FavoritoResponse(
            f.getId(),
            new ProdutoResumo(
                f.getProduto().getId(),
                f.getProduto().getNome(),
                f.getProduto().getDescricao(),
                f.getProduto().getPreco(),
                f.getProduto().getPrecoPromocional(),
                f.getProduto().isPromocao(),
                f.getProduto().isDestaque(),
                f.getProduto().getCategoria() != null 
                    ? f.getProduto().getCategoria().getNome() : null,
                f.getProduto().getMarca() != null 
                    ? f.getProduto().getMarca().getNome() : null
            )
        ))
        .toList();
}

@PostMapping
public FavoritoResponse adicionar(@RequestBody FavoritoRequest request) {
    Favorito favorito = favoritos.findByClienteIdAndProdutoId(
            request.clienteId(), request.produtoId())
        .orElseGet(() -> {
            Favorito f = new Favorito();
            f.setCliente(usuarios.findById(request.clienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado")));
            f.setProduto(produtos.findById(request.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));
            return favoritos.save(f);
        });

    return new FavoritoResponse(
        favorito.getId(),
        new ProdutoResumo(
            favorito.getProduto().getId(),
            favorito.getProduto().getNome(),
            favorito.getProduto().getDescricao(),
            favorito.getProduto().getPreco(),
            favorito.getProduto().getPrecoPromocional(),
            favorito.getProduto().isPromocao(),
            favorito.getProduto().isDestaque(),
            favorito.getProduto().getCategoria() != null 
                ? favorito.getProduto().getCategoria().getNome() : null,
            favorito.getProduto().getMarca() != null 
                ? favorito.getProduto().getMarca().getNome() : null
        )
    );
}

```

⚠️ **O mesmo problema afeta outros controllers**
Verificar e corrigir TODOS os controllers que retornam entidades com relacionamentos:

* `FavoritoController` — `Favorito.cliente`, `Favorito.produto`

* `PedidoController` — `Pedido.cliente`, `Pedido.itens`, `Pedido.historico`

* `PedidoItem` — `PedidoItem.produto`, `PedidoItem.variacao`

* `UsuarioController` — `Usuario.enderecos`

* `ProdutoController` — `Produto.variacoes`, `Produto.imagens`

> **Regra geral:** nunca retornar entidades JPA diretamente. Sempre usar DTOs.

# 🔴 2. Corrigir `POST /api/auth/recuperar-senha`

## Problema

O endpoint está retornando o hash BCrypt da senha do usuário no corpo da resposta, em vez de gerar um token descartável de recuperação.

**Resposta atual (ERRADA):**

```
{
  "senha": "$2a$10$VSMLGFthR87nW3Stq7rbZOsXbN.7wd4wdUz47719pA3zE0OWsms4u"
}

```

**Resposta correta esperada:**

```
{
  "mensagem": "Se o e-mail estiver cadastrado, você receberá um código."
}

```

**Impactos:**

* 🚨 Falha de segurança (hash exposto)

* 🔴 Fluxo de recuperação de senha quebrado

* 🔴 Bloqueia `POST /api/auth/redefinir-senha`

## Solução

### Fluxo correto

1. Usuário solicita → `POST /api/auth/recuperar-senha` com `{ "email": "..." }`

2. Backend gera token único (`UUID.randomUUID()`)

3. Salva em `password_reset_token` com validade (1h)

4. Envia por e-mail com link `https://arthurbaby.com/redefinir?token=abc123`

5. Em dev: imprime token no console (`System.out.println`)

6. Resposta: apenas `{ "mensagem": "..." }` — nunca o token

### Tabela

```
CREATE TABLE password_reset_token (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    usuario_id BIGINT UNSIGNED NOT NULL,
    expira_em DATETIME NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE INDEX idx_token_reset ON password_reset_token(token);

```

### Entity

```
@Entity
public class PasswordResetToken {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    @Column(nullable = false)
    private boolean usado = false;

    private LocalDateTime criadoEm;

    @PrePersist 
    void prePersist() { 
        criadoEm = LocalDateTime.now(); 
    }

    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(expiraEm);
    }
}

```

### Service

```
@Service
public class PasswordResetService {
    private final UsuarioRepository usuarios;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;

    public PasswordResetService(UsuarioRepository usuarios, 
                                PasswordResetTokenRepository tokens, 
                                PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.encoder = encoder;
    }

    @Transactional
    public void solicitarRecuperacao(String email) {
        Optional<Usuario> userOpt = usuarios.findByEmail(email.toLowerCase().trim());
        if (userOpt.isEmpty()) return; // não revela se existe

        Usuario usuario = userOpt.get();
        String token = UUID.randomUUID().toString();

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUsuario(usuario);
        prt.setExpiraEm(LocalDateTime.now().plusHours(1));

        tokens.save(prt);

        // EM DEV: imprime no console
        System.out.println("[RECUPERAR-SENHA] email=" + email + " token=" + token);
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        PasswordResetToken prt = tokens.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (prt.isUsado()) throw new IllegalArgumentException("Token já foi usado");
        if (prt.isExpirado()) throw new IllegalArgumentException("Token expirado");

        Usuario usuario = prt.getUsuario();
        usuario.setSenha(encoder.encode(novaSenha));
        usuarios.save(usuario);

        prt.setUsado(true);
        tokens.save(prt);
    }
}

```

# 🔴 3. Sistema de cupons dinâmico

## Problema

Hoje o app tem cupom hardcoded (`if ("ARTHUR10".equals(cupom))`).
Isso impede o admin de:

* Criar cupons novos sem mexer no app

* Desativar cupons antigos

* Criar cupons com regras diferentes (frete grátis, valor fixo)

* Definir validade

## Solução

### Tabela

```
CREATE TABLE cupom (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    tipo ENUM('PERCENTUAL', 'VALOR_FIXO', 'FRETE_GRATIS') NOT NULL,
    valor DECIMAL(12,2) NULL,
    valor_minimo DECIMAL(12,2) NULL,
    valor_maximo_desconto DECIMAL(12,2) NULL,
    valido_de DATETIME NULL,
    valido_ate DATETIME NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    descricao VARCHAR(200) NULL,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO cupom (codigo, tipo, valor, descricao, ativo) VALUES
  ('ARTHUR10', 'PERCENTUAL', 10.00, '10% de desconto', TRUE),
  ('FRETEGRATIS', 'FRETE_GRATIS', NULL, 'Frete grátis', TRUE),
  ('BEMVINDO', 'VALOR_FIXO', 15.00, 'R$ 15 de desconto', TRUE);

```

### Endpoint

`POST /api/cupons/validar` (público)

**Request:**

```
{
  "codigo": "ARTHUR10",
  "subtotal": 149.90
}

```

**Response 200:**

```
{
  "valido": true,
  "codigo": "ARTHUR10",
  "tipo": "PERCENTUAL",
  "descricao": "10% de desconto",
  "desconto": 14.99,
  "freteGratis": false,
  "motivo": null
}

```

### DTOs

```
public record CupomValidacaoRequest(String codigo, BigDecimal subtotal) {}

public record CupomValidacaoResponse(
    boolean valido, 
    String codigo, 
    String tipo, 
    String descricao,
    BigDecimal desconto, 
    boolean freteGratis, 
    String motivo
) {}

```

### Controller

```
@RestController
@RequestMapping("/api/cupons")
public class CupomController {
    private final CupomService service;

    public CupomController(CupomService service) {
        this.service = service;
    }

    @PostMapping("/validar")
    public CupomValidacaoResponse validar(@RequestBody CupomValidacaoRequest request) {
        return service.validar(request.codigo(), request.subtotal());
    }
}

```

# 🔴 4. Recalcular frete, desconto e total no back

## Problema

Hoje o back confia cegamente nos valores que o app envia no `POST /api/pedidos`:

```
{
  "clienteId": 8,
  "formaRecebimento": "ENTREGA",
  "cupom": "HACKEADO",
  "desconto": 9999.99,
  "frete": 0
}

```

Se um usuário mal-intencionado interceptar a requisição, pode:

* Enviar desconto maior que o subtotal (total negativo)

* Zerar o frete em uma entrega

* Usar cupom inexistente

## Solução

O back deve recalcular tudo, ignorando o que o front enviou.

### Ajuste no `PedidoService.criar()`

```
// ... após processar itens e somar subtotal ...

// 5) Calcula desconto no BACK
BigDecimal desconto = BigDecimal.ZERO;
boolean freteGratis = false;

if (request.cupom() != null && !request.cupom().isBlank()) {
    Cupom cupom = cupons.findByCodigo(request.cupom().toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Cupom inválido"));

    if (!cupom.isValido(subtotal))
        throw new IllegalArgumentException("Cupom não aplicável");

    switch (cupom.getTipo()) {
        case PERCENTUAL:
            desconto = subtotal.multiply(cupom.getValor())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            break;
        case VALOR_FIXO:
            desconto = cupom.getValor();
            break;
        case FRETE_GRATIS:
            freteGratis = true;
            break;
    }

    if (desconto.compareTo(subtotal) > 0) desconto = subtotal;

    if (cupom.getValorMaximoDesconto() != null
            && desconto.compareTo(cupom.getValorMaximoDesconto()) > 0) {
        desconto = cupom.getValorMaximoDesconto();
    }
}

pedido.setDesconto(desconto);
pedido.setCupom(request.cupom());

// 6) Calcula frete no BACK
BigDecimal frete = freteGratis ? BigDecimal.ZERO
        : calcularFrete(request.formaRecebimento(), subtotal);
pedido.setFrete(frete);

// 7) Calcula total
BigDecimal total = subtotal.subtract(desconto).add(frete);
if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
pedido.setTotal(total);

// ... registrar histórico e salvar ...

private BigDecimal calcularFrete(FormaRecebimento forma, BigDecimal subtotal) {
    if (forma == FormaRecebimento.RETIRADA_LOJA) return BigDecimal.ZERO;
    if (subtotal.compareTo(new BigDecimal("200.00")) >= 0) return BigDecimal.ZERO;
    return new BigDecimal("15.00");
}

```

# 🟡 5. Seed incompleto — modelos, imagens e avaliação

## Problema

O `DataInitializer` cadastra cor e tamanho, mas não:

* Modelos — tabela `modelo` fica vazia

* Imagens — tabela `produto_imagem` fica vazia

* Avaliação — coluna `avaliacao` em `produto` fica 0

## Solução

No `DataInitializer`:

```
// Modelos
if (modelos.count() == 0) {
    for (String nome : List.of("Padrão", "Premium", "Deluxe")) {
        Modelo m = new Modelo();
        m.setNome(nome);
        modelos.save(m);
    }
}

// Na variação
v.setModelo(modelos.findAll().get(0));

// Imagem
if (imagens.count() == 0) {
    ProdutoImagem img = new ProdutoImagem();
    img.setProduto(p);
    img.setUrl("https://placehold.co/400x400/ED83A4/FFFFFF?text=Produto");
    img.setPrincipal(true);
    img.setOrdemExibicao(1);
    imagens.save(img);
}

// Avaliação
p.setAvaliacao(5);

```

# 🟡 6. Criar `GET /api/auth/me`

## Problema

O app não tem como buscar os dados do usuário logado (email, CPF, telefone).
O Perfil só mostra nome e perfil (do `TokenStorage`).

## Solução

`GET /api/auth/me` (autenticado)

**Response 200:**

```
{
  "id": 8,
  "nomeCompleto": "Maria Teste",
  "email": "maria@teste.com",
  "cpf": "123.456.789-09",
  "telefone": "(71) 98888-7777",
  "perfil": "CLIENTE"
}

```

### Controller

```
@GetMapping("/me")
public UsuarioResponse me(Authentication auth) {
    Usuario usuario = (Usuario) auth.getPrincipal();
    return new UsuarioResponse(
        usuario.getId(), 
        usuario.getNomeCompleto(),
        usuario.getEmail(), 
        usuario.getCpf(),
        usuario.getTelefone(), 
        usuario.getPerfil().name()
    );
}

public record UsuarioResponse(
    Long id, 
    String nomeCompleto, 
    String email, 
    String cpf,
    String telefone, 
    String perfil
) {}

```

# 🟡 7. Tratamento de erros 401/403/404/500

## Problema

Hoje o `ApiExceptionHandler` só trata `IllegalArgumentException` (400).
Erros 401, 403, 404 e 500 retornam HTML feio do Spring.

## Solução

```
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Dados inválidos"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, String>> notFound(NoSuchElementException ex) {
        return ResponseEntity.status(404).body(Map.of("erro", "Recurso não encontrado"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, String>> forbidden(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(Map.of("erro", "Sem permissão"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> generic(Exception ex) {
        return ResponseEntity.status(500).body(Map.of("erro", "Erro interno do servidor"));
    }
}

```

# 🟢 8. Endpoint de cadastro completo de produto

## Problema

Hoje, para cadastrar um produto com variações e imagens, é preciso fazer 4 chamadas separadas via `/api/admin/{tipo}`.

## Solução

Criar um endpoint dedicado:
`POST /api/admin/produtos/completo`

**Request:**

```
{
  "categoriaId": 1,
  "marcaId": 1,
  "codigo": "AB-002",
  "sku": "AB-002",
  "nome": "Body Bebê Rosa",
  "descricao": "Body manga curta",
  "preco": 39.90,
  "destaque": true,
  "promocao": false,
  "imagens": [
    { "url": "...", "principal": true }
  ],
  "variacoes": [
    {
      "sku": "AB-002-P-ROSA-PAD",
      "tamanhoId": 2, 
      "corId": 3, 
      "modeloId": 1,
      "estoqueAtual": 10, 
      "preco": 39.90
    }
  ]
}

```

### DTOs

```
public record ProdutoCompletoRequest(
    Long categoriaId, 
    Long marcaId,
    String codigo, 
    String sku, 
    String nome, 
    String descricao,
    BigDecimal preco, 
    BigDecimal precoPromocional,
    boolean destaque, 
    boolean promocao,
    List<ImagemRequest> imagens,
    List<VariacaoRequest> variacoes
) {}

public record ImagemRequest(String url, boolean principal) {}

public record VariacaoRequest(
    String sku, 
    Long tamanhoId, 
    Long corId, 
    Long modeloId,
    int estoqueAtual, 
    BigDecimal preco
) {}

```

# 🟢 9. Normalizar e-mail no login/cadastro

## Problema

`TESTE@x.com` e `teste@x.com` viram usuários diferentes.

## Solução

No `AuthService`:

```
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

# 🟢 10. Endpoints de estoque (entrada/ajuste/estorno)

## Problema

O `EstoqueService` só tem `baixarEstoque()`. Falta entrada, ajuste e estorno.

## Solução

```
@Transactional
public void adicionarEstoque(Produto produto, ProdutoVariacao variacao, 
                             int quantidade, Usuario usuario, String obs) {
    if (quantidade <= 0)
        throw new IllegalArgumentException("Quantidade deve ser maior que zero");

    int anterior = variacao.getEstoqueAtual();
    variacao.setEstoqueAtual(anterior + quantidade);
    variacoes.save(variacao);

    registrar(produto, variacao, usuario, MovimentoEstoqueTipo.ENTRADA, 
            quantidade, anterior, variacao.getEstoqueAtual(), obs);
}

@Transactional
public void ajustarEstoque(Produto produto, ProdutoVariacao variacao, 
                           int novoEstoque, Usuario usuario, String obs) {
    int anterior = variacao.getEstoqueAtual();
    variacao.setEstoqueAtual(novoEstoque);
    variacoes.save(variacao);

    registrar(produto, variacao, usuario, MovimentoEstoqueTipo.AJUSTE, 
            Math.abs(novoEstoque - anterior), anterior, novoEstoque, obs);
}

@Transactional
public void estornarEstoque(Produto produto, ProdutoVariacao variacao, 
                            int quantidade, Usuario usuario, String obs) {
    if (quantidade <= 0) return;

    int anterior = variacao.getEstoqueAtual();
    variacao.setEstoqueAtual(anterior + quantidade);
    variacoes.save(variacao);

    registrar(produto, variacao, usuario, MovimentoEstoqueTipo.ESTORNO, 
            quantidade, anterior, variacao.getEstoqueAtual(), obs);
}

```

## 📋 Checklist de prioridades

| \# | Pendência | Prioridade | Status | 
 | ----- | ----- | ----- | ----- | 
| 1 | Erro 500 nos favoritos | 🔴 Crítica | ⏳ | 
| 2 | Bug do recuperar senha | 🔴 Crítica | ⏳ | 
| 3 | Sistema de cupons | 🔴 Crítica | ⏳ | 
| 4 | Recalcular no back | 🔴 Crítica | ⏳ | 
| 5 | Seed (modelos, imagens, avaliação) | 🟡 Importante | ⏳ | 
| 6 | GET /api/auth/me | 🟡 Importante | ⏳ | 
| 7 | Tratamento 401/403/404/500 | 🟡 Importante | ⏳ | 
| 8 | Cadastro completo de produto | 🟢 Desejável | ⏳ | 
| 9 | Normalizar e-mail | 🟢 Desejável | ⏳ | 
| 10 | Endpoints de estoque | 🟢 Desejável | ⏳ | 

> **Nota:** Prioridades 1 e 4 são as mais urgentes — bloqueiam funcionalidades do app e representam falha de segurança.
>
> *Documento consolidado em 24/09/2026 — Pendências finais após integração completa.*
