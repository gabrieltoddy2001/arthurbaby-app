package br.com.arthurbaby.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity
public class PedidoStatusHistorico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    // WRITE_ONLY: aceito na entrada (CRUD admin envia { "id": N }), mas omitido na saida para evitar referencia circular no JSON
    @ManyToOne(optional = false) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) private Pedido pedido;
    @ManyToOne private Usuario usuario;
    private String statusAnterior;
    @Column(nullable = false) private String statusNovo;
    private String observacao;
    private LocalDateTime criadoEm;
    @PrePersist void prePersist() { criadoEm = LocalDateTime.now(); }
}
