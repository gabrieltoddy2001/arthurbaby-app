package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.FormaRecebimento;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @Entity
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 30) private String numeroPedido;
    @ManyToOne(optional = false) private Usuario cliente;
    @ManyToOne private Usuario vendedor;
    @Enumerated(EnumType.STRING) private PedidoStatus status = PedidoStatus.PEDIDO_GERADO;
    @Enumerated(EnumType.STRING) private FormaRecebimento formaRecebimento = FormaRecebimento.RETIRADA_LOJA;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal desconto = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal frete = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total = BigDecimal.ZERO;
    @Column(columnDefinition = "TEXT") private String observacao;
    private String motivoCancelamento;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private LocalDateTime confirmadoEm;
    private LocalDateTime canceladoEm;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> itens = new ArrayList<>();
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoStatusHistorico> historico = new ArrayList<>();
    @PrePersist void prePersist() { criadoEm = LocalDateTime.now(); atualizadoEm = criadoEm; }
    @PreUpdate void preUpdate() { atualizadoEm = LocalDateTime.now(); }
}
