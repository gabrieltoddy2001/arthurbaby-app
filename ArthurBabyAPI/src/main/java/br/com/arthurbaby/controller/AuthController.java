package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login, cadastro de clientes e recuperação de senha. Endpoints públicos (não exigem token).")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica o usuário com email e senha e retorna o token JWT e os dados do usuário.")
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

    @PostMapping("/recuperar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Recuperar senha", description = "Gera um token de redefinição de senha válido por 30 minutos para o email informado. Responde 204 mesmo se o email não existir.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Solicitação recebida; se o email existir, o token de redefinição foi gerado"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido ou mal formatado")
    })
    public void recuperarSenha(@RequestBody RecuperarSenhaRequest request) { auth.recuperarSenha(request.email()); }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Redefinir senha", description = "Define uma nova senha a partir do token de recuperação recebido.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token vazio ou expirado, ou nova senha com menos de 4 caracteres"),
            @ApiResponse(responseCode = "404", description = "Token de recuperação não encontrado")
    })
    public void redefinirSenha(@RequestBody RedefinirSenhaRequest request) {
        auth.redefinirSenha(request.token(), request.novaSenha());
    }
}
