package br.com.fiap.predit.risk.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerAuthorizationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private CustomerDtos.CreateCustomerRequest request() {
        return new CustomerDtos.CreateCustomerRequest("Ana Lima", "ana@example.com", "+5511999999999",
                "Ford Lapa", true, "9BFABCD12R9999999", "Ranger", 2025, 1000,
                LocalDate.now().plusYears(3), LocalDate.now());
    }

    @Test
    void advisorCannotCreateCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .with(jwt().authorities(() -> "ROLE_ADVISOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanCreateCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .with(jwt().authorities(() -> "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated());
    }
}
