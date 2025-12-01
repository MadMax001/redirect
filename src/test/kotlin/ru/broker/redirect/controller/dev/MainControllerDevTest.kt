package ru.broker.redirect.controller.dev

import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import ru.broker.redirect.service.Redirector
import java.util.UUID

@ActiveProfiles("dev")
@WebMvcTest(controllers = [MainControllerDev::class])
class MainControllerDevTest {
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
        whenever(redirector.buildRedirectUrl(any()))
            .thenReturn(expected)

        mockMvc.perform(
            MockMvcRequestBuilders.get(path)
            .param("p1", "a")
            .param("p2", "b")
            .param("p3",gpbId))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(Matchers.containsString("[$gpbId]. Перенаправление на $expected")))
    }

    @Test
    fun requestForRedirectThrowsExceptionTest() {
        val gpbId = UUID.randomUUID().toString()
        whenever(redirector.buildRedirectUrl(any()))
            .thenThrow(RuntimeException("Что-то пошло не так"))

        mockMvc.perform(
            MockMvcRequestBuilders.get(path)
            .param("p1", "a")
            .param("p2", "b")
            .param("p3", gpbId))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().string(
                    Matchers.containsString(
                        "Ошибка в процессе обработки. Перенаправление на $errorUrl"
                    )
                ))
    }

    @Test
    fun redirectProcessReturnEmptyUrlForRedirectionTest() {
        val gpbId = UUID.randomUUID().toString()
        val error = RuntimeException("[$gpbId]. Не удалось расшифровать ссылку")
        whenever(redirector.buildRedirectUrl(any()))
            .thenThrow(error)

        mockMvc.perform(
            MockMvcRequestBuilders.get(path)
            .param("p3", gpbId))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().string(
                    Matchers.containsString(
                        "Ошибка в процессе обработки. Перенаправление на $errorUrl"
                    )
                ))
    }
}