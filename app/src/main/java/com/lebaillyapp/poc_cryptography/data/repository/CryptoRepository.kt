package com.lebaillyapp.poc_cryptography.data.repository

import com.lebaillyapp.poc_cryptography.data.service.CryptoService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * Repository pour les opérations de cryptage.
 * @param cryptoService Service de cryptage.
 */
class CryptoRepository @Inject constructor(
    private val cryptoService: CryptoService
) {
    /**
     * Accesseur pour obtenir le sel courant du service.
     */
    val currentSalt: ByteArray? get() = cryptoService.currentSalt

    /**
     * Accesseur pour obtenir le hash courant du service.
     */
    val currentHash: ByteArray? get() = cryptoService.currentHash

    /**
     * Demande au service de générer un nouveau sel.
     *
     * @return Le sel généré.
     */
    fun generateSalt(): ByteArray = cryptoService.generateSalt()

    /**
     * Demande au service de générer un hash du mot de passe en utilisant le sel courant.
     *
     * @param password Le mot de passe utilisateur.
     * @return Le hash généré ou null si aucun sel courant n'est disponible.
     */
    fun generateHash(password: String?): ByteArray? = cryptoService.generateHash(password)

    /**
     * Fonction pour crypter un fichier en utilisant des flux.
     * @param inputStream Flux d'entrée des données du fichier à crypter.
     * @param outputStream Flux de sortie pour écrire les données cryptées.
     * @param password Mot de passe pour le cryptage.
     * @param keySize Taille de la clé (en bits).
     * @param iterations Nombre d'itérations pour la dérivation de clé.
     * @param mode Mode de cryptage (1 = AES/CBC, 2 = AES/GCM, 3 = AES/CTR).
     * @param extension Extension du fichier original (peut être utilisé pour nommer le fichier crypté).
     * @return Un flux contenant la progression du cryptage (0.0 à 1.0).
     */
    fun encryptFile(
        inputStream: InputStream,
        outputStream: OutputStream,
        password: String,
        keySize: Int?,
        iterations: Int?,
        mode: Int?,
        extension: String
    ): Flow<Float> = cryptoService.encryptFile(inputStream, outputStream, password, keySize, iterations, mode, extension)

    /**
     * Fonction pour décrypter un fichier en utilisant des flux.
     * @param inputStream Flux d'entrée des données du fichier à décrypter.
     * @param outputStream Flux de sortie pour écrire les données décryptées.
     * @param password Mot de passe pour le décryptage.
     * @param keySize Taille de la clé (en bits).
     * @param iterations Nombre d'itérations pour la dérivation de clé.
     * @param mode Mode de cryptage.
     * @return Un flux contenant la progression du décryptage (0.0 à 1.0).
     */
    fun decryptFile(
        inputStream: InputStream,
        outputStream: OutputStream,
        password: String,
        keySize: Int?,
        iterations: Int?,
        mode: Int?
    ): Flow<Float> = cryptoService.decryptFile(inputStream, outputStream, password, keySize, iterations, mode)

    /**
     * Récupère le dernier sel généré via le service.
     *
     * @return Le dernier sel généré ou null si aucun fichier n'a été encrypté.
     */
    fun getLastGeneratedSalt(): ByteArray? = cryptoService.getLastGeneratedSalt()

    /**
     * Génère un hash du mot de passe en utilisant le sel via le service (pour affichage dans le POC).
     *
     * @param password Le mot de passe utilisateur.
     * @param salt Le sel à utiliser.
     * @return Le hash généré sous forme de tableau de bytes ou null en cas d'erreur.
     */
    fun hashPasswordForDisplay(password: String, salt: ByteArray?): ByteArray? =
        cryptoService.hashPasswordForDisplay(password, salt)
}