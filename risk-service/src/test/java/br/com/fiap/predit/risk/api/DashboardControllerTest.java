package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.domain.CustomerRepository;
import br.com.fiap.predit.risk.domain.LeadRepository;
import br.com.fiap.predit.risk.domain.RiskAssessmentRepository;
import br.com.fiap.predit.risk.domain.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired LeadRepository leads;
    @Autowired RiskAssessmentRepository risks;
    @Autowired VehicleRepository vehicles;
    @Autowired CustomerRepository customers;

    @BeforeEach
    void clean() {
        leads.deleteAll();
        risks.deleteAll();
        vehicles.deleteAll();
        customers.deleteAll();
    }

    @Test
    void shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication required"));
    }

    @Test
    void shouldReturnSummaryForManager() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .with(jwt().authorities(() -> "ROLE_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vinSharePercent").value(68.0))
                .andExpect(jsonPath("$.monitoredVehicles").value(0))
                .andExpect(jsonPath("$.customersAtRisk").value(0))
                .andExpect(jsonPath("$.riskDistribution").isArray())
                .andExpect(jsonPath("$.modelRisk").isArray());
    }
}
