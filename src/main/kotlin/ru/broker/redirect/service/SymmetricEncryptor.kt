package ru.broker.redirect.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Симметричное шифрование для строки. Используется трансформация AES/GCM/NoPadding
 */
@Service
class SymmetricEncryptor(@Value("\${secrets.encryptionKey:key}") private val encryptionKey: String) {
    private val cipherTransformation = "AES/GCM/NoPadding"
    private val gcmTagLengthBits = 128
    private val ivLengthBytes = 12 // 96 bits recommended for GCM

    /**
     * Формирование URL с зашифрованным параметром.
     * @param baseUrl - часть URL запроса, содержащую scheme и authority, например, https://base.ru
     * @param param - параметр GET-запроса, который шифруется
     * @return URL GET-запроса с двумя параметрами p1 - шифрованный параметр param, p2 - инициализационный вектор
     */
    fun buildUrlWithEncryptedParam(baseUrl: String, param: String): String {
        val (encryptedParam, encryptedIv) = encrypt(param)
        return "$baseUrl?p1=$encryptedParam&p2=$encryptedIv"
    }

    /**
     * Расшифровка строки на основании его шифрованного варианта и инициализационного вектора
     * @param encryptedString - зашифрованная строка
     * @param iv - инициализационный вектор
     * @return - расшифрованная строка. Можем вернуть null, если расшифровать не удалось
     */
    fun decryptUrl(encryptedString: String?, iv: String?): String? =
        decrypt(encryptedString, iv)

    private fun encrypt(data: String): Array<String> {
        val plain = data.toByteArray(StandardCharsets.UTF_8)

        val cipher = Cipher.getInstance(cipherTransformation)
        val iv = initIv()
        val ivSpec = GCMParameterSpec(gcmTagLengthBits, iv)
        val keySpec = getKeySpecification()
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        val encrypted = cipher.doFinal(plain)

        val encoder = Base64.getUrlEncoder().withoutPadding()
        val p1 = encoder.encodeToString(encrypted)
        val p2 = encoder.encodeToString(iv)
        return arrayOf(p1, p2)
    }

    private fun initIv(): ByteArray {
        val iv = ByteArray(ivLengthBytes)
        SecureRandom().nextBytes(iv)
        return iv
    }

    private fun decrypt(encryptedParam: String?, encryptedIV: String?): String? {
        if (encryptedParam == null || encryptedIV == null) return null

        val decoder = Base64.getUrlDecoder()
        val encryptedBytes = try {
            decoder.decode(encryptedParam)
        } catch (_: IllegalArgumentException) {
            return null
        }
        val iv = try {
            decoder.decode(encryptedIV)
        } catch (_: IllegalArgumentException) {
            return null
        }

        val cipher = Cipher.getInstance(cipherTransformation)
        val ivSpec = GCMParameterSpec(gcmTagLengthBits, iv)
        val keySpec = getKeySpecification()
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        val decrypted = try {
            cipher.doFinal(encryptedBytes)
        } catch (_: Exception) {
            return null
        }

        return String(decrypted, StandardCharsets.UTF_8)
    }

    private fun getKeySpecification(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(encryptionKey.toByteArray(StandardCharsets.UTF_8))
        val full = digest.digest()
        val key = full.copyOfRange(0, 16)
        return SecretKeySpec(key, "AES")
    }

}
