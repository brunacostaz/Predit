package br.com.fiap.predit.identity.api;

import br.com.fiap.predit.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users", description = "Gestao de usuarios (somente ADMIN)")
@ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado")
@ApiResponse(responseCode = "403", description = "Perfil diferente de ADMIN")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    public List<UserResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um usuario")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "Usuario inexistente")
    public UserResponse find(@PathVariable UUID id) {
        return service.find(id);
    }

    @PostMapping
    @Operation(summary = "Cria usuario com perfil ADMIN, MANAGER ou ADVISOR")
    @ApiResponse(responseCode = "201", description = "Usuario criado; header Location aponta para o recurso")
    @ApiResponse(responseCode = "400", description = "Payload invalido ou senha fraca")
    @ApiResponse(responseCode = "409", description = "E-mail ja cadastrado")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }
}
