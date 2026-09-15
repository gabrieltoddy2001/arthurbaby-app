package br.com.arthurbaby.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity
public class PedidoStatusHistorico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JsonIgnore private Pedido pedido;
    @ManyToOne private Usuario usuario;
    private String statusAnterior;
    @Column(nullable = false) private String statusNovo;
    private String observacao;
    private LocalDateTime criadoEm;
    @PrePersist void prePersist() { criadoEm = LocalDateTime.now(); }
}
