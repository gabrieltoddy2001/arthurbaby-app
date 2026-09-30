package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta de login e cadastro. O {@code token} deve ser enviado nas próximas requisições
 * no cabeçalho {@code Authorization: Bearer <token>}.
 */
@Schema(description = "Token JWT e dados básicos do usuário autenticado")
public record AuthResponse(
        @Schema(description = "Token JWT de acesso", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwicGVyZmlsIjoiQ0xJRU5URSJ9.4sQx9kXz0v1uXcW7mV2p8bR3nT6yL0aF5dJ1hG9eK2c") String token,
        @Schema(description = "Id do usuário logado", example = "3") Long usuarioId,
        @Schema(description = "Nome completo do usuário", example = "Ana Souza") String nome,
        @Schema(description = "Perfil de acesso", example = "CLIENTE", allowableValues = {"ADMINISTRADOR", "VENDEDOR", "CLIENTE"}) String perfil) {}
