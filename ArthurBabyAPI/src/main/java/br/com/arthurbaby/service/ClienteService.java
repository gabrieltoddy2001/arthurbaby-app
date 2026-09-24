package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.AtualizarClienteRequest;
import br.com.arthurbaby.dto.ClienteResponse;
import br.com.arthurbaby.dto.EnderecoRequest;
import br.com.arthurbaby.dto.EnderecoResponse;
import br.com.arthurbaby.entity.Endereco;
import br.com.arthurbaby.entity.Enums.UsuarioStatus;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.EnderecoRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ClienteService {
    private final UsuarioRepository usuarios;
    private final EnderecoRepository enderecos;

    public ClienteService(UsuarioRepository usuarios, EnderecoRepository enderecos) {
        this.usuarios = usuarios;
        this.enderecos = enderecos;
    }

    public ClienteResponse buscarPerfil(Long id) {
        return ClienteResponse.from(buscarUsuario(id));
    }

    public ClienteResponse atualizarPerfil(Long id, AtualizarClienteRequest request) {
        Usuario usuario = buscarUsuario(id);

        if (request.email() != null && !request.email().isBlank()) {
            String email = AuthService.normalizarEmail(request.email());
            if (!email.equals(usuario.getEmail()) && usuarios.existsByEmail(email)) {
                throw new IllegalArgumentException("E-mail ja cadastrado");
            }
            usuario.setEmail(email);
        }

        if (request.cpf() != null && !request.cpf().isBlank()) {
            String cpf = request.cpf().replaceAll("\\D", "");
            if (!AuthService.validarCpf(cpf)) throw new IllegalArgumentException("CPF invalido");
            if (!cpf.equals(usuario.getCpf()) && usuarios.existsByCpf(cpf)) {
                throw new IllegalArgumentException("CPF ja cadastrado");
            }
            usuario.setCpf(cpf);
        }

        if (request.nomeCompleto() != null && !request.nomeCompleto().isBlank()) {
            usuario.setNomeCompleto(request.nomeCompleto());
        }
        if (request.telefone() != null) {
            usuario.setTelefone(request.telefone());
        }

        return ClienteResponse.from(usuarios.save(usuario));
    }

    public void inativar(Long id) {
        Usuario usuario = buscarUsuario(id);
        usuario.setStatus(UsuarioStatus.INATIVO);
        usuarios.save(usuario);
    }

    public List<EnderecoResponse> listarEnderecos(Long clienteId) {
        buscarUsuario(clienteId);
        return enderecos.findByUsuarioId(clienteId).stream().map(EnderecoResponse::from).toList();
    }

    public EnderecoResponse buscarEndereco(Long clienteId, Long enderecoId) {
        return EnderecoResponse.from(buscarEnderecoDoCliente(clienteId, enderecoId));
    }

    public EnderecoResponse criarEndereco(Long clienteId, EnderecoRequest request) {
        Usuario usuario = buscarUsuario(clienteId);
        Endereco endereco = new Endereco();
        endereco.setUsuario(usuario);
        preencher(endereco, request);

        if (Boolean.TRUE.equals(request.principal()) || enderecos.findByUsuarioId(clienteId).isEmpty()) {
            desmarcarPrincipais(clienteId);
            endereco.setPrincipal(true);
        }

        return EnderecoResponse.from(enderecos.save(endereco));
    }

    public EnderecoResponse atualizarEndereco(Long clienteId, Long enderecoId, EnderecoRequest request) {
        Endereco endereco = buscarEnderecoDoCliente(clienteId, enderecoId);
        preencher(endereco, request);

        if (Boolean.TRUE.equals(request.principal())) {
            desmarcarPrincipais(clienteId);
            endereco.setPrincipal(true);
        }

        return EnderecoResponse.from(enderecos.save(endereco));
    }

    public void excluirEndereco(Long clienteId, Long enderecoId) {
        Endereco endereco = buscarEnderecoDoCliente(clienteId, enderecoId);
        enderecos.delete(endereco);
    }

    private void preencher(Endereco endereco, EnderecoRequest request) {
        endereco.setCep(request.cep());
        endereco.setLogradouro(request.logradouro());
        endereco.setNumero(request.numero());
        endereco.setComplemento(request.complemento());
        endereco.setBairro(request.bairro());
        endereco.setCidade(request.cidade());
        endereco.setUf(request.uf());
        endereco.setReferencia(request.referencia());
    }

    private void desmarcarPrincipais(Long clienteId) {
        List<Endereco> atuais = enderecos.findByUsuarioId(clienteId);
        atuais.forEach(e -> e.setPrincipal(false));
        enderecos.saveAll(atuais);
    }

    private Usuario buscarUsuario(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado"));
    }

    private Endereco buscarEnderecoDoCliente(Long clienteId, Long enderecoId) {
        return enderecos.findByIdAndUsuarioId(enderecoId, clienteId)
                .orElseThrow(() -> new NoSuchElementException("Endereco nao encontrado"));
    }
}
