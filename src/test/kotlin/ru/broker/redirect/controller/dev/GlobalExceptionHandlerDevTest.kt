package ru.broker.redirect.controller.dev

import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import ru.broker.redirect.dao.RequestDao
import ru.broker.redirect.service.Redirector

@ActiveProfiles("dev")
@WebMvcTest(MainControllerDev::class)
@Import(GlobalExceptionHandlerDev::class)
class GlobalExceptionHandlerDevTest {
    private val errorUrl = "https://example.com/error"

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var redirector: Redirector

    @MockitoBean
    @Suppress("unused")
    lateinit var dao: RequestDao

            @Test
    fun notFoundRequestLeadsToDefaultRedirectTest() {
        mockMvc.perform(get("/non-existing-path"))
            .andExpect(status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().string(
                    Matchers.containsString(
                        "Запрос на несуществующий адрес. Перенаправление на $errorUrl"
                    )
                ))
    }

    @Test
    fun anyExceptionInProcessLeadsToDefaultRedirectTest() {
        val error = RuntimeException("Что-то пошло не так")
        `when`(redirector.buildRedirectUrl(Mockito.any()))
            .thenThrow(error)

        mockMvc.perform(get("/v1/redirect"))
            .andExpect(status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().string(
                    Matchers.containsString(
                        "Ошибка в процессе обработки. Перенаправление на $errorUrl"
                    )
                ))
    }
}
