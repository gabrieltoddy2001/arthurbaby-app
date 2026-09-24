# Pendências do Backend — Parte 3

Documento com as **pendências finais** identificadas após a integração completa do app Android.

> ℹ️ **Status anterior:** Os itens das partes 1 e 2 já foram resolvidos. Este documento cobre o que **ainda falta**.

---

## 📊 Resumo Executivo

| # | Pendência | Prioridade | Impacto |
|---|---|---|---|
| **1** | Corrigir `POST /api/auth/recuperar-senha` | 🔴 Crítica | Bloqueia recuperação de senha |
| **2** | Sistema de cupons dinâmico | 🔴 Crítica | Cupom *hardcoded* no front |
| **3** | Recalcular frete/desconto/total no back | 🔴 Crítica | Falha de segurança financeira |
| **4** | Criar `GET /api/auth/me` | 🟡 Importante | Perfil incompleto |
| **5** | Tratamento de erros (401/403/404/500) | 🟡 Importante | UX ruim em erros |
| **6** | Normalizar e-mail no login/cadastro | 🟢 Desejável | Duplicidade de contas |
| **7** | Endpoints de estoque (entrada/ajuste/estorno) | 🟢 Desejável | Painel admin futuro |

---

## 🔴 1. Corrigir `POST /api/auth/recuperar-senha`

### ⚠️ Problema
O endpoint está retornando o **hash BCrypt da senha do usuário** no corpo da resposta, em vez de gerar um **token descartável** de recuperação.

#### Resposta Atual (ERRADA):
```json
{
  "senha": "$2a$10$VSMLGFthR87nW3Stq7rbZOsXbN.7wd4wdUz47719pA3zE0OWsms4u"
}
```

#### Resposta Correta Esperada:
```json
{
  "mensagem": "Se o e-mail estiver cadastrado, você receberá um código."
}
```

#### Impactos:
* 🚨 **Falha grave de segurança** (hash exposto).
* 🔴 Fluxo de recuperação de senha quebrado no app.
* 🔴 Bloqueia o endpoint `POST /api/auth/redefinir-senha`.

---

### 💡 Solução

#### Fluxo Correto
1. **Usuário solicita:** `POST /api/auth/recuperar-senha` com `{ "email": "..." }`
2. **Backend gera token único:** `UUID.randomUUID()`
3. **Persistência:** Salva em `password_reset_token` com validade (1 hora).
4. **Notificação:** Envia por e-mail com link `https://arthurbaby.com/redefinir?token=abc123`
5. **Ambiente Dev:** Imprime o token no console (`System.out.println`).
6. **Resposta:** Apenas `{ "mensagem": "..." }` (nunca o token).

#### Tabela SQL
```sql
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

#### Entity (Java)
```java
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

    // Getters e Setters
}
```

#### Service (Java)
```java
@Service
public class PasswordResetService {
    private final UsuarioRepository usuarios;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    // private final EmailService emailService; // Opcional

    public PasswordResetService(UsuarioRepository usuarios, PasswordResetTokenRepository tokens, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.encoder = encoder;
    }

