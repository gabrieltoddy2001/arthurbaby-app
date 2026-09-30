package br.com.arthurbaby.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter @Entity
public class PedidoItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    // WRITE_ONLY: aceito na entrada (CRUD admin envia { "id": N }), mas omitido na saida para evitar referencia circular no JSON
    @ManyToOne(optional = false) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) private Pedido pedido;
    @ManyToOne(optional = false) private Produto produto;
    @ManyToOne private ProdutoVariacao variacao;
    @Column(nullable = false, length = 50) private String codigoProduto;
    @Column(nullable = false, length = 180) private String nomeProduto;
    private String variacaoDescricao;
    private int quantidade;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal valorUnitario;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal desconto = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal valorTotal;
}
