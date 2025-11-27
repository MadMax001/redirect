package ru.broker.redirect.controller.prod

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.broker.redirect.service.Redirector
import java.util.UUID

@ActiveProfiles("prod")
@WebMvcTest(controllers = [MainControllerProd::class])
class MainControllerProdTest {
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
        `when`(redirector.buildRedirectUrl("a", "b", gpbId))
            .thenReturn(expected)

        mockMvc.perform(get(path)
            .param("p2", "b")
            .param("p1", "a")
            .param("p3", gpbId))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(expected))
    }

    @Test
    fun requestForRedirectThrowsExceptionTest() {
        val gpbId = UUID.randomUUID().toString()
        `when`(redirector.buildRedirectUrl("a", "b", gpbId))
            .thenThrow(RuntimeException("Что-то пошло не так"))

        mockMvc.perform(get(path)
            .param("p1", "a")
            .param("p2", "b")
            .param("p3", gpbId))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(errorUrl))
    }

    @Test
    fun redirectProcessReturnEmptyUrlForRedirectionTest() {
        val gpbId = UUID.randomUUID().toString()
        val error = RuntimeException("[$gpbId]. Не удалось расшифровать ссылку")
        `when`(redirector.buildRedirectUrl(null, null, gpbId))
            .thenThrow(error)

        mockMvc.perform(get(path)
            .param("p3", gpbId))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(errorUrl))
    }
}
