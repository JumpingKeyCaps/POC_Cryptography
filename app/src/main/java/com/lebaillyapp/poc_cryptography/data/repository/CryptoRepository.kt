package com.lebaillyapp.poc_cryptography.data.repository

import com.lebaillyapp.poc_cryptography.data.service.CryptoService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Repository pour les opérations de cryptage.
 * @param cryptoService Service de cryptage.
 */
class CryptoRepository  @Inject constructor(
    private val cryptoService: CryptoService
) {
    /**
     * Fonction pour crypter un fichier.
     * @param fileData Données du fichier à crypter.
     * @param password Mot de passe pour le cryptage.
     * @param keySize Taille de la clé (en bits).
     * @param iterations Nombre d'itérations pour la dérivation de clé.
     * @param mode Mode de cryptage (1 = AES/CBC, 2 = AES/GCM, 3 = AES/CTR).
     * @return Un flux contenant la progression et les données cryptées.
     */
    fun encryptFile(
        fileData: ByteArray,
        password: String,
        keySize: Int?,
        iterations: Int?,
        mode: Int?,
        extension: String
    ): Flow<Pair<Float, ByteArray>> = cryptoService.encryptFile(fileData, password, keySize, iterations, mode, extension)

    /**
     * Fonction pour décrypter un fichier.
     * @param fileData Données du fichier à décrypter.
     * @param password Mot de passe pour le décryptage.
     * @param keySize Taille de la clé (en bits).
     * @param iterations Nombre d'itérations pour la dérivation de clé.
     * @param mode Mode de cryptage.
     * @return Un flux contenant les données décryptées.
     */
    fun decryptFile(
        fileData: ByteArray,
        password: String,
        keySize: Int?,
        iterations: Int?,
        mode: Int?
    ): Flow<Pair<Float, ByteArray>> = cryptoService.decryptFile(fileData, password, keySize, iterations, mode)
}