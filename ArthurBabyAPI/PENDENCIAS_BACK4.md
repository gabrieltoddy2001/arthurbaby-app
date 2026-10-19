# Especificação de Back-end — Sprint de Melhorias do App

Este documento descreve as regras de negócio, alterações no banco de dados, mapeamentos de entidades JPA/Spring e os novos endpoints necessários para dar suporte às novas funcionalidades do aplicativo Android.

> **Nota de Integração:** As telas, animações, botões e a interface do front-end já estão implementadas e aguardando apenas a disponibilidade destes endpoints para integração.

---

## 📊 Resumo Executivo das Tasks

| # | Funcionalidade | Tipo de Alteração | Endpoint(s) Envolvido(s) | Prioridade |
|---|---|---|---|---|
| 1 | **Alterar Senha (Logado)** | Novo Endpoint | `PUT /api/clientes/{id}/senha` / `PUT /api/auth/alterar-senha` | 🔴 Alta |
| 2 | **Meus Cupons** | Novo Endpoint | `GET /api/cupons/ativos` | 🟡 Média |
| 3 | **Avaliar Produto** | Tabela + DTOs + Endpoints | `POST /api/produtos/{id}/avaliacoes`<br>`GET /api/produtos/{id}/avaliacoes` | 🟡 Média |

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

## Implementação Sugerida (Spring Boot)

### Request Record
```java
public record AlterarSenhaRequest(
    @NotBlank(message = "A senha atual é obrigatória")
    String senhaAtual,
    
    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 4, message = "A nova senha deve possuir no mínimo 4 caracteres")
    String novaSenha
) {}
```

### Service
```java
@Transactional
public void alterarSenha(Long clienteId, AlterarSenhaRequest request) {
    Usuario usuario = usuarioRepository.findById(clienteId)
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

    if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
        throw new RegraNegocioException("Senha atual incorreta");
    }

    usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
    usuarioRepository.save(usuario);
}
```

---

# 🟡 Task 2: Meus Cupons

## Contexto
Exibir uma listagem com os cupons válidos e disponíveis no aplicativo para consulta do cliente antes do checkout.

## Endpoint
- **URL:** `GET /api/cupons/ativos`
- **Autenticação:** Público ou Autenticado (conforme política da aplicação)

## Payload e Respostas

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

---

## Regras de Negócio
1. **Filtro de Status:** Retornar exclusivamente registros onde `ativo = true`.
2. **Filtro de Data Limite:** Ignorar cupons cuja data `validoAte` seja anterior ao timestamp atual (`validoAte < NOW()`). Se `validoAte` for `NULL`, o cupom não possui expiração por data.
3. **Filtro de Data Inicial:** Ignorar cupons cuja data `validoDe` seja posterior ao timestamp atual (`validoDe > NOW()`).

---

## Implementação Sugerida (Spring Boot)

### CupomDTO Record
```java
public record CupomResponse(
    String codigo,
    String descricao,
    String tipo,
    BigDecimal valor,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime validoAte
) {}
```

### Repository Interface
```java
@Repository
public interface CupomRepository extends JpaRepository<Cupom, Long> {

    @Query("""
        SELECT c FROM Cupom c 
        WHERE c.ativo = true 
          AND (c.validoAte IS NULL OR c.validoAte >= :agora)
          AND (c.validoDe IS NULL OR c.validoDe <= :agora)
    """)
    List<Cupom> findCuponsAtivos(@Param("agora") LocalDateTime agora);
}
```

---

# 🟡 Task 3: Avaliar Produto

## Contexto
Permitir que clientes logados avaliem produtos comprados enviando nota (1 a 5 estrelas) e comentário. O sistema armazena o histórico de avaliações e calcula dinamicamente a média das notas para exibição no detalhe do produto.

---

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

---

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

---

### 3.2.2 Listar Avaliações por Produto
- **URL:** `GET /api/produtos/{produtoId}/avaliacoes`
- **Autenticação:** Público

#### Response Success (200 OK)
```json
[
  {
    "id": 1,
    "nota": 5,
    "comentario": "Tecido muito macio, amei!",
    "clienteNome": "Maria Silva",
    "criadoEm": "2026-10-19T10:00:00"
  },
  {
    "id": 2,
    "nota": 4,
    "comentario": "Ótimo produto, entrega rápida.",
    "clienteNome": "João Santos",
    "criadoEm": "2026-10-18T14:30:00"
  }
]
```

---

## 3.3 Regras de Negócio
1. **Validação de Existência:** Validar se `clienteId` e `produtoId` existem no banco.
2. **Validação do Intervalo da Nota:** Garantir que a nota esteja estritamente entre `1` e `5`.
3. **Unicidade de Avaliação:** Um cliente pode avaliar o mesmo produto apenas **uma única vez** (garantido por `UNIQUE KEY` e verificação no Service).
4. **Validação de Compra Anterior (Recomendada):** Verificar se o cliente possui algum pedido no status `ENTREGUE` contendo o `produtoId` antes de permitir o envio da avaliação.

---

## 3.4 Atualização de Endpoint Existente: `GET /api/produtos/{id}`

O campo `avaliacao` (já existente no DTO de detalhe do produto) deve ser recalculado dinamicamente no banco através da média das notas:

$$\text{Média Avaliação} = \frac{\sum_{i=1}^{n} \text{nota}_i}{n}$$

- **Com avaliações:** Retornar a média com precisão decimal (ex: `4.5`).
- **Sem avaliações:** Retornar `0.0`.

---

## 3.5 Mapeamento JPA (Spring Boot)

### Entidade JPA (`Avaliacao.java`)
```java
@Entity
@Table(name = "avaliacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @Column(nullable = false)
    private Integer nota;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
```

### Repository (`AvaliacaoRepository.java`)
```java
@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByProdutoIdOrderByCriadoEmDesc(Long produtoId);

    boolean existsByClienteIdAndProdutoId(Long clienteId, Long produtoId);

    @Query("SELECT COALESCE(AVG(a.nota), 0.0) FROM Avaliacao a WHERE a.produto.id = :produtoId")
    Double calcularMediaAvaliacoesByProdutoId(@Param("produtoId") Long produtoId);
}
```

---

## 📋 Checklist de Implementação Backend

- [ ] **Task 1: Alterar Senha**
    - [ ] Criar DTO `AlterarSenhaRequest` com anotações `@NotBlank` e `@Size`.
    - [ ] Implementar verificação de senha atual com `PasswordEncoder`.
    - [ ] Criar testes unitários para cenários de sucesso, senha incorreta e validação de tamanho.

- [ ] **Task 2: Meus Cupons**
    - [ ] Adicionar query customizada no `CupomRepository` filtrando cupons ativos e vigentes.
    - [ ] Mapear o DTO de resposta `CupomResponse`.
    - [ ] Testar cenários com cupons expirados, inativos e sem data limite (`NULL`).

- [ ] **Task 3: Avaliar Produto**
    - [ ] Executar script de migração DDL da tabela `avaliacao`.
    - [ ] Mapear a entidade `Avaliacao` e repository correspondente.
    - [ ] Criar endpoints `POST` e `GET` de avaliações.
    - [ ] Atualizar consulta/mapeamento do `GET /api/produtos/{id}` para injetar a média calculada via `AVG()`.