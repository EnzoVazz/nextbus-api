package br.com.fiap.nextbus.controller;

import br.com.fiap.nextbus.dto.LoginRequest;
import br.com.fiap.nextbus.dto.RegistrarRequest;
import br.com.fiap.nextbus.dto.TokenResponse;
import br.com.fiap.nextbus.dto.UsuarioResponse;
import br.com.fiap.nextbus.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "1. Acesso", description = "Cadastro, login e dados de quem está autenticado")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registrar")
    @SecurityRequirements
    @Operation(summary = "Cria a conta do cidadão", description = "Cadastro público. O papel gravado é CIDADAO. A senha fica só como hash.")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistrarRequest request) {
        return ResponseEntity.status(201).body(authService.registrar(request));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Entra com email e senha", description = "Devolve o JWT. Envie o token no cabeçalho Authorization: Bearer.")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/eu")
    @Operation(summary = "Mostra o usuário da sessão", description = "Exige o JWT. Vale para cidadão e gestor.")
    public UsuarioResponse eu() {
        return authService.eu();
    }
}
