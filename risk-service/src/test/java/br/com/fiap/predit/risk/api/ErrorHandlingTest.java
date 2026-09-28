package br.com.fiap.predit.risk.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Garante que todo erro sai como application/problem+json com status coerente e sem detalhes internos. */
@SpringBootTest
@AutoConfigureMockMvc
class ErrorHandlingTest {
    @Autowired MockMvc mockMvc;

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/campaigns")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Malformed request"))
                .andExpect(jsonPath("$.instance").value("/api/v1/campaigns"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void invalidUuidReturnsBadRequestWithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/api/v1/customers/not-a-uuid").with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid parameter"))
                .andExpect(jsonPath("$.errors.id").value("invalid value"));
    }

    @Test
    void invalidEnumFilterDoesNotLeakClassNames() throws Exception {
        mockMvc.perform(get("/api/v1/customers").param("riskLevel", "XYZ").with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", not(containsString("br.com.fiap"))));
    }

    @Test
    void unsupportedMethodReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/v1/campaigns").with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Method not allowed"));
    }

    @Test
    void unknownEndpointReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/does-not-exist").with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void unsupportedMediaTypeReturns415() throws Exception {
        mockMvc.perform(post("/api/v1/campaigns").with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
