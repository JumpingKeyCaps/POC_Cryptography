package com.lebaillyapp.poc_cryptography.data.service

import kotlinx.coroutines.flow.Flow

/**
 * Interface for the CryptoService.
 * Defines methods for symmetric encryption and decryption with customizable parameters.
 */
interface CryptoService {
    /**
     * Encrypts the provided file data using AES encryption with a password-derived key.
     *
     * @param fileData Data of the file to be encrypted
     * @param password User-provided password for encryption key derivation
     * @param keySize Size of the key in bits (default is 256)
     * @param iterations Number of iterations for key derivation (default is 10000)
     * @param mode Mode of encryption (1 for AES/CBC, 2 for AES/GCM, 3 for AES/CTR)
     * @return A Flow of Pair<Float, ByteArray> where the first value is the progress (0.0 to 1.0) and the second value is the encrypted data
     */
    fun encryptFile(
        fileData: ByteArray,
        password: String,
        keySize: Int? = 256,
        iterations: Int? = 10000,
        mode: Int? = 1
    ): Flow<Pair<Float, ByteArray>>

    /**
     * Decrypts the provided file data using AES decryption with a password-derived key.
     *
     * @param fileData The encrypted data to be decrypted
     * @param password The user-provided password used to derive the decryption key
     * @param keySize Size of the key in bits (default is 256)
     * @param iterations Number of iterations for key derivation (default is 10000)
     * @param mode Mode of encryption (1 for AES/CBC, 2 for AES/GCM, 3 for AES/CTR)
     * @return The decrypted data as a byte array
     */
    fun decryptFile(
        fileData: ByteArray,
        password: String,
        keySize: Int? = 256,
        iterations: Int? = 10000,
        mode: Int? = 1
    ): Flow<Pair<Float, ByteArray>>
}