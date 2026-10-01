package br.com.arthurbaby.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/** Registro de acao feita no painel admin. O nome do usuario e gravado como texto para o log sobreviver a exclusoes. */
@Getter @Setter @Entity
public class Auditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(length = 180) private String usuario;
    @Column(nullable = false, length = 40) private String acao;
    @Column(length = 60) private String entidade;
    private Long entidadeId;
    @Column(columnDefinition = "TEXT") private String descricao;
    // READ_ONLY: a data e sempre a do servidor, ignorando o que vier no corpo
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) private LocalDateTime data;
    @PrePersist void prePersist() { data = LocalDateTime.now(); }
}