    @Transactional
    public void solicitarRecuperacao(String email) {
        Optional<Usuario> userOpt = usuarios.findByEmail(email.toLowerCase().trim());
        if (userOpt.isEmpty()) return; // Não revela se o e-mail existe por segurança

        Usuario usuario = userOpt.get();
        String token = UUID.randomUUID().toString();

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUsuario(usuario);
        prt.setExpiraEm(LocalDateTime.now().plusHours(1));
        tokens.save(prt);

        // Envia por e-mail (ou simula em dev)
        String link = "https://arthurbaby.com/redefinir?token=" + token;
        // emailService.enviar(usuario.getEmail(), "Recuperação de senha", link);

        // EM DEV: Imprime no console
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

---

## 🔴 2. Sistema de Cupons Dinâmico

### ⚠️ Problema
Hoje o app tem cupom *hardcoded* (`if ("ARTHUR10".equals(cupom))`). Isso impede a administração de:
* Criar cupons novos sem lançar nova versão do app.
* Desativar cupons antigos.
* Criar cupons com regras dinâmicas (frete grátis, valor fixo, percentual).
* Definir datas de validade.

---

### 💡 Solução

#### Tabela SQL
```sql
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
```

#### Seed Inicial
```sql
INSERT INTO cupom (codigo, tipo, valor, descricao, ativo) VALUES
  ('ARTHUR10', 'PERCENTUAL', 10.00, '10% de desconto', TRUE),
  ('FRETEGRATIS', 'FRETE_GRATIS', NULL, 'Frete grátis', TRUE),
  ('BEMVINDO', 'VALOR_FIXO', 15.00, 'R$ 15 de desconto', TRUE);
```

#### Entity (Java)
```java
@Entity
public class Cupom {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCupom tipo;

    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorMinimo;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorMaximoDesconto;

    private LocalDateTime validoDe;
    private LocalDateTime validoAte;

    @Column(nullable = false)
    private boolean ativo = true;

    private String descricao;
    private LocalDateTime criadoEm;

    @PrePersist 
    void prePersist() { 
        criadoEm = LocalDateTime.now(); 
    }

    public boolean isValido(BigDecimal subtotal) {
        LocalDateTime agora = LocalDateTime.now();
        if (!ativo) return false;
        if (validoDe != null && agora.isBefore(validoDe)) return false;
        if (validoAte != null && agora.isAfter(validoAte)) return false;
        if (valorMinimo != null && subtotal.compareTo(valorMinimo) < 0) return false;
        return true;
    }

    public enum TipoCupom {
        PERCENTUAL, VALOR_FIXO, FRETE_GRATIS
    }

    // Getters e Setters
}
```

#### API de Validação

Endpoint: `POST /api/cupons/validar` (Público)

**Request:**
```json
{
  "codigo": "ARTHUR10",
  "subtotal": 149.90
}
```

**Response 200 (Válido):**
```json
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

**Response 200 (Inválido):**
```json
{
  "valido": false,
  "motivo": "Cupom expirado"
}
```

**Response 200 (Frete Grátis):**
```json
{
  "valido": true,
  "codigo": "FRETEGRATIS",
  "tipo": "FRETE_GRATIS",
  "descricao": "Frete grátis",
  "desconto": 0,
  "freteGratis": true
}
```

#### DTOs
```java
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

#### Controller & Service
```java
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

```java
@Service
public class CupomService {
    private final CupomRepository cupons;

    public CupomService(CupomRepository cupons) {
        this.cupons = cupons;
    }

    public CupomValidacaoResponse validar(String codigo, BigDecimal subtotal) {
        if (codigo == null || codigo.isBlank()) {
            return new CupomValidacaoResponse(false, null, null, null,
                    BigDecimal.ZERO, false, "Informe um cupom");
        }

        Optional<Cupom> opt = cupons.findByCodigo(codigo.toUpperCase().trim());
        if (opt.isEmpty()) {
            return new CupomValidacaoResponse(false, codigo, null, null,
                    BigDecimal.ZERO, false, "Cupom inválido");
        }

        Cupom c = opt.get();
        if (!c.isValido(subtotal)) {
            return new CupomValidacaoResponse(false, codigo, c.getTipo().name(),
                    c.getDescricao(), BigDecimal.ZERO, false, "Cupom não aplicável");
        }

        BigDecimal desconto = BigDecimal.ZERO;
        boolean freteGratis = false;

        switch (c.getTipo()) {
            case PERCENTUAL:
                desconto = subtotal.multiply(c.getValor())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                break;
            case VALOR_FIXO:
                desconto = c.getValor();
                break;
            case FRETE_GRATIS:
                freteGratis = true;
                break;
        }

        if (desconto.compareTo(subtotal) > 0) desconto = subtotal;
        if (c.getValorMaximoDesconto() != null
                && desconto.compareTo(c.getValorMaximoDesconto()) > 0) {
            desconto = c.getValorMaximoDesconto();
        }

        return new CupomValidacaoResponse(true, codigo, c.getTipo().name(),
                c.getDescricao(), desconto, freteGratis, null);
    }
}
```

---

## 🔴 3. Recalcular Frete, Desconto e Total no Backend

### ⚠️ Problema
O backend confia cegamente nos valores financeiros enviados pelo frontend no `POST /api/pedidos`:
```json
{
  "clienteId": 8,
  "formaRecebimento": "ENTREGA",
  "cupom": "HACKEADO",
  "desconto": 9999.99,
  "frete": 0,
  "itens": [...]
}
```
Isso gera vulnerabilidade para manipulação de requisições (ex: desconto maior que o total, frete zerado, etc.).

---

### 💡 Solução
O backend deve recalcular **todos** os valores financeiros no servidor, ignorando os valores enviados pelo aplicativo.

#### Ajuste no `PedidoService.criar()`
```java
@Transactional
public Pedido criar(PedidoRequest request) {
    // 1) Valida itens
    if (request.itens() == null || request.itens().isEmpty())
        throw new IllegalArgumentException("Pedido deve possuir pelo menos um item");

    // 2) Busca cliente
    Usuario cliente = usuarios.findById(request.clienteId())
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

    // 3) Cria pedido base
    Pedido pedido = new Pedido();
    pedido.setNumeroPedido("AB" + LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
    pedido.setCliente(cliente);
    pedido.setFormaRecebimento(request.formaRecebimento());
    pedido.setObservacao(request.observacao());

    // 4) Processa itens (SOMA o subtotal no BACK)
    BigDecimal subtotal = BigDecimal.ZERO;
    for (PedidoItemRequest itemReq : request.itens()) {
        Produto produto = produtos.findById(itemReq.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        ProdutoVariacao variacao = itemReq.variacaoId() == null ? null
                : variacoes.findById(itemReq.variacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Variação não encontrada"));

        int qtd = itemReq.quantidade() == null ? 0 : itemReq.quantidade();
        if (qtd <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");

        BigDecimal valorUnitario = (variacao != null && variacao.getPreco() != null)
                ? variacao.getPreco()
                : (produto.getPrecoPromocional() != null
                    ? produto.getPrecoPromocional()
                    : produto.getPreco());

        BigDecimal totalItem = valorUnitario.multiply(BigDecimal.valueOf(qtd));

        PedidoItem item = new PedidoItem();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setVariacao(variacao);
        item.setCodigoProduto(produto.getCodigo());
        item.setNomeProduto(produto.getNome());
        item.setVariacaoDescricao(descrever(variacao));
        item.setQuantidade(qtd);
        item.setValorUnitario(valorUnitario);
        item.setDesconto(BigDecimal.ZERO);
        item.setValorTotal(totalItem);

        pedido.getItens().add(item);
        subtotal = subtotal.add(totalItem);

        estoque.baixarEstoque(produto, variacao, qtd, cliente,
                "Pedido " + pedido.getNumeroPedido());
    }

    pedido.setSubtotal(subtotal);

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

    // 8) Histórico
    registrarHistorico(pedido, null, PedidoStatus.PEDIDO_GERADO, cliente, "Pedido gerado");

    return pedidos.save(pedido);
}

private BigDecimal calcularFrete(FormaRecebimento forma, BigDecimal subtotal) {
    if (forma == FormaRecebimento.RETIRADA_LOJA) return BigDecimal.ZERO;
    if (subtotal.compareTo(new BigDecimal("200.00")) >= 0) return BigDecimal.ZERO;
    return new BigDecimal("15.00");
}
```

> 📌 **Nota:** O backend pode continuar recebendo `desconto` e `frete` do front na DTO, porém deve ignorá-los ao persistir.

---

## 🟡 4. Criar `GET /api/auth/me`

### ⚠️ Problema
O app mobile não tem como consultar o perfil atualizado do usuário logado (CPF, telefone, e-mail), confiando apenas nos dados gravados localmente no login.

---

### 💡 Solução
Endpoint autenticado: `GET /api/auth/me`

**Response 200:**
```json
{
  "id": 8,
  "nomeCompleto": "Maria Teste",
  "email": "maria@teste.com",
  "cpf": "123.456.789-09",
  "telefone": "(71) 98888-7777",
  "perfil": "CLIENTE"
}
```

#### Controller & DTO
```java
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

---

## 🟡 5. Tratamento Global de Erros (401, 403, 404, 500)

### ⚠️ Problema
Atualmente o `ApiExceptionHandler` intercepta apenas `IllegalArgumentException` (HTTP 400). Exceções de rotas ausentes ou erros internos expõem HTML do Spring Boot.

---

### 💡 Solução
```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Dados inválidos"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> notFound(NoSuchElementException ex) {
        return ResponseEntity.status(404).body(Map.of("erro", "Recurso não encontrado"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> forbidden(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(Map.of("erro", "Sem permissão"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> generic(Exception ex) {
        return ResponseEntity.status(500).body(Map.of("erro", "Erro interno do servidor"));
    }
}
```

---

## 🟢 6. Normalizar E-mail no Login e Cadastro

### ⚠️ Problema
Tratar `TESTE@x.com` e `teste@x.com` como strings diferentes permite criação de cadastros duplicados.

---

### 💡 Solução
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
    // ...
    usuario.setEmail(email);
    // ...
}
```

---

## 🟢 7. Endpoints de Estoque (Entrada / Ajuste / Estorno)

### ⚠️ Problema
O `EstoqueService` possui apenas a lógica para `baixarEstoque()`. Falta suporte a entrada manual, ajustes e estornos de cancelamentos.

---

### 💡 Solução
```java
@Service
public class EstoqueService {

    // ... métodos existentes ...

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
}
```

---

## 📋 Checklist de Prioridades

- [ ] **1. Corrigir recuperar-senha** `(🔴 Crítica)`
- [ ] **2. Sistema de cupons dinâmico** `(🔴 Crítica)`
- [ ] **3. Recalcular frete/desconto no back** `(🔴 Crítica)`
- [ ] **4. GET /api/auth/me** `(🟡 Importante)`
- [ ] **5. Tratamento de erros** `(🟡 Importante)`
- [ ] **6. Normalizar e-mail** `(🟢 Desejável)`
- [ ] **7. Endpoints de estoque** `(🟢 Desejável)`

> 🚀 **Meta:** Após a conclusão dos itens 1, 2 e 3, a integração do app estará 100% concluída e segura para produção.  
> 📅 *Documento atualizado em 24/09/2026.*