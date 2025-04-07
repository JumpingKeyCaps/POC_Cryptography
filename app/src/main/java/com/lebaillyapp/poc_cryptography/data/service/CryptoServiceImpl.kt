package com.lebaillyapp.poc_cryptography.data.service

import android.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Implementation of the CryptoService.
 */
class CryptoServiceImpl : CryptoService {

    // Default size for AES key (256 bits)
    private val defaultKeySize = 256
    // Default number of iterations for PBKDF2
    private val defaultIterations = 10000
    // Default mode (AES/CBC/PKCS5Padding)
    private val defaultMode = "AES/CBC/PKCS5Padding"

    // Supported encryption modes (Cipher Block Chaining, GCM, etc.)
    private val modes = mapOf(
        1 to "AES/CBC/PKCS5Padding",   // AES with CBC mode and PKCS5 padding
        2 to "AES/GCM/NoPadding",      // AES with GCM mode and No padding
        3 to "AES/CTR/NoPadding"       // AES with CTR mode and No padding
    )

    /**
     * Encrypts the provided file data using AES encryption with a password-derived key.
     *
     * @param fileData Data of the file to be encrypted
     * @param password User-provided password for encryption key derivation
     * @param keySize Size of the key in bits (default is 256)
     * @param iterations Number of iterations for key derivation (default is 10000)
     * @param mode Mode of encryption (1 for AES/CBC, 2 for AES/GCM, 3 for AES/CTR)
     * @return A Flow that emits the encryption progress (from 0 to 1) and the encrypted data as a byte array (Base64 encoded)
     */
    override fun encryptFile(
        fileData: ByteArray,
        password: String,
        keySize: Int?,
        iterations: Int?,
        mode: Int?
    ): Flow<Pair<Float, ByteArray>> {
        val mKeySize = keySize ?: defaultKeySize
        val mIterations = iterations ?: defaultIterations
        val mMode = mode ?: 1

        val salt = generateSalt()
        val iv = generateIv()

        // Derive the key from the password and salt
        val secretKey = generateKeyFromPassword(password, salt, mKeySize, mIterations)

        // Get the selected encryption mode
        val encryptionMode = modes[mMode] ?: defaultMode

        val cipher = Cipher.getInstance(encryptionMode)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

        // Total size for progress calculation
        val totalSize = fileData.size.toFloat()
        var bytesProcessed = 0

        val buffer = ByteArray(1024) // Buffer for processing the file in chunks

        return flow {
            // Encrypt file in chunks
            while (bytesProcessed < totalSize) {
                val length = minOf(buffer.size.toFloat(), totalSize - bytesProcessed).toInt()
                fileData.copyInto(buffer, 0, bytesProcessed, bytesProcessed + length)

                // Process chunk of data
                cipher.update(buffer, 0, length)

                // Update bytes processed
                bytesProcessed += length

                // Emit progress (between 0 and 1)
                emit(Pair(bytesProcessed / totalSize, ByteArray(0)))
            }

            // Finalize encryption and get encrypted data
            val encryptedData = cipher.doFinal()

            // Emit final progress (100%) and the encrypted data
            emit(Pair(1.0f, encryptedData))
        }
    }

    /**
     * Decrypts the provided file data using AES decryption with a password-derived key,
     * and reports progress during decryption.
     *
     * @param fileData The encrypted data to be decrypted
     * @param password The user-provided password used to derive the decryption key
     * @param keySize Size of the key in bits (default is 256)
     * @param iterations Number of iterations for key derivation (default is 10000)
     * @param mode Mode of encryption (1 for AES/CBC, 2 for AES/GCM, 3 for AES/CTR)
     * @return A Flow that emits the decryption progress (from 0 to 1) and the decrypted data as a byte array
     */
    override fun decryptFile(
        fileData: ByteArray,
        password: String,
        keySize: Int?,
        iterations: Int?,
        mode: Int?
    ): Flow<Pair<Float, ByteArray>> {

        val mKeySize = keySize ?: defaultKeySize
        val mIterations = iterations ?: defaultIterations
        val mMode = mode ?: 1

        val decodedData = Base64.decode(fileData, Base64.NO_WRAP)

        // Extract salt, IV, and encrypted data from the decoded data
        val salt = decodedData.copyOfRange(0, 16)
        val iv = decodedData.copyOfRange(16, 32)
        val encryptedData = decodedData.copyOfRange(32, decodedData.size)

        // Derive the key from the password and salt
        val secretKey = generateKeyFromPassword(password, salt, mKeySize, mIterations)

        // Get the selected encryption mode
        val encryptionMode = modes[mMode] ?: defaultMode

        val cipher = Cipher.getInstance(encryptionMode)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        // Total size for progress calculation
        val totalSize = encryptedData.size.toFloat()
        var bytesProcessed = 0

        val buffer = ByteArray(1024) // Buffer for processing the file in chunks

        return flow {
            // Decrypt file in chunks
            while (bytesProcessed < totalSize) {
                val length = minOf(buffer.size.toFloat(), totalSize - bytesProcessed).toInt()
                encryptedData.copyInto(buffer, 0, bytesProcessed, bytesProcessed + length)

                // Process chunk of data
                cipher.update(buffer, 0, length)

                // Update bytes processed
                bytesProcessed += length

                // Emit progress (between 0 and 1)
                emit(Pair(bytesProcessed / totalSize, ByteArray(0)))
            }

            // Finalize decryption and get decrypted data
            val decryptedData = cipher.doFinal()

            // Emit final progress (100%) and the decrypted data
            emit(Pair(1.0f, decryptedData))
        }
    }

    /**
     * Generates a random salt to be used for key derivation.
     *
     * @return A 16-byte salt
     */
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Generates a random initialization vector (IV).
     *
     * @return A 16-byte IV
     */
    private fun generateIv(): ByteArray {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return iv
    }

    /**
     * Generates a secret key from the provided password and salt using PBKDF2WithHmacSHA256.
     *
     * @param password The user's password
     * @param salt The salt for key derivation
     * @param keySize The size of the key (in bits)
     * @param iterations The number of iterations for PBKDF2
     * @return A SecretKey derived from the password and salt
     */
    private fun generateKeyFromPassword(password: String, salt: ByteArray, keySize: Int, iterations: Int): SecretKey {
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keySpec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt, iterations, keySize)
        val key = keyFactory.generateSecret(keySpec)
        return SecretKeySpec(key.encoded, "AES")
    }
}