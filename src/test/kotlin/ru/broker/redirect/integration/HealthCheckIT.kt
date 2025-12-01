package ru.broker.redirect.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.broker.redirect.TestcontainersConfiguration
import java.util.Base64

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HealthCheckIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun healthCheckWithCorrectAuthTest() {
        val userName = "dev-user"
        val userPassword = "dev-password"
        val authByteArray = Base64.getEncoder().encodeToString(("$userName:$userPassword").toByteArray())
        val authHeader = "Basic $authByteArray"

        mockMvc.perform(
            get("/e67d4c74-dfcd-4c24-94f9-7540d4d07f3b/check")
                .header(HttpHeaders.AUTHORIZATION, authHeader))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.components.livenessState.status").value("UP"))
            .andExpect(jsonPath("$.components.readinessState.status").value("UP"))
    }

    @Test
    fun healthCheckWithWrongAuthTest() {
        val userName = "dev-user"
        val userPassword = "XXX"
        val authByteArray = Base64.getEncoder().encodeToString(("$userName:$userPassword").toByteArray())
        val authHeader = "Basic $authByteArray"

        mockMvc.perform(
            get("/e67d4c74-dfcd-4c24-94f9-7540d4d07f3b/check")
                .header(HttpHeaders.AUTHORIZATION, authHeader))
            .andExpect(status().isUnauthorized)
    }
}