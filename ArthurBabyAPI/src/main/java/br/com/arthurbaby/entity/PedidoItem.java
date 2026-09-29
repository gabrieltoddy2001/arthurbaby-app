package br.com.arthurbaby.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter @Entity
public class PedidoItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JsonIgnore private Pedido pedido;
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
