package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Corpo do {@code POST /api/auth/cadastro}: dados do novo cliente e o seu endereço principal.
 * Os aceites de termo de uso e LGPD são gravados junto com a conta.
 */
@Schema(description = "Dados para cadastro de um novo cliente")
public record CadastroRequest(
        @Schema(description = "Nome completo", example = "Ana Souza") String nomeCompleto,
        @Schema(description = "E-mail (único no sistema)", example = "ana.souza@email.com") String email,
        @Schema(description = "CPF com ou sem máscara (único no sistema)", example = "529.982.247-25") String cpf,
        @Schema(description = "Telefone com DDD", example = "(71) 99339-9816") String telefone,
        @Schema(description = "Senha de acesso", example = "123456") String senha,
        @Schema(description = "Aceite do termo de uso", example = "true") boolean aceiteTermoUso,
        @Schema(description = "Aceite da política de privacidade (LGPD)", example = "true") boolean aceiteLgpd,
        @Schema(description = "Endereço principal do cliente") EnderecoRequest endereco) {}
