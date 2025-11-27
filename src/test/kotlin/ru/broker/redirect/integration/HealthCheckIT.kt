package ru.broker.redirect.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.broker.redirect.TestcontainersConfiguration

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HealthCheckIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun healthCheckTest() {
        mockMvc.perform(
            get("/actuator/health"))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.components.livenessState.status").value("UP"))
            .andExpect(jsonPath("$.components.readinessState.status").value("UP"))
    }
}