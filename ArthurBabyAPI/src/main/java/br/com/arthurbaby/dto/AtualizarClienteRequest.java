package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Corpo do {@code PUT /api/clientes/{id}}: dados cadastrais editáveis pelo cliente. */
@Schema(description = "Dados cadastrais a atualizar")
public record AtualizarClienteRequest(
        @Schema(description = "Nome completo", example = "Ana Souza Lima") String nomeCompleto,
        @Schema(description = "E-mail (não pode pertencer a outro cliente)", example = "ana.souza@email.com") String email,
        @Schema(description = "CPF (não pode pertencer a outro cliente)", example = "529.982.247-25") String cpf,
        @Schema(description = "Telefone com DDD", example = "(71) 99131-1944") String telefone) {}
