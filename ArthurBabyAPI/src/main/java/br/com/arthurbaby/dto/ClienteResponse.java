package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Usuario;

public record ClienteResponse(
        Long id,
        String nomeCompleto,
        String email,
        String cpf,
        String telefone,
        String perfil,
        String status
) {
    public static ClienteResponse from(Usuario u) {
        return new ClienteResponse(u.getId(), u.getNomeCompleto(), u.getEmail(), u.getCpf(), u.getTelefone(),
                u.getPerfil().name(), u.getStatus().name());
    }
}
