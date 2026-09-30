package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Endereco;
import io.swagger.v3.oas.annotations.media.Schema;

/** Endereço cadastrado de um cliente. */
@Schema(description = "Endereço do cliente")
public record EnderecoResponse(
        @Schema(description = "Id do endereço", example = "7") Long id,
        @Schema(description = "CEP", example = "40020-455") String cep,
        @Schema(description = "Rua, avenida etc.", example = "Avenida Sete de Setembro") String logradouro,
        @Schema(description = "Número", example = "548") String numero,
        @Schema(description = "Complemento", example = "Apto 302") String complemento,
        @Schema(description = "Bairro", example = "Centro - Dois de Julho") String bairro,
        @Schema(description = "Cidade", example = "Salvador") String cidade,
        @Schema(description = "Sigla do estado", example = "BA") String uf,
        @Schema(description = "Ponto de referência", example = "Próximo à Praça da Piedade") String referencia,
        @Schema(description = "Se é o endereço principal", example = "true") boolean principal
) {
    /** Converte a entidade {@link Endereco} para a resposta da API. */
    public static EnderecoResponse from(Endereco e) {
        return new EnderecoResponse(e.getId(), e.getCep(), e.getLogradouro(), e.getNumero(), e.getComplemento(),
                e.getBairro(), e.getCidade(), e.getUf(), e.getReferencia(), e.isPrincipal());
    }
}
