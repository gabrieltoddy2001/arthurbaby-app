package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) { return auth.login(request); }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse cadastro(@RequestBody CadastroRequest request) { return auth.cadastrar(request); }

    @PostMapping("/recuperar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recuperarSenha(@RequestBody RecuperarSenhaRequest request) { auth.recuperarSenha(request.email()); }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(@RequestBody RedefinirSenhaRequest request) {
        auth.redefinirSenha(request.token(), request.novaSenha());
    }
}
