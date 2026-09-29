package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.VariacaoStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter @Entity
public class ProdutoVariacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JsonIgnore private Produto produto;
    @ManyToOne private Tamanho tamanho;
    @ManyToOne private Cor cor;
    @ManyToOne private Modelo modelo;
    @Column(nullable = false, unique = true, length = 100) private String sku;
    @Column(precision = 12, scale = 2) private BigDecimal preco;
    private int estoqueAtual;
    private int estoqueMinimo;
    @Enumerated(EnumType.STRING) private VariacaoStatus status = VariacaoStatus.ATIVA;
}
