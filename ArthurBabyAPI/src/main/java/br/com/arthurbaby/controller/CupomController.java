package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.CupomValidacaoRequest;
import br.com.arthurbaby.dto.CupomValidacaoResponse;
import br.com.arthurbaby.service.CupomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/** Prévia de cupom para o carrinho do app; o valor definitivo é recalculado ao criar o pedido. */
@RestController
@RequestMapping(value = "/api/cupons", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Cupons", description = "Validação de cupons de desconto cadastrados pelo admin. Endpoint público.")
public class CupomController {
    private final CupomService service;

    public CupomController(CupomService service) {
        this.service = service;
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
