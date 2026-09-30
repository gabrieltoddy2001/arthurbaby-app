package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.VariacaoStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter @Entity
public class ProdutoVariacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    // WRITE_ONLY: aceito na entrada (CRUD admin envia { "id": N }), mas omitido na saida para evitar referencia circular no JSON
    @ManyToOne(optional = false) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) private Produto produto;
    @ManyToOne private Tamanho tamanho;
    @ManyToOne private Cor cor;
    @ManyToOne private Modelo modelo;
    @Column(nullable = false, unique = true, length = 100) private String sku;
    @Column(precision = 12, scale = 2) private BigDecimal preco;
    private int estoqueAtual;
    private int estoqueMinimo;
    @Enumerated(EnumType.STRING) private VariacaoStatus status = VariacaoStatus.ATIVA;
}
