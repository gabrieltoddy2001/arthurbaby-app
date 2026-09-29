package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

/** Dados cadastrais de um usuário, sem expor a senha nem os aceites. */
@Schema(description = "Dados cadastrais do cliente")
public record ClienteResponse(
        @Schema(description = "Id do usuário", example = "3") Long id,
        @Schema(description = "Nome completo", example = "Ana Souza") String nomeCompleto,
        @Schema(description = "E-mail", example = "ana.souza@email.com") String email,
        @Schema(description = "CPF", example = "529.982.247-25") String cpf,
        @Schema(description = "Telefone com DDD", example = "(71) 99339-9816") String telefone,
        @Schema(description = "Perfil de acesso", example = "CLIENTE", allowableValues = {"ADMINISTRADOR", "VENDEDOR", "CLIENTE"}) String perfil,
        @Schema(description = "Situação da conta", example = "ATIVO", allowableValues = {"ATIVO", "INATIVO", "BLOQUEADO"}) String status
) {
    /** Converte a entidade {@link Usuario} para a resposta da API. */
    public static ClienteResponse from(Usuario u) {
        return new ClienteResponse(u.getId(), u.getNomeCompleto(), u.getEmail(), u.getCpf(), u.getTelefone(),
                u.getPerfil().name(), u.getStatus().name());
    }
}
