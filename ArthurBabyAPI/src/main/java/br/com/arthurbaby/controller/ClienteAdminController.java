package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.ClienteAdminResponse;
import br.com.arthurbaby.entity.Enums.UsuarioStatus;
import br.com.arthurbaby.service.ClienteAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Rota específica do painel para clientes da loja; /api/admin/** já exige Admin ou Vendedor. */
@RestController
@RequestMapping(value = "/api/admin/clientes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Administração de clientes", description = "Operações da aba Clientes, apenas perfil CLIENTE.")
public class ClienteAdminController {
    private final ClienteAdminService service;

    public ClienteAdminController(ClienteAdminService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Listar somente clientes da loja, sem administradores ou vendedores")
    public List<ClienteAdminResponse> listar() { return service.listar(); }

    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bloquear/desbloquear exclusivamente uma conta CLIENTE")
    public ClienteAdminResponse alterarStatus(@PathVariable Long id, @RequestBody AlterarStatusRequest request) {
        if (request == null) throw new IllegalArgumentException("Informe o status do cliente");
        return service.alterarStatus(id, request.status());
    }

    @PatchMapping(value = "/{id}/senha", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Redefinir senha exclusivamente de uma conta CLIENTE")
    public void redefinirSenha(@PathVariable Long id, @RequestBody RedefinirSenhaRequest request) {
        if (request == null) throw new IllegalArgumentException("Informe a nova senha");
        service.redefinirSenha(id, request.senha());
    }

    public record AlterarStatusRequest(UsuarioStatus status) {}
    public record RedefinirSenhaRequest(String senha) {}
}
