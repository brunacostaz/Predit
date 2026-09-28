package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.support.TestData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LeadApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private JsonNode createCustomer(boolean consent) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.customerJson(TestData.uniqueEmail(), TestData.vin(), consent)))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String leadJson(String customerId, String vehicleId) {
        return """
                {"customerId":"%s","vehicleId":"%s","title":"Revisao atrasada","action":"Ligar e oferecer revisao",
                 "priority":"HIGH"}""".formatted(customerId, vehicleId);
    }

    private String createLead() throws Exception {
        JsonNode customer = createCustomer(true);
        MvcResult result = mockMvc.perform(post("/api/v1/leads")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leadJson(customer.at("/customer/id").asText(), customer.at("/vehicle/id").asText())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();
        return result.getResponse().getHeader("Location");
    }

    @Test
    void managerCreatesLeadReachableThroughLocation() throws Exception {
        String location = createLead();
        mockMvc.perform(get(location).with(jwt().authorities(() -> "ROLE_ADVISOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void advisorCanUpdateLeadStatus() throws Exception {
        String location = createLead();
        mockMvc.perform(patch(location + "/status")
                        .with(jwt().authorities(() -> "ROLE_ADVISOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONTACTED\",\"assignedTo\":\"consultor@predit.com.br\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONTACTED"))
                .andExpect(jsonPath("$.assignedTo").value("consultor@predit.com.br"));
    }

    @Test
    void advisorCannotCreateLead() throws Exception {
        JsonNode customer = createCustomer(true);
        mockMvc.perform(post("/api/v1/leads")
                        .with(jwt().authorities(() -> "ROLE_ADVISOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leadJson(customer.at("/customer/id").asText(), customer.at("/vehicle/id").asText())))
                .andExpect(status().isForbidden());
    }

    @Test
    void leadRequiresContactConsent() throws Exception {
        JsonNode customer = createCustomer(false);
        mockMvc.perform(post("/api/v1/leads")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leadJson(customer.at("/customer/id").asText(), customer.at("/vehicle/id").asText())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Business rule violated"));
    }

    @Test
    void leadRejectsVehicleOfAnotherCustomer() throws Exception {
        JsonNode first = createCustomer(true);
        JsonNode second = createCustomer(true);
        mockMvc.perform(post("/api/v1/leads")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leadJson(first.at("/customer/id").asText(), second.at("/vehicle/id").asText())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Vehicle does not belong to customer"));
    }

    @Test
    void unknownLeadReturnsNotFound() throws Exception {
        mockMvc.perform(patch("/api/v1/leads/{id}/status", UUID.randomUUID())
                        .with(jwt().authorities(() -> "ROLE_ADVISOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONTACTED\"}"))
                .andExpect(status().isNotFound());
    }
}
