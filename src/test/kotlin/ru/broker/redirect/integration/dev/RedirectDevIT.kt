package ru.broker.redirect.integration.dev

import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import ru.broker.redirect.TestcontainersConfiguration
import java.util.UUID
import java.util.stream.Stream

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(properties = ["secrets.encryptionKey=test"])
@Import(TestcontainersConfiguration::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RedirectDevIT {


    val validPath = "/v1/redirect"

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbc: NamedParameterJdbcTemplate

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @ParameterizedTest
    @MethodSource("getNormalCases")
    fun normalRedirectProcessTest(p1: String, p2: String, p3: String?, expectedUrl: String) {

        mockMvc.perform(
            MockMvcRequestBuilders
            .get(validPath)
            .param("p1", p1)
            .param("p2", p2)
            .param("p3", p3))
            .andReturn()

        val sql = "SELECT count(*) FROM request WHERE url = :url AND request_id = :id"
        val id = UUID.fromString(p3)
        val params = MapSqlParameterSource().addValues(
            mapOf("url" to expectedUrl, "id" to id))
        Thread.sleep(500)
        val count = jdbc.queryForObject(sql, params, Int::class.java)
        Assertions.assertThat(count).isOne()
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @ParameterizedTest
    @MethodSource("getAbnormalCases")
    fun redirectProcessWithIncorrectGpbIdTest(p1: String, p2: String, p3: String?, expectedUrl: String) {

        mockMvc.perform(
            MockMvcRequestBuilders
            .get(validPath)
            .param("p1", p1)
            .param("p2", p2)
            .param("p3", p3))
            .andReturn()

        val sql = "SELECT count(*) FROM request WHERE url = :url AND request_id IS NULL"
        val params = MapSqlParameterSource().addValues(
            mapOf("url" to expectedUrl))
        Thread.sleep(500)
        val count = jdbc.queryForObject(sql, params, Int::class.java)
        Assertions.assertThat(count).isOne()
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    fun redirectProcessByUnexistedEndPointTest() {
        val p1 = "fekMZOmH8vj83IiKYWG7fnbLBUIi1IHxVu_KoTu_l7T8"
        val p2 = "znsaIe2lTiz3zuGS"
        val p3 = UUID.randomUUID().toString()
        val expectedUrl = ERROR_URL

        mockMvc.perform(
            MockMvcRequestBuilders
            .get("/v_error/redirect")
            .param("p1", p1)
            .param("p2", p2)
            .param("p3", p3))
            .andExpect(
                MockMvcResultMatchers.content().string(
                    Matchers.containsString(
                        "Запрос на несуществующий адрес. Перенаправление на $expectedUrl"
                    )
                ))
        val sql = "SELECT count(*) FROM request WHERE url = :url AND request_id IS NULL"
        val params = MapSqlParameterSource().addValues(
            mapOf("url" to expectedUrl))
        Thread.sleep(500)
        val count = jdbc.queryForObject(sql, params, Int::class.java)
        Assertions.assertThat(count).isZero()
    }

    companion object {
        const val VALID_URL = "https://newurl.ru"
        const val ERROR_URL = "https://example.com/error"

        @JvmStatic
        fun getNormalCases() : Stream<Arguments> {
            return Stream.of(
                Arguments.arguments(
                    "fekMZOmH8vj83IiKYWG7fnbLBUIi1IHxVu_KoTu_l7T8",
                    "znsaIe2lTiz3zuGS", UUID.randomUUID().toString(), VALID_URL
                ),
                //iv param p2 is wrong
                Arguments.arguments(
                    "fekMZOmH8vj83IiKYWG7fnbLBUIi1IHxVu_KoTu_l7T8",
                    "AAA", UUID.randomUUID().toString(), ERROR_URL
                ),
                //encrypted param p1 is wrong
                Arguments.arguments(
                    "BBB",
                    "znsaIe2lTiz3zuGS", UUID.randomUUID().toString(), ERROR_URL
                ),
                //encrypted param p1 and iv param p2 are wrong
                Arguments.arguments(
                    "BBB",
                    "AAA", UUID.randomUUID().toString(), ERROR_URL
                )
            )
        }

        @JvmStatic
        fun getAbnormalCases() : Stream<Arguments> {
            return Stream.of(
                //requestId is null
                Arguments.arguments(
                    "fekMZOmH8vj83IiKYWG7fnbLBUIi1IHxVu_KoTu_l7T8",
                    "znsaIe2lTiz3zuGS", null, VALID_URL
                ),
                //requestId is not UUID
                Arguments.arguments(
                    "fekMZOmH8vj83IiKYWG7fnbLBUIi1IHxVu_KoTu_l7T8",
                    "znsaIe2lTiz3zuGS", "aaaa-bbbb-cccc-dddd",  VALID_URL,
                )
            )
        }
    }
}