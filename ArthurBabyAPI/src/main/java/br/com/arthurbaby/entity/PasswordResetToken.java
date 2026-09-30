package br.com.arthurbaby.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

/** Token descartavel de recuperacao de senha: expira e so pode ser usado uma vez. */
@Getter @Setter @Entity
@Table(name = "password_reset_token", indexes = @Index(name = "idx_token_reset", columnList = "token"))
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 100) private String token;
    @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_password_reset_token_usuario"))
    private Usuario usuario;
    @Column(nullable = false) private LocalDateTime expiraEm;
    @Column(nullable = false) private boolean usado = false;
    private LocalDateTime criadoEm;
    @PrePersist void prePersist() { criadoEm = LocalDateTime.now(); }

    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(expiraEm);
    }
}
