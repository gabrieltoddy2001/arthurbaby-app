package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Endereço enviado no cadastro do cliente e na criação/edição de endereços. */
@Schema(description = "Dados do endereço")
public record EnderecoRequest(
        @Schema(description = "CEP com ou sem hífen", example = "40020-455") String cep,
        @Schema(description = "Rua, avenida etc.", example = "Avenida Sete de Setembro") String logradouro,
        @Schema(description = "Número", example = "548") String numero,
        @Schema(description = "Complemento (opcional)", example = "Apto 302") String complemento,
        @Schema(description = "Bairro", example = "Centro - Dois de Julho") String bairro,
        @Schema(description = "Cidade", example = "Salvador") String cidade,
        @Schema(description = "Sigla do estado", example = "BA") String uf,
        @Schema(description = "Ponto de referência (opcional)", example = "Próximo à Praça da Piedade") String referencia,
        @Schema(description = "Se é o endereço principal do cliente", example = "true") Boolean principal
) {}
