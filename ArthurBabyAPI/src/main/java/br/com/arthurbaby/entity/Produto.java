package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.ProdutoStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @Entity
public class Produto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Categoria categoria;
    @ManyToOne private Marca marca;
    @Column(nullable = false, unique = true, length = 50) private String codigo;
    @Column(nullable = false, unique = true, length = 80) private String sku;
    @Column(nullable = false, length = 180) private String nome;
    @Column(columnDefinition = "TEXT") private String descricao;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal preco;
    @Column(precision = 12, scale = 2) private BigDecimal precoPromocional;
    @Column(precision = 12, scale = 2) private BigDecimal custo;
    private BigDecimal peso;
    private BigDecimal altura;
    private BigDecimal largura;
    private BigDecimal comprimento;
    private int estoqueMinimo;
    @Enumerated(EnumType.STRING) private ProdutoStatus status = ProdutoStatus.ATIVO;
    private boolean destaque;
    private boolean promocao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProdutoImagem> imagens = new ArrayList<>();
    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProdutoVariacao> variacoes = new ArrayList<>();
    @PrePersist void prePersist() { criadoEm = LocalDateTime.now(); atualizadoEm = criadoEm; }
    @PreUpdate void preUpdate() { atualizadoEm = LocalDateTime.now(); }
}
