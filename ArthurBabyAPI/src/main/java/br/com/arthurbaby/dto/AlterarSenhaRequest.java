package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Corpo do "Alterar senha" da tela de Perfil (usuário já logado).
 * A validação (senha atual correta, nova senha com no mínimo 4 caracteres) é feita no
 * {@link br.com.arthurbaby.service.ClienteService#alterarSenha}, que devolve 400 com a mensagem do erro.
 */
@Schema(description = "Troca de senha do usuário logado")
public record AlterarSenhaRequest(
        @Schema(description = "Senha atual, conferida com o hash gravado no banco", example = "123456") String senhaAtual,
        @Schema(description = "Nova senha (mínimo 4 caracteres)", example = "novaSenha123") String novaSenha) {}
