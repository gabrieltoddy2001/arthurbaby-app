package br.com.arthurbaby.network.dto;

/** Endereço de entrega gravado no pedido (cópia feita no momento da compra). */
public class EnderecoEntregaResponse {
    public String cep;
    public String logradouro;
    public String numero;
    public String complemento;
    public String bairro;
    public String cidade;
    public String uf;
    public String referencia;
}
