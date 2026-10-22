package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.CupomResponse;
import br.com.arthurbaby.dto.CupomValidacaoRequest;
import br.com.arthurbaby.dto.CupomValidacaoResponse;
import br.com.arthurbaby.service.CupomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cupons para o app: lista dos vigentes ("Meus Cupons") e prévia de desconto no carrinho.
 * O valor definitivo do desconto é sempre recalculado ao criar o pedido.
 */
@RestController
@RequestMapping(value = "/api/cupons", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Cupons", description = "Listagem e validação de cupons de desconto cadastrados pelo admin. Endpoints públicos.")
public class CupomController {
    private final CupomService service;

    public CupomController(CupomService service) {
        this.service = service;
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar cupons ativos", description = "Retorna os cupons ativos e dentro da validade agora (validoDe já alcançado e validoAte "
            + "não vencido; datas nulas não limitam). Usado na tela \"Meus Cupons\" do app.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de cupons vigentes (vazia se não houver)")
    })
    public List<CupomResponse> ativos() {
        return service.listarAtivos();
    }

    @PostMapping("/validar")
    @Operation(summary = "Validar cupom", description = "Verifica se o cupom existe, está ativo, dentro da validade e atende ao subtotal mínimo, "
            + "e calcula o desconto sobre o subtotal informado. Apenas prévia: o pedido recalcula tudo no servidor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado da validação; se \"valido\" for false, \"motivo\" explica a recusa"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido ou mal formatado")
    })
    public CupomValidacaoResponse validar(@RequestBody CupomValidacaoRequest request) {
        return service.validar(request.codigo(), request.subtotal());
    }
}
