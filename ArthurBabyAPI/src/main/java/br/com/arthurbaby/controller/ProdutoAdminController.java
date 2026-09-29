package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.ProdutoCompletoRequest;
import br.com.arthurbaby.dto.ProdutoDetalheResponse;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.ProdutoAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Cadastro de produto em uma chamada só (produto + imagens + variações), usado pelo painel admin. */
@RestController
@RequestMapping(value = "/api/admin/produtos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Administração - Produtos", description = "Cadastro completo de produtos. Exige perfil ADMINISTRADOR ou VENDEDOR.")
public class ProdutoAdminController {
    private final ProdutoAdminService service;
    public ProdutoAdminController(ProdutoAdminService service) {
        this.service = service;
    }

    @PostMapping("/completo")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar produto completo", description = "Cria o produto com imagens e variações em uma única transação (tudo ou nada). "
            + "Estoque inicial de cada variação é registrado como movimentação de ENTRADA. Sem imagem marcada como principal, a primeira assume.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado; retorna o detalhe com imagens e variações"),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes, código/SKU já cadastrado, SKU de variação repetido, "
                    + "categoria/marca/tamanho/cor/modelo inexistente ou preços inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: apenas perfis ADMINISTRADOR ou VENDEDOR")
    })
    public ProdutoDetalheResponse cadastrarCompleto(@RequestBody ProdutoCompletoRequest request, @AuthenticationPrincipal Usuario logado) {
        return service.cadastrarCompleto(request, logado);
    }
}
