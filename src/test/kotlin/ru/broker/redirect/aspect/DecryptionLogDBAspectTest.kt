package ru.broker.redirect.aspect

import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import ru.broker.redirect.dao.RequestDao
import ru.broker.redirect.model.Request
import java.util.UUID
import java.util.stream.Stream
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class DecryptionLogDBAspectTest {
    private val errorUrl = "http://error"

    @Mock
    private lateinit var dao: RequestDao
    @Mock
    private lateinit var joinPoint: ProceedingJoinPoint
    private lateinit var aspect: DecryptionLogDBAspect

    @BeforeEach
    fun setUp() {
        aspect = DecryptionLogDBAspect(dao, errorUrl)
    }

    @ParameterizedTest
    @MethodSource("getValidHttpRequest")
    fun successProceedAndValidHttpRequestTest(httpRequest: HttpServletRequest, expected: Request) {
        `when`(joinPoint.proceed()).thenReturn(decryptedUrl)
        `when`(joinPoint.args).thenReturn(arrayOf(httpRequest))
        val requestCaptor = argumentCaptor<Request>()

        aspect.decryptUrlAroundWorker(joinPoint)

        verify(dao).save(requestCaptor.capture())
        val request : Request = requestCaptor.firstValue

        assertThat(request).isEqualTo(expected)
    }

    @Test
    fun processForEmptyHttRequestTest() {
        val expected = Request(decryptedUrl, null, null, null)
        `when`(joinPoint.proceed()).thenReturn(decryptedUrl)
        `when`(joinPoint.args).thenReturn(arrayOf(null))
        val requestCaptor = argumentCaptor<Request>()

        aspect.decryptUrlAroundWorker(joinPoint)

        verify(dao).save(requestCaptor.capture())
        val request : Request = requestCaptor.firstValue

        assertThat(request).isEqualTo(expected)
    }

    @Test
    fun processForEmptyArgumentsTest() {
        val expected = Request(decryptedUrl, null, null, null)
        `when`(joinPoint.proceed()).thenReturn(decryptedUrl)
        `when`(joinPoint.args).thenReturn(arrayOf())
        val requestCaptor = argumentCaptor<Request>()

        aspect.decryptUrlAroundWorker(joinPoint)

        verify(dao).save(requestCaptor.capture())
        val request : Request = requestCaptor.firstValue

        assertThat(request).isEqualTo(expected)
    }

    @Test
    fun errorInDecryptionProcessTest() {
        val expected = Request(errorUrl, gpbId, clientIp, clientAgent)
        val error = RuntimeException("Что-то пошло не так")
        `when`(joinPoint.proceed()).thenThrow(error)
        val httpRequest = buildHttpRequest(
            gpbIdStr, clientIp, null, null, clientAgent)
        `when`(joinPoint.args).thenReturn(arrayOf(httpRequest))
        val requestCaptor = argumentCaptor<Request>()

        aspect.decryptUrlAroundWorker(joinPoint)

        verify(dao).save(requestCaptor.capture())
        val request : Request = requestCaptor.firstValue

        assertThat(request).isEqualTo(expected)
    }


    companion object {
        private val gpbId = UUID.randomUUID()
        private val gpbIdStr = gpbId.toString()
        private val clientIp = "10.20.30.40"
        private val clientIpArray = "2.3.4.5,10.20.30.40"
        private val clientAgent = "Chrome-agent"
        private val decryptedUrl = "decryptedUrl"

        @JvmStatic
        private fun getValidHttpRequest(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    buildHttpRequest(gpbIdStr, clientIp, null, null, clientAgent),
                    Request(decryptedUrl, gpbId, clientIp, clientAgent)),
                arguments(
                    buildHttpRequest(gpbIdStr, null, clientIp, null, clientAgent),
                    Request(decryptedUrl, gpbId, clientIp, clientAgent)),
                arguments(
                    buildHttpRequest(gpbIdStr, "", clientIp, null, clientAgent),
                    Request(decryptedUrl, gpbId, clientIp, clientAgent)),
                arguments(
                    buildHttpRequest(gpbIdStr, null, clientIpArray, null, clientAgent),
                    Request(decryptedUrl, gpbId, "2.3.4.5", clientAgent)),
                arguments(
                    buildHttpRequest(gpbIdStr, null, null, clientIp, clientAgent),
                    Request(decryptedUrl, gpbId, clientIp, clientAgent)),
                arguments(
                    buildHttpRequest(gpbIdStr, clientIp, null, null, null),
                    Request(decryptedUrl, gpbId, clientIp, null)),
                arguments(
                    buildHttpRequest(gpbIdStr, null, null, null, clientAgent),
                    Request(decryptedUrl, gpbId, null, clientAgent)),
                arguments(
                    buildHttpRequest(null, clientIp, null, null, clientAgent),
                    Request(decryptedUrl, null, clientIp, clientAgent))
            )
        }

        private fun buildHttpRequest(gpbId: String?, remoteAddr: String?, xForwardHeader: String?, xRealHeader: String?, userAgentHeader: String?): HttpServletRequest {
            val mockRequest: HttpServletRequest = mock<HttpServletRequest>()
            gpbId?.run {
                `when`(mockRequest.getParameter("p3")).thenReturn(gpbId)
            }
            remoteAddr?.run {
                `when`(mockRequest.remoteAddr).thenReturn(remoteAddr)
            }
            xForwardHeader?.run {
                `when`(mockRequest.getHeader("X-Forwarded-For")).thenReturn(xForwardHeader)
            }
            xRealHeader?.run {
                `when`(mockRequest.getHeader("X-Real-IP")).thenReturn(xRealHeader)
            }
            userAgentHeader?.run {
                `when`(mockRequest.getHeader("User-Agent")).thenReturn(userAgentHeader)
            }
            return mockRequest
        }
    }


}