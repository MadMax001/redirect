package ru.broker.redirect.controller.dev

import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import ru.broker.redirect.TestConstants.Companion.ERROR_TEXT
import ru.broker.redirect.config.Constants.Companion.DEV_AUTHENTIFICATION_ERROR_ANSWER
import ru.broker.redirect.config.Constants.Companion.DEV_ERROR_ANSWER
import ru.broker.redirect.config.SecurityConfiguration
import ru.broker.redirect.config.SecurityDevConfiguration
import ru.broker.redirect.dao.RequestDao
import ru.broker.redirect.service.Redirector

@ActiveProfiles("dev")
@WebMvcTest(MainControllerDev::class)
@Import(SecurityDevConfiguration::class, SecurityConfiguration::class, GlobalExceptionHandlerDev::class)
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
                        "$DEV_AUTHENTIFICATION_ERROR_ANSWER $errorUrl"
                    )
                ))
    }

    @Test
    fun anyExceptionInProcessLeadsToDefaultRedirectTest() {
        val error = RuntimeException(ERROR_TEXT)
        whenever(redirector.buildRedirectUrl(Mockito.any()))
            .thenThrow(error)

        mockMvc.perform(get("/v1/redirect"))
            .andExpect(status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().string(
                    Matchers.containsString(
                        "$DEV_ERROR_ANSWER $errorUrl"
                    )
                ))
    }
}
