package br.com.arthurbaby.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
public class Endereco {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // WRITE_ONLY: aceito na entrada (CRUD admin envia { "id": N }), mas omitido na saida para evitar referencia circular no JSON
    @ManyToOne(optional = false) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Usuario usuario;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String referencia;
    private boolean principal;
}
