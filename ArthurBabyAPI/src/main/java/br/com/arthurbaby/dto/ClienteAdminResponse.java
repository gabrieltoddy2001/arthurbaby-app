package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Endereco;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Enums.UsuarioStatus;
import br.com.arthurbaby.entity.Usuario;

import java.time.LocalDateTime;
import java.util.List;

/** Dados exclusivos da aba Clientes, sem campos de autenticação ou usuários internos. */
public record ClienteAdminResponse(
        Long id,
        String nome,
        String email,
        String cpf,
        String telefone,
        Perfil perfil,
        UsuarioStatus status,
        LocalDateTime criadoEm,
        List<EnderecoResponse> enderecos
) {
    public static ClienteAdminResponse from(Usuario cliente) {
        List<Endereco> enderecos = cliente.getEnderecos();
        return new ClienteAdminResponse(
                cliente.getId(), cliente.getNomeCompleto(), cliente.getEmail(),
                cliente.getCpf(), cliente.getTelefone(), cliente.getPerfil(),
                cliente.getStatus(), cliente.getCriadoEm(),
                enderecos == null ? List.of() : enderecos.stream().map(EnderecoResponse::from).toList());
    }
}
