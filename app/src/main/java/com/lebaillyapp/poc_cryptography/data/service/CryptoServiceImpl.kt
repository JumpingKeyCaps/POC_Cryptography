package com.lebaillyapp.poc_cryptography.data.service

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

/**
 * Implementation of the CryptoService.
 */
class CryptoServiceImpl @Inject constructor() : CryptoService {

    // Default size for AES key (256 bits)
    private val defaultKeySize = 256
    // Default number of iterations for PBKDF2
    private val defaultIterations = 10000
    // Default mode (AES/CBC/PKCS5Padding)
    private val defaultMode = "AES/CBC/PKCS5Padding"

    // Supported encryption modes
    private val modes = mapOf(
        1 to "AES/CBC/PKCS5Padding",
        2 to "AES/GCM/NoPadding",
        3 to "AES/CTR/NoPadding"
    )

    // Variables pour stocker le sel et le hash courants
    override var currentSalt: ByteArray? = null
    override var currentHash: ByteArray? = null

    // Variable pour stocker le dernier sel généré (pour affichage initial)
    private var lastGeneratedSalt: ByteArray? = null

    override fun getLastGeneratedSalt(): ByteArray? = lastGeneratedSalt

    override fun hashPasswordForDisplay(password: String, salt: ByteArray?): ByteArray? {
        if (salt == null) return null
        return try {
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keySpec = PBEKeySpec(password.toCharArray(), salt, 1, 32 * 8)
            keyFactory.generateSecret(keySpec).encoded
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Génère un nouveau sel et le stocke comme le sel courant.
     *
     * @return Le sel généré.
     */
    override fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        currentSalt = salt
        return salt
    }



    /**
     * Génère un hash du mot de passe en utilisant le sel courant et le stocke comme le hash courant.
     *
     * @param password Le mot de passe utilisateur.
     * @return Le hash généré sous forme de tableau de bytes ou null si aucun sel courant n'est disponible.
     */
    override fun generateHash(password: String?): ByteArray? {
        val salt = currentSalt ?: return null
        return try {
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keySpec = PBEKeySpec(password?.toCharArray(), salt, defaultIterations, defaultKeySize)
            val key = keyFactory.generateSecret(keySpec).encoded
            currentHash = key
            key
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun encryptFile(
        inputStream: InputStream,
        outputStream: OutputStream,
        password: String,
        keySize: Int?,
        iterations: Int?,
        mode: Int?,
        extension: String
    ): Flow<Float> = flow {
        val mKeySize = keySize ?: defaultKeySize
        val mIterations = iterations ?: defaultIterations
        val mMode = mode ?: 1

        val saltToUse = currentSalt ?: generateSalt().also { currentSalt = it } // Utiliser le sel courant ou en générer un
        val iv = generateIv()
        val secretKey = generateKeyFromPassword(password, saltToUse, mKeySize, mIterations)
        val encryptionMode = modes[mMode] ?: defaultMode

        val cipher = Cipher.getInstance(encryptionMode)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, when (encryptionMode) {
            "AES/GCM/NoPadding" -> GCMParameterSpec(128, iv)
            else -> IvParameterSpec(iv)
        })

        outputStream.write(saltToUse) // Écrire le sel
        outputStream.write(iv)        // Écrire l'IV

        val buffer = ByteArray(8192)
        var bytesRead: Int
        var totalBytesRead = 0L
        val totalBytes = try { inputStream.available().toLong() } catch (e: Exception) { -1L }

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            val encryptedChunk = cipher.update(buffer, 0, bytesRead)
            outputStream.write(encryptedChunk)
            totalBytesRead += bytesRead
            if (totalBytes > 0) {
                emit(totalBytesRead.toFloat() / totalBytes.toFloat())
            } else {
                emit(-1f)
            }
        }

        val finalChunk = cipher.doFinal()
        outputStream.write(finalChunk)
        emit(1.0f)

        outputStream.flush()
        lastGeneratedSalt = saltToUse
        currentSalt = null // Réinitialiser après l'encryption
        currentHash = null // Réinitialiser après l'encryption
    }.flowOn(Dispatchers.IO)

    override fun decryptFile(
        inputStream: InputStream,
        outputStream: OutputStream,
        password: String,
        keySize: Int?,
        iterations: Int?,
        mode: Int?
    ): Flow<Float> = flow {
        val mKeySize = keySize ?: defaultKeySize
        val mIterations = iterations ?: defaultIterations
        val mMode = mode ?: 1

        val salt = ByteArray(16)
        inputStream.read(salt)
        val iv = ByteArray(16)
        inputStream.read(iv)
        val secretKey = generateKeyFromPassword(password, salt, mKeySize, mIterations)
        val encryptionMode = modes[mMode] ?: defaultMode
        val cipher = Cipher.getInstance(encryptionMode)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        val buffer = ByteArray(8192)
        var bytesRead: Int
        var totalBytesRead = 0L
        val totalBytes = try {
            val available = inputStream.available()
            if (available > 0) available.toLong() else -1L
        } catch (e: Exception) {
            -1L
        }

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            val decryptedChunk = cipher.update(buffer, 0, bytesRead)
            if (decryptedChunk != null) {
                outputStream.write(decryptedChunk)
            }
            totalBytesRead += bytesRead
            if (totalBytes > 0) {
                emit(totalBytesRead.toFloat() / totalBytes.toFloat())
            } else {
                emit(-1f)
            }
        }

        val finalChunk = cipher.doFinal()
        outputStream.write(finalChunk)
        emit(1.0f)

        outputStream.flush()
    }.flowOn(Dispatchers.IO)



    private fun generateIv(): ByteArray {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return iv
    }

    private fun generateKeyFromPassword(password: String, salt: ByteArray, keySize: Int, iterations: Int): SecretKey {
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keySpec = PBEKeySpec(password.toCharArray(), salt, iterations, keySize)
        val key = keyFactory.generateSecret(keySpec)
        return SecretKeySpec(key.encoded, "AES")
    }
}