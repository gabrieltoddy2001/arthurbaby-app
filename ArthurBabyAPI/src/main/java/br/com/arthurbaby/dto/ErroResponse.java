package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Formato padrão de erro da API (documentação do Swagger). */
@Schema(description = "Corpo retornado em qualquer resposta de erro")
public record ErroResponse(
        @Schema(description = "Mensagem descrevendo o erro", example = "Registro nao encontrado") String erro,
        @Schema(description = "Código HTTP do erro", example = "404") int codigo) {
}
