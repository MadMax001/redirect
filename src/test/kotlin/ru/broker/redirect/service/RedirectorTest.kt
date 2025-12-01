package ru.broker.redirect.service

import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import ru.broker.redirect.TestConstants.Companion.ERROR_TEXT
import ru.broker.redirect.config.Constants.Companion.DECRYPTION_ERROR_TEXT
import ru.broker.redirect.config.Constants.Companion.FINISH_REDIRECT_PROCESS
import ru.broker.redirect.config.Constants.Companion.START_REDIRECT_PROCESS
import java.util.UUID

@ExtendWith(MockitoExtension::class, OutputCaptureExtension::class)
class RedirectorTest {
    private val decryptedUrl = "https://decrypted.ru"
    @Mock
    private lateinit var encryptor: SymmetricEncryptor

    @InjectMocks
    private lateinit var redirector: Redirector

    @Mock
    private lateinit var mockRequest: HttpServletRequest

    @Test
    fun buildRedirectUrlTest(capturedOutput: CapturedOutput) {
        whenever(encryptor.decryptUrl(any(), any())).thenReturn(decryptedUrl)
        val gpbId = UUID.randomUUID().toString()
        val httpRequest = buildHttpRequest(gpbId)

        val url = redirector.buildRedirectUrl(httpRequest)

        assertThat(url).isEqualTo(decryptedUrl)
        assertThat(capturedOutput.all)
            .contains(START_REDIRECT_PROCESS)
            .contains(FINISH_REDIRECT_PROCESS)
            .contains(gpbId)
            .contains(decryptedUrl)
    }

    @Test
    fun buildRedirectUrlThrowsExceptionTest(capturedOutput: CapturedOutput) {
        val error = RuntimeException(ERROR_TEXT)
        whenever(encryptor.decryptUrl(any(), any())).thenThrow(error)
        val gpbId = UUID.randomUUID().toString()
        val httpRequest = buildHttpRequest(gpbId)

        assertThatThrownBy{ redirector.buildRedirectUrl(httpRequest) }
            .isExactlyInstanceOf(RuntimeException::class.java)
            .hasMessageContaining(ERROR_TEXT)

        assertThat(capturedOutput.all)
            .contains(START_REDIRECT_PROCESS)
            .contains(gpbId)
            .doesNotContain(FINISH_REDIRECT_PROCESS)
    }

    @Test
    fun buildRedirectUrlReturnsNullTest(capturedOutput: CapturedOutput) {
        whenever(encryptor.decryptUrl(any(), any())).thenReturn(null)
        val gpbId = UUID.randomUUID().toString()
        val httpRequest = buildHttpRequest(gpbId)

        assertThatThrownBy{ redirector.buildRedirectUrl(httpRequest) }
            .isExactlyInstanceOf(RuntimeException::class.java)
            .hasMessageContaining(DECRYPTION_ERROR_TEXT)

        assertThat(capturedOutput.all)
            .contains(START_REDIRECT_PROCESS)
            .contains(gpbId)
            .doesNotContain(FINISH_REDIRECT_PROCESS)
    }

    @Test
    fun wrongGpbIdTest(capturedOutput: CapturedOutput) {
        whenever(encryptor.decryptUrl(any(), any())).thenReturn(decryptedUrl)
        val incorrectGpbId = "sdsfskdjgldfkgjsdlfg"
        val httpRequest = buildHttpRequest(incorrectGpbId)

        val url = redirector.buildRedirectUrl(httpRequest)

        assertThat(url).isEqualTo(decryptedUrl)
        assertThat(capturedOutput.all)
            .contains(START_REDIRECT_PROCESS)
            .contains(FINISH_REDIRECT_PROCESS)
            .contains(decryptedUrl)
            .contains(incorrectGpbId)
    }

    @Test
    fun emptyGpbIdTest(capturedOutput: CapturedOutput) {
        whenever(encryptor.decryptUrl(any(), any())).thenReturn(decryptedUrl)
        val httpRequest = buildHttpRequest(null)

        val url = redirector.buildRedirectUrl(httpRequest)

        assertThat(url).isEqualTo(decryptedUrl)
        assertThat(capturedOutput.all)
            .contains(START_REDIRECT_PROCESS)
            .contains(FINISH_REDIRECT_PROCESS)
            .contains(decryptedUrl)
            .contains("[null]")
    }


    private fun buildHttpRequest(gpbId: String?): HttpServletRequest {
        whenever(mockRequest.getParameter("p1")).thenReturn("a")
        whenever(mockRequest.getParameter("p2")).thenReturn("b")
        gpbId?.run {
            whenever(mockRequest.getParameter("p3")).thenReturn(gpbId)
        }
        return mockRequest
    }
}