package br.com.arthurbaby.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

/**
 * Avaliação (nota de 1 a 5 + comentário) que um cliente deixa num produto que comprou.
 * Cada cliente avalia o mesmo produto uma única vez (uk_cliente_produto). A média das notas
 * é calculada na hora no detalhe do produto (ver AvaliacaoRepository#calcularMediaAvaliacoesByProdutoId).
 * A tabela é criada sozinha pelo Hibernate ({@code ddl-auto=update}); no MySQL o SchemaMigration
 * alinha as FKs com os ids BIGINT UNSIGNED do arthurbaby_bd.sql.
 */
@Getter @Setter @Entity
@Table(name = "avaliacao", uniqueConstraints = @UniqueConstraint(name = "uk_cliente_produto", columnNames = {"cliente_id", "produto_id"}))
@Check(constraints = "nota BETWEEN 1 AND 5")
public class Avaliacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    // ON DELETE CASCADE: excluir o produto ou o cliente apaga as avaliações dele junto
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "produto_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cliente_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario cliente;

    @Column(nullable = false) private Integer nota;
    @Column(columnDefinition = "TEXT") private String comentario;
    @Column(name = "criado_em", nullable = false, updatable = false) private LocalDateTime criadoEm;

    @PrePersist void prePersist() { if (criadoEm == null) criadoEm = LocalDateTime.now(); }
}
