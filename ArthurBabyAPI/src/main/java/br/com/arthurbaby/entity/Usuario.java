package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Enums.UsuarioStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter @Setter
@Entity
public class Usuario implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String nomeCompleto;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Column(nullable = false)
    private String senha;
    private String telefone;
    @Column(unique = true, length = 14)
    private String cpf;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Perfil perfil = Perfil.CLIENTE;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private UsuarioStatus status = UsuarioStatus.ATIVO;
    private boolean aceiteTermoUso;
    private LocalDateTime dataAceiteTermoUso;
    private boolean aceiteLgpd;
    private LocalDateTime dataAceiteLgpd;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new ArrayList<>();

    @PrePersist void prePersist() { criadoEm = LocalDateTime.now(); atualizadoEm = criadoEm; }
    @PreUpdate void preUpdate() { atualizadoEm = LocalDateTime.now(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name())); }
    @Override public String getPassword() { return senha; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return status != UsuarioStatus.BLOQUEADO; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return status == UsuarioStatus.ATIVO; }
}
