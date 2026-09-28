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

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerApiTest {
    private static final String RISK = """
            {"score":91,"reasons":"Revisao atrasada e garantia vencendo","recommendedAction":"Oferecer revisao",
             "modelName":"churn-gbm","modelVersion":"2.1.0"}""";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private JsonNode createCustomer(String email, String vin) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.customerJson(email, vin, true)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/customers/")))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void createdCustomerIsReachableThroughLocationHeader() throws Exception {
        String vin = TestData.vin();
        JsonNode created = createCustomer(TestData.uniqueEmail(), vin);
        mockMvc.perform(get("/api/v1/customers/{id}", created.at("/customer/id").asText())
                        .with(jwt().authorities(() -> "ROLE_ADVISOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicle.vin").value(vin));
    }

    @Test
    void duplicatedEmailReturnsConflict() throws Exception {
        String email = TestData.uniqueEmail();
        createCustomer(email, TestData.vin());
        mockMvc.perform(post("/api/v1/customers")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.customerJson(email, TestData.vin(), true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Resource conflict"));
    }

    @Test
    void invalidVinReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.customerJson(TestData.uniqueEmail(), "INVALID-VIN", true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.vin").exists());
    }

    @Test
    void unknownCustomerReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/customers/{id}", UUID.randomUUID())
                        .with(jwt().authorities(() -> "ROLE_MANAGER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void modelInferenceIsStoredAndExposedAsResource() throws Exception {
        JsonNode created = createCustomer(TestData.uniqueEmail(), TestData.vin());
        String customerId = created.at("/customer/id").asText();
        String vehicleId = created.at("/vehicle/id").asText();

        MvcResult result = mockMvc.perform(post("/api/v1/customers/{c}/vehicles/{v}/risk-assessments", customerId, vehicleId)
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON).content(RISK))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value("CRITICAL"))
                .andExpect(jsonPath("$.modelVersion").value("2.1.0"))
                .andReturn();
        String location = result.getResponse().getHeader("Location");

        mockMvc.perform(get(location).with(jwt().authorities(() -> "ROLE_ADVISOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(91));
        mockMvc.perform(get("/api/v1/customers/{c}/vehicles/{v}/risk-assessments", customerId, vehicleId)
                        .with(jwt().authorities(() -> "ROLE_ADVISOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/customers").param("riskLevel", "CRITICAL")
                        .with(jwt().authorities(() -> "ROLE_ADVISOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.customer.id == '" + customerId + "')]").exists());
    }

    @Test
    void assessmentForVehicleOfAnotherCustomerIsRejected() throws Exception {
        JsonNode first = createCustomer(TestData.uniqueEmail(), TestData.vin());
        JsonNode second = createCustomer(TestData.uniqueEmail(), TestData.vin());
        mockMvc.perform(post("/api/v1/customers/{c}/vehicles/{v}/risk-assessments",
                        first.at("/customer/id").asText(), second.at("/vehicle/id").asText())
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON).content(RISK))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Vehicle not found for this customer"));
    }

    @Test
    void advisorCannotRegisterModelInference() throws Exception {
        JsonNode created = createCustomer(TestData.uniqueEmail(), TestData.vin());
        mockMvc.perform(post("/api/v1/customers/{c}/vehicles/{v}/risk-assessments",
                        created.at("/customer/id").asText(), created.at("/vehicle/id").asText())
                        .with(jwt().authorities(() -> "ROLE_ADVISOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(RISK))
                .andExpect(status().isForbidden());
    }

    @Test
    void scoreOutOfRangeIsRejected() throws Exception {
        JsonNode created = createCustomer(TestData.uniqueEmail(), TestData.vin());
        mockMvc.perform(post("/api/v1/customers/{c}/vehicles/{v}/risk-assessments",
                        created.at("/customer/id").asText(), created.at("/vehicle/id").asText())
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON).content(RISK.replace("91", "150")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.score").exists());
    }
}
