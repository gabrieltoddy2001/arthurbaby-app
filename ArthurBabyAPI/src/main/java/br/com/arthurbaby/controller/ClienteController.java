package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.AtualizarClienteRequest;
import br.com.arthurbaby.dto.ClienteResponse;
import br.com.arthurbaby.dto.EnderecoRequest;
import br.com.arthurbaby.dto.EnderecoResponse;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Perfil e endereços do cliente. Exige token; o cliente só acessa os próprios dados, administrador e vendedor acessam qualquer cliente.")
public class ClienteController {
    private static final String OUTRO_CLIENTE = "Acesso negado: dados pertencem a outro cliente";

    private final ClienteService service;
    public ClienteController(ClienteService service) { this.service = service; }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar perfil do cliente", description = "Retorna os dados do cliente identificado pelo id informado. Cliente só acessa o próprio perfil; administrador e vendedor acessam qualquer um.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do cliente"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ClienteResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.buscarPerfil(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar perfil do cliente", description = "Atualiza os dados cadastrais do cliente identificado pelo id informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil atualizado; retorna os dados do cliente"),
            @ApiResponse(responseCode = "400", description = "E-mail ou CPF inválido ou já cadastrado por outro cliente"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ClienteResponse atualizar(@PathVariable Long id, @RequestBody AtualizarClienteRequest request,
                                     @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.atualizarPerfil(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir conta do cliente", description = "Inativa a conta do cliente identificado pelo id informado (exclusão lógica).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta inativada"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        service.inativar(id);
    }

    @GetMapping("/{id}/enderecos")
    @Operation(summary = "Listar endereços do cliente", description = "Retorna todos os endereços cadastrados do cliente identificado pelo id informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de endereços do cliente"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public List<EnderecoResponse> listarEnderecos(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.listarEnderecos(id);
    }

    @GetMapping("/{id}/enderecos/{enderecoId}")
    @Operation(summary = "Buscar endereço por id", description = "Retorna o endereço identificado pelo enderecoId, pertencente ao cliente informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Cliente ou endereço não encontrado")
    })
    public EnderecoResponse buscarEndereco(@PathVariable Long id, @PathVariable Long enderecoId,
                                           @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.buscarEndereco(id, enderecoId);
    }

    @PostMapping("/{id}/enderecos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar endereço", description = "Cadastra um novo endereço para o cliente com os dados fornecidos no corpo da requisição.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Endereço criado"),
            @ApiResponse(responseCode = "400", description = "Dados do endereço inválidos ou corpo mal formatado"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public EnderecoResponse criarEndereco(@PathVariable Long id, @RequestBody EnderecoRequest request,
                                          @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.criarEndereco(id, request);
    }

    @PutMapping("/{id}/enderecos/{enderecoId}")
    @Operation(summary = "Atualizar endereço", description = "Atualiza o endereço identificado pelo enderecoId, pertencente ao cliente informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados do endereço inválidos ou corpo mal formatado"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Cliente ou endereço não encontrado")
    })
    public EnderecoResponse atualizarEndereco(@PathVariable Long id, @PathVariable Long enderecoId,
                                              @RequestBody EnderecoRequest request,
                                              @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(id, logado);
        return service.atualizarEndereco(id, enderecoId, request);
    }

    @DeleteMapping("/{id}/enderecos/{enderecoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir endereço", description = "Exclui o endereço identificado pelo enderecoId, pertencente ao cliente informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Endereço excluído"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Cliente ou endereço não encontrado")
    })
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
