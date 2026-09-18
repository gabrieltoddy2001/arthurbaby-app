package br.com.arthurbaby.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity
public class ConfiguracaoLoja {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String nomeFantasia;
    private String razaoSocial;
    private String cnpj;
    private String telefone;
    private String whatsapp;
    private String email;
    private String emailPedidos;
    private String instagram;
    private String shopeeUrl;
    private String facebookUrl;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;
    private String logoUrl;
    private String corPrimaria;
    private String corDestaque;
    private String corAcento;
    private String corFundo;
    private String corSuperficie;
    private String corTextoPrimario;
    private String corTextoSecundario;
    private LocalDateTime atualizadoEm;
    @PrePersist @PreUpdate void touch() { atualizadoEm = LocalDateTime.now(); }
}
