package br.com.arthurbaby.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * Cópia do endereço de entrega gravada no pedido no momento da compra.
 * Fica no próprio pedido (colunas entrega_*) para não mudar se o cliente editar ou apagar o endereço depois.
 */
@Getter @Setter @Embeddable
public class EnderecoEntrega {
    @Column(name = "entrega_endereco_id") private Long enderecoId;
    @Column(name = "entrega_cep") private String cep;
    @Column(name = "entrega_logradouro") private String logradouro;
    @Column(name = "entrega_numero") private String numero;
    @Column(name = "entrega_complemento") private String complemento;
    @Column(name = "entrega_bairro") private String bairro;
    @Column(name = "entrega_cidade") private String cidade;
    @Column(name = "entrega_uf") private String uf;
    @Column(name = "entrega_referencia") private String referencia;

    public static EnderecoEntrega de(Endereco e) {
        EnderecoEntrega c = new EnderecoEntrega();
        c.enderecoId = e.getId();
        c.cep = e.getCep();
        c.logradouro = e.getLogradouro();
        c.numero = e.getNumero();
        c.complemento = e.getComplemento();
        c.bairro = e.getBairro();
        c.cidade = e.getCidade();
        c.uf = e.getUf();
        c.referencia = e.getReferencia();
        return c;
    }
}