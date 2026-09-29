package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.AtualizarClienteRequest;
import br.com.arthurbaby.dto.ClienteResponse;
import br.com.arthurbaby.dto.EnderecoRequest;
import br.com.arthurbaby.dto.EnderecoResponse;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;
    public ClienteController(ClienteService service) { this.service = service; }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.buscarPerfil(id);
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id, @RequestBody AtualizarClienteRequest request,
                                     @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.atualizarPerfil(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        service.inativar(id);
    }

    @GetMapping("/{id}/enderecos")
    public List<EnderecoResponse> listarEnderecos(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.listarEnderecos(id);
    }

    @GetMapping("/{id}/enderecos/{enderecoId}")
    public EnderecoResponse buscarEndereco(@PathVariable Long id, @PathVariable Long enderecoId,
                                           @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.buscarEndereco(id, enderecoId);
    }

    @PostMapping("/{id}/enderecos")
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoResponse criarEndereco(@PathVariable Long id, @RequestBody EnderecoRequest request,
                                          @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.criarEndereco(id, request);
    }

    @PutMapping("/{id}/enderecos/{enderecoId}")
    public EnderecoResponse atualizarEndereco(@PathVariable Long id, @PathVariable Long enderecoId,
                                              @RequestBody EnderecoRequest request,
                                              @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.atualizarEndereco(id, enderecoId, request);
    }

    @DeleteMapping("/{id}/enderecos/{enderecoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirEndereco(@PathVariable Long id, @PathVariable Long enderecoId,
                                @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        service.excluirEndereco(id, enderecoId);
    }

    private void verificarAcesso(Long id, Usuario logado) {
        boolean equipeLoja = logado.getPerfil() == Perfil.ADMINISTRADOR || logado.getPerfil() == Perfil.VENDEDOR;
        if (!equipeLoja && !logado.getId().equals(id)) {
            throw new AccessDeniedException("Acesso negado a dados de outro cliente");
        }
    }
}
