package br.com.arthurbaby.network.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProdutoResponse {
    public Long id;
    public String codigo;
    public String sku;
    public String nome;
    public String descricao;
    public BigDecimal preco;
    public BigDecimal precoPromocional;
    public boolean promocao;
    public boolean destaque;
    public String status;
    public Long categoriaId;
    public String categoriaNome;
    public Long marcaId;
    public String marca;                      // ← back retorna "marca" (não marcaNome)
    // Média das avaliações (ex.: 4.5). O back envia BigDecimal; como int o Gson lançava
    // NumberFormatException ("Expected an int but was 4.5") e a listagem inteira falhava.
    public BigDecimal avaliacao;
    public List<ImagemResponse> imagens;
    public List<VariacaoResponse> variacoes;

    public static class ImagemResponse {
        public Long id;
        public String url;
        public String descricao;
        public boolean principal;
    }

    public static class VariacaoResponse {
        public Long id;
        public String sku;
        public String tamanho;
        public String cor;
        public String modelo;
        public BigDecimal preco;
        public int estoqueAtual;
    }
}