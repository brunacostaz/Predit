package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.support.TestTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Valida o JwtDecoder real do Risk Service: assinatura, emissor, expiracao e claim de perfis. */
@SpringBootTest
@AutoConfigureMockMvc
class JwtValidationTest {
    private static final String SUMMARY = "/api/v1/dashboard/summary";
    private static final String CAMPAIGN = """
            {"name":"Revisao","description":"Contato","segment":"Revisao atrasada","consentRequired":true,
             "eligibleCustomers":10,"estimatedConversionRate":20.0,"estimatedRevenue":1000.0}""";

    @Autowired MockMvc mockMvc;

    @Test
    void acceptsValidTokenSignedByIdentityService() throws Exception {
        mockMvc.perform(get(SUMMARY).header(HttpHeaders.AUTHORIZATION, TestTokens.bearer("ADVISOR")))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsMissingToken() throws Exception {
        mockMvc.perform(get(SUMMARY))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        String expired = TestTokens.sign(TestTokens.SECRET, TestTokens.ISSUER, List.of("ADMIN"),
                Instant.now().minus(Duration.ofMinutes(5)));
        mockMvc.perform(get(SUMMARY).header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication required"));
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() throws Exception {
        String forged = TestTokens.sign("another-secret-with-at-least-32-characters-long!", TestTokens.ISSUER,
                List.of("ADMIN"), Instant.now().plus(Duration.ofMinutes(30)));
        mockMvc.perform(get(SUMMARY).header(HttpHeaders.AUTHORIZATION, "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsTamperedPayload() throws Exception {
        String[] parts = TestTokens.valid("ADVISOR").split("\\.");
        String adminPayload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                new String(java.util.Base64.getUrlDecoder().decode(parts[1]))
                        .replace("ADVISOR", "ADMIN").getBytes());
        mockMvc.perform(get(SUMMARY).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + parts[0] + "." + adminPayload + "." + parts[2]))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsTokenFromUnknownIssuer() throws Exception {
        String foreign = TestTokens.sign(TestTokens.SECRET, "another-issuer", List.of("ADMIN"),
                Instant.now().plus(Duration.ofMinutes(30)));
        mockMvc.perform(get(SUMMARY).header(HttpHeaders.AUTHORIZATION, "Bearer " + foreign))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMalformedToken() throws Exception {
        mockMvc.perform(get(SUMMARY).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithoutRolesCannotReadProtectedResources() throws Exception {
        String noRoles = TestTokens.sign(TestTokens.SECRET, TestTokens.ISSUER, null,
                Instant.now().plus(Duration.ofMinutes(30)));
        mockMvc.perform(get(SUMMARY).header(HttpHeaders.AUTHORIZATION, "Bearer " + noRoles))
                .andExpect(status().isForbidden());
    }

    @Test
    void rolesClaimDrivesAuthorization() throws Exception {
        mockMvc.perform(post("/api/v1/campaigns").header(HttpHeaders.AUTHORIZATION, TestTokens.bearer("ADVISOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(CAMPAIGN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access denied"));
        mockMvc.perform(post("/api/v1/campaigns").header(HttpHeaders.AUTHORIZATION, TestTokens.bearer("MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON).content(CAMPAIGN))
                .andExpect(status().isCreated());
    }
}
