package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.AuthService;
import br.com.arthurbaby.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login, cadastro de clientes e recuperação de senha (públicos) e dados do usuário logado (exige token).")
public class AuthController {
    static final String MENSAGEM_RECUPERACAO = "Se o e-mail estiver cadastrado, você receberá um código.";

    private final AuthService auth;
    private final PasswordResetService passwordReset;
    public AuthController(AuthService auth, PasswordResetService passwordReset) {
        this.auth = auth;
        this.passwordReset = passwordReset;
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica o usuário com email (ou CPF) e senha e retorna o token JWT e os dados do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado; retorna o token JWT e os dados do usuário"),
            @ApiResponse(responseCode = "400", description = "Usuário não encontrado, senha inválida ou conta inativa")
    })
    public AuthResponse login(@RequestBody LoginRequest request) { return auth.login(request); }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar cliente", description = "Cria a conta de um novo cliente com o endereço principal e retorna o token JWT já autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado; retorna o token JWT e os dados do usuário"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos: e-mail vazio ou já cadastrado, CPF inválido ou já cadastrado, ou corpo mal formatado")
    })
    public AuthResponse cadastro(@RequestBody CadastroRequest request) { return auth.cadastrar(request); }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário logado", description = "Retorna os dados cadastrais (e-mail, CPF, telefone, perfil) do dono do token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do usuário logado")
    })
    public ClienteResponse me(@AuthenticationPrincipal Usuario logado) {
        return ClienteResponse.from(logado);
    }

    @PostMapping("/recuperar-senha")
    @Operation(summary = "Recuperar senha", description = "Gera um token descartável de redefinição de senha, válido por 1 hora, e o envia ao e-mail "
            + "(em desenvolvimento o token é impresso no log). A resposta é a mesma exista ou não o e-mail e nunca contém o token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitação recebida: { \"mensagem\": \"" + MENSAGEM_RECUPERACAO + "\" }"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido ou mal formatado")
    })
    public MensagemResponse recuperarSenha(@RequestBody RecuperarSenhaRequest request) {
        passwordReset.solicitarRecuperacao(request.email());
        return new MensagemResponse(MENSAGEM_RECUPERACAO);
    }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Redefinir senha", description = "Define uma nova senha a partir do token de recuperação recebido. O token só pode ser usado uma vez.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido, já usado ou expirado, ou nova senha com menos de 4 caracteres")
    })
    public void redefinirSenha(@RequestBody RedefinirSenhaRequest request) {
        passwordReset.redefinirSenha(request.token(), request.novaSenha());
    }
}
