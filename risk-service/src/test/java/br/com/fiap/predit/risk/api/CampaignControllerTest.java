package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.api.CampaignDtos.CreateCampaignRequest;
import br.com.fiap.predit.risk.domain.CampaignRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CampaignControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CampaignRepository campaigns;

    @BeforeEach
    void clean() { campaigns.deleteAll(); }

    private CreateCampaignRequest validRequest() {
        return new CreateCampaignRequest("Garantia em risco", "Contato antes do fim da cobertura",
                "Garantia a vencer", true, 860, new BigDecimal("24.00"), new BigDecimal("2400000.00"));
    }

    @Test
    void managerCanCreateCampaign() throws Exception {
        mockMvc.perform(post("/api/v1/campaigns")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.consentRequired").value(true));
    }

    @Test
    void advisorCannotCreateCampaign() throws Exception {
        mockMvc.perform(post("/api/v1/campaigns")
                        .with(jwt().authorities(() -> "ROLE_ADVISOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access denied"));
    }

    @Test
    void invalidCampaignReturnsProblemDetail() throws Exception {
        CreateCampaignRequest invalid = new CreateCampaignRequest("", "", "", true, -1, null, null);
        mockMvc.perform(post("/api/v1/campaigns")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    void missingCampaignReturnsNotFound() throws Exception {
        mockMvc.perform(patch("/api/v1/campaigns/{id}/status", UUID.randomUUID())
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void createdCampaignIsReachableAndCanBeActivated() throws Exception {
        String location = mockMvc.perform(post("/api/v1/campaigns")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location).with(jwt().authorities(() -> "ROLE_ADVISOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Garantia em risco"));

        mockMvc.perform(patch(location + "/status")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.activatedAt").exists());
    }

    @Test
    void unknownCampaignReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/campaigns/{id}", UUID.randomUUID())
                        .with(jwt().authorities(() -> "ROLE_ADVISOR")))
                .andExpect(status().isNotFound());
    }
}
