package br.com.fiap.predit.identity.api;

import br.com.fiap.predit.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login e emissao de JWT")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica o usuario e emite um JWT valido por 30 minutos (endpoint publico)")
    @ApiResponse(responseCode = "200", description = "Token emitido com sub, iss, iat, exp, email, name e roles")
    @ApiResponse(responseCode = "400", description = "Payload invalido")
    @ApiResponse(responseCode = "401", description = "E-mail ou senha invalidos")
    @ApiResponse(responseCode = "429", description = "Limite de requisicoes excedido")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
