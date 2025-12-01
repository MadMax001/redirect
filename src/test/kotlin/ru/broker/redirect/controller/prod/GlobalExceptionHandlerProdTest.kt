package ru.broker.redirect.controller.prod

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.context.bean.override.mockito.MockitoBean
import ru.broker.redirect.dao.RequestDao
import ru.broker.redirect.service.Redirector

@ActiveProfiles("prod")
@WebMvcTest(MainControllerProd::class)
@Import(GlobalExceptionHandlerProd::class)
class GlobalExceptionHandlerProdTest {
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
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(errorUrl))
    }

    @Test
    fun anyExceptionInProcessLeadsToDefaultRedirectTest() {
        val error = RuntimeException("Что-то пошло не так")
        whenever(redirector.buildRedirectUrl(Mockito.any()))
            .thenThrow(error)

        mockMvc.perform(get("/v1/redirect"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(errorUrl))
    }
}
