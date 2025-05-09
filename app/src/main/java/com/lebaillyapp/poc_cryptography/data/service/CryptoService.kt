package com.lebaillyapp.poc_cryptography.data.service

import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

/**
 * Interface for the CryptoService.
 * Defines methods for symmetric encryption and decryption using streams with customizable parameters.
 */
interface CryptoService {
    /**
     * Mutable property to store the current salt being used for encryption.
     */
    var currentSalt: ByteArray?

    /**
     * Mutable property to store the current hash derived from the password and salt.
     */
    var currentHash: ByteArray?

    /**
     * Generates a new random salt.
     *
     * @return A 16-byte salt.
     */
    fun generateSalt(): ByteArray

    /**
     * Generates a hash of the password using the provided salt and the default number of iterations
     * and key size used for encryption.
     *
     * @param password The user's password.
     * @param salt The salt to use for hashing.
     * @return The generated hash as a byte array.
     */
    fun generateHash(password: String?): ByteArray?

    /**
     * Encrypts the data from the input stream and writes the encrypted data to the output stream
     * using AES encryption with a password-derived key.
     *
     * @param inputStream Input stream of the file data to be encrypted.
     * @param outputStream Output stream to write the encrypted data.
     * @param password User-provided password for encryption key derivation.
     * @param keySize Size of the key in bits (default is 256).
     * @param iterations Number of iterations for key derivation (default is 10000).
     * @param mode Mode of encryption (1 for AES/CBC, 2 for AES/GCM, 3 for AES/CTR).
     * @param extension The original file extension (can be used for naming the encrypted file).
     * @return A Flow of Float representing the encryption progress (0.0 to 1.0).
     */
    fun encryptFile(
        inputStream: InputStream,
        outputStream: OutputStream,
        password: String,
        keySize: Int? = 256,
        iterations: Int? = 10000,
        mode: Int? = 1,
        extension: String
    ): Flow<Float>

    /**
     * Decrypts the data from the input stream and writes the decrypted data to the output stream
     * using AES decryption with a password-derived key.
     *
     * @param inputStream The encrypted data input stream.
     * @param outputStream The output stream for the decrypted data.
     * @param password The user-provided password used to derive the decryption key.
     * @param keySize Size of the key in bits (default is 256).
     * @param iterations Number of iterations for key derivation (default is 10000).
     * @param mode Mode of encryption (1 for AES/CBC, 2 for AES/GCM, 3 for AES/CTR).
     * @return A Flow of Float representing the decryption progress (0.0 to 1.0).
     */
    fun decryptFile(
        inputStream: InputStream,
        outputStream: OutputStream,
        password: String,
        keySize: Int? = 256,
        iterations: Int? = 10000,
        mode: Int? = 1
    ): Flow<Float>

    /**
     * Retrieves the last generated salt used for encryption.
     *
     * @return The last generated salt or null if no file has been encrypted yet.
     */
    fun getLastGeneratedSalt(): ByteArray?

    /**
     * Generates a hash of the password using the provided salt with a single iteration of PBKDF2
     * for quick display purposes (POC).
     *
     * @param password The user's password.
     * @param salt The salt to use for hashing.
     * @return The generated hash as a byte array or null in case of an error.
     */
    fun hashPasswordForDisplay(password: String, salt: ByteArray?): ByteArray?
}