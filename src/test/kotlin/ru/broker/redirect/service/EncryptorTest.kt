package ru.broker.redirect.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class EncryptorTest {
    private lateinit var symmetricEncryptor: SymmetricEncryptor
    private val url = "https://base.ru"
    private val rParam = "https://newurl.ru"

    @BeforeEach
    fun init() {
        symmetricEncryptor = SymmetricEncryptor("test")
    }

    @Test
    fun buildUrlWithEncryptedParamTest() {
        val  actual = symmetricEncryptor.buildUrlWithEncryptedParam(url, rParam)

        assertThat(actual).contains(url)
            .contains("p1=")
            .contains("p2=")
    }

    @Test
    fun encryptAndDecryptTest() {
        val params = getRealParams()

        val actual =  symmetricEncryptor.decryptUrl(params.first, params.second)

        assertThat(actual).isEqualTo(rParam)
    }

    @Test
    fun decryptWithNullDataTest() {
        val params = getRealParams()

        val actual =  symmetricEncryptor.decryptUrl(null, params.second)

        assertThat(actual).isNull()
    }

    @Test
    fun decryptWithNullIVTest() {
        val params = getRealParams()

        val actual =  symmetricEncryptor.decryptUrl(params.first, null)

        assertThat(actual).isNull()
    }

    @Test
    fun decryptEmptyStringTest() {
        val encryptedUrl = symmetricEncryptor.buildUrlWithEncryptedParam(url, "")
        val uri = URI(encryptedUrl)
        val query = uri.query
        val pairs = query.split("&")
        val map = pairs.map { it.split("=") }.associate { it[0] to URLDecoder.decode(it[1], StandardCharsets.UTF_8) }
        val actual = symmetricEncryptor.decryptUrl(map["p1"], map["p2"])
        assertThat(actual).isEqualTo("")
    }

    @Test
    fun decryptWithInvalidBase64Test() {
        val params = getRealParams()
        val bad = "!!!notbase64!!!"
        val actual = symmetricEncryptor.decryptUrl(bad, params.second)
        assertThat(actual).isNull()
    }

    @Test
    fun decryptTamperedDataTest() {
        val params = getRealParams()
        // tamper first character
        val tampered = "A" + params.first.substring(1)
        val actual = symmetricEncryptor.decryptUrl(tampered, params.second)
        assertThat(actual).isNull()
    }

    @Test
    fun decryptWithDifferentKeyTest() {
        val params = getRealParams()
        val other = SymmetricEncryptor("otherkey")
        val actual = other.decryptUrl(params.first, params.second)
        assertThat(actual).isNull()
    }

    private fun getRealParams(): Pair<String, String> {
        val  encrypted = symmetricEncryptor.buildUrlWithEncryptedParam(url, rParam)
        val uri = URI(encrypted)
        val query: String? = uri.query
        val pairs = query?.split("&")
        val params = mutableMapOf<String, String>()
        if (pairs != null) {
            for(pair in pairs) {
                val kv = pair.split("=")
                if (kv.size >= 2) {
                    val key = kv[0]
                    val value = URLDecoder.decode(kv.subList(1, kv.size).joinToString("="), StandardCharsets.UTF_8)
                    params[key] = value
                }
            }
        }
        return Pair(params["p1"]!!, params["p2"]!!)
    }
}
