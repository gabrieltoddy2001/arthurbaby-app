package br.com.arthurbaby.controller;

import br.com.arthurbaby.service.AdminCrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administração", description = "CRUD genérico das tabelas da loja. Exige perfil ADMINISTRADOR ou VENDEDOR.")
public class AdminCrudController {
    private static final String TIPOS = " Tipos aceitos: usuarios, enderecos, categorias, marcas, produtos, produto-imagens, "
            + "tamanhos, cores, modelos, produto-variacoes, movimentacoes-estoque, pedidos, pedido-itens, "
            + "pedido-status-historicos, configuracoes-loja, favoritos.";
    private static final String SEM_PERMISSAO = "Acesso negado: apenas perfis ADMINISTRADOR ou VENDEDOR";

    private final AdminCrudService adminCrudService;

    public AdminCrudController(AdminCrudService adminCrudService) {
        this.adminCrudService = adminCrudService;
    }

    @GetMapping("/{tipo}")
    @Operation(summary = "Listar registros", description = "Retorna todos os registros do tipo informado." + TIPOS)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de registros do tipo informado"),
            @ApiResponse(responseCode = "400", description = "Tipo de recurso inválido"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO)
    })
    public List<?> listar(@PathVariable String tipo) {
        return adminCrudService.listar(tipo);
    }

    @GetMapping("/{tipo}/{id}")
    @Operation(summary = "Buscar registro por id", description = "Retorna o registro do tipo informado identificado pelo id." + TIPOS)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "400", description = "Tipo de recurso inválido ou registro não encontrado"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO)
    })
    public Object buscar(@PathVariable String tipo, @PathVariable Long id) {
        return adminCrudService.buscar(tipo, id);
    }

    @PostMapping("/{tipo}")
    @Operation(summary = "Criar registro", description = "Cria um novo registro do tipo informado com os dados fornecidos no corpo da requisição." + TIPOS)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro criado"),
            @ApiResponse(responseCode = "400", description = "Tipo de recurso inválido ou dados do corpo inválidos"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO)
    })
    public Object criar(@PathVariable String tipo, @RequestBody Object body) {
        return adminCrudService.criar(tipo, body);
    }

    @PutMapping("/{tipo}/{id}")
    @Operation(summary = "Atualizar registro", description = "Atualiza o registro do tipo informado identificado pelo id com os dados do corpo da requisição." + TIPOS)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro atualizado"),
            @ApiResponse(responseCode = "400", description = "Tipo de recurso inválido, registro não encontrado ou dados do corpo inválidos"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO)
    })
    public Object atualizar(@PathVariable String tipo, @PathVariable Long id, @RequestBody Object body) {
        return adminCrudService.atualizar(tipo, id, body);
    }

    @DeleteMapping("/{tipo}/{id}")
    @Operation(summary = "Excluir registro", description = "Exclui o registro do tipo informado identificado pelo id." + TIPOS)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro excluído"),
            @ApiResponse(responseCode = "400", description = "Tipo de recurso inválido ou registro não encontrado"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO)
    })
    public void excluir(@PathVariable String tipo, @PathVariable Long id) {
        adminCrudService.excluir(tipo, id);
    }
}
