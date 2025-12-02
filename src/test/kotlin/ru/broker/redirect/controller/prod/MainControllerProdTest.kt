package ru.broker.redirect.controller.prod

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import ru.broker.redirect.config.Constants
import ru.broker.redirect.config.SecurityConfiguration
import ru.broker.redirect.config.SecurityProdConfiguration
import ru.broker.redirect.service.Redirector
import java.util.UUID

@ActiveProfiles("prod")
@WebMvcTest(controllers = [MainControllerProd::class])
@Import(SecurityProdConfiguration::class, SecurityConfiguration::class)
class  MainControllerProdTest {
    private val errorUrl = "https://example.com/error"
    private val path = "/v1/redirect"

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var redirector: Redirector

    @Test
    fun requestForRedirectTest() {
        val expected = "https://example.com/success"
        val gpbId = UUID.randomUUID().toString()
        whenever(redirector.buildRedirectUrl(ArgumentMatchers.any())).thenReturn(expected)

        mockMvc.perform(
            MockMvcRequestBuilders.get(path)
            .param("p2", "b")
            .param("p1", "a")
            .param("p3", gpbId))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
            .andExpect(MockMvcResultMatchers.redirectedUrl(expected))
    }

    @Test
    fun requestForRedirectThrowsExceptionTest() {
        val gpbId = UUID.randomUUID().toString()
        whenever(redirector.buildRedirectUrl(ArgumentMatchers.any()))
            .thenThrow(RuntimeException("Что-то пошло не так"))

        mockMvc.perform(
            MockMvcRequestBuilders.get(path)
            .param("p1", "a")
            .param("p2", "b")
            .param("p3", gpbId))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
            .andExpect(MockMvcResultMatchers.redirectedUrl(errorUrl))
    }

    @Test
    fun redirectProcessReturnEmptyUrlForRedirectionTest() {
        val gpbId = UUID.randomUUID().toString()
        val error = RuntimeException("[$gpbId]. ${Constants.Companion.DECRYPTION_ERROR_TEXT}")
        whenever(redirector.buildRedirectUrl(ArgumentMatchers.any())).thenThrow(error)

        mockMvc.perform(
            MockMvcRequestBuilders.get(path)
            .param("p3", gpbId))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
            .andExpect(MockMvcResultMatchers.redirectedUrl(errorUrl))
    }
}