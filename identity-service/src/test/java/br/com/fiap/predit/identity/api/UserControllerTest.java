package br.com.fiap.predit.identity.api;

import br.com.fiap.predit.identity.domain.Role;
import br.com.fiap.predit.identity.domain.User;
import br.com.fiap.predit.identity.domain.UserRepository;
import br.com.fiap.predit.identity.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Usa tokens reais emitidos pelo JwtService, validados pelo JwtDecoder da aplicacao. */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    private static final String NEW_USER = """
            {"name":"Consultor","email":"%s","password":"Strong123!x","role":"ADVISOR"}""";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository repository;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwtService;

    private String adminToken;
    private String managerToken;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        User admin = repository.save(new User("Admin", "admin@predit.com.br", encoder.encode("Strong123!"), Role.ADMIN));
        User manager = repository.save(new User("Manager", "manager@predit.com.br", encoder.encode("Strong123!"), Role.MANAGER));
        adminToken = "Bearer " + jwtService.issue(admin).value();
        managerToken = "Bearer " + jwtService.issue(manager).value();
    }

    @Test
    void adminCreatesUserReachableThroughLocation() throws Exception {
        String location = mockMvc.perform(post("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_USER.formatted("advisor@predit.com.br")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/users/")))
                .andExpect(jsonPath("$.role").value("ADVISOR"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location).header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("advisor@predit.com.br"));
    }

    @Test
    void duplicatedEmailReturnsConflict() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_USER.formatted("manager@predit.com.br")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Resource conflict"));
    }

    @Test
    void weakPasswordReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X Y\",\"email\":\"x@predit.com.br\",\"password\":\"weak\",\"role\":\"ADVISOR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void managerCannotManageUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, managerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access denied"));
    }

    @Test
    void anonymousCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication required"));
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, adminToken + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        JwtService expiredIssuer = new JwtService("unit-test-secret-with-at-least-32-characters-long",
                "predit-identity", -5);
        User admin = repository.findByEmailIgnoreCase("admin@predit.com.br").orElseThrow();
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredIssuer.issue(admin).value()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownUserReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }
}
