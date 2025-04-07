package com.lebaillyapp.poc_cryptography.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lebaillyapp.poc_cryptography.data.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour la gestion des opérations de cryptage et de décryptage des fichiers.
 * Ce ViewModel interagit avec le repository CryptoRepository pour effectuer les opérations
 * de cryptage et de décryptage tout en exposant les états de progression et de résultat
 * à l'UI.
 *
 * @param cryptoRepository Repository responsable des opérations de cryptage et de décryptage.
 */
@HiltViewModel
class CryptoViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {

    /**
     * Etat qui représente le résultat de l'opération d'encryptage.
     * Contient un [Result] encapsulant soit une erreur, soit le tableau de bytes cryptés.
     */
    private val _encryptState = MutableStateFlow<Result<ByteArray>?>(null)
    val encryptState: StateFlow<Result<ByteArray>?> = _encryptState

    /**
     * Etat qui représente le résultat de l'opération de décryptage.
     * Contient un [Result] encapsulant soit une erreur, soit le tableau de bytes décryptés.
     */
    private val _decryptState = MutableStateFlow<Result<ByteArray>?>(null)
    val decryptState: StateFlow<Result<ByteArray>?> = _decryptState

    /**
     * Etat qui suit la progression de l'opération d'encryptage.
     * La valeur est un [Float] entre 0 et 1, représentant la progression de l'encryption.
     */
    private val _encryptProgress = MutableStateFlow(0f)
    val encryptProgress: StateFlow<Float> = _encryptProgress

    /**
     * Etat qui suit la progression de l'opération de décryptage.
     * La valeur est un [Float] entre 0 et 1, représentant la progression du décryptage.
     */
    private val _decryptProgress = MutableStateFlow(0f)
    val decryptProgress: StateFlow<Float> = _decryptProgress

    /**
     * Fonction pour crypter un fichier.
     * Cette méthode démarre une coroutine qui appelle le repository pour effectuer l'encryption du fichier
     * avec les paramètres nécessaires (data du fichier, mot de passe, taille de clé, itérations et mode de cryptage).
     * La progression de l'encryption est suivie et exposée en temps réel à l'UI.
     *
     * @param fileData Les données du fichier à crypter sous forme de tableau de bytes.
     * @param password Le mot de passe utilisé pour l'encryption.
     */
    fun encryptFile(fileData: ByteArray, password: String) {
        viewModelScope.launch {
            cryptoRepository.encryptFile(fileData = fileData, password = password, keySize = 256, iterations = 10000, mode = 1)
                .catch { e ->
                    _encryptState.value = Result.failure(e) // Gérer l'erreur et exposer le résultat
                }
                .collect { (progress, _) ->
                    _encryptProgress.value = progress // Mise à jour de la progression de l'encryption
                }
        }
    }

    /**
     * Fonction pour décrypter un fichier.
     * Cette méthode démarre une coroutine qui appelle le repository pour effectuer le décryptage du fichier
     * avec les paramètres nécessaires (data du fichier, mot de passe, taille de clé, itérations et mode de cryptage).
     * La progression du décryptage est suivie et exposée en temps réel à l'UI.
     *
     * @param fileData Les données du fichier à décrypter sous forme de tableau de bytes.
     * @param password Le mot de passe utilisé pour le décryptage.
     */
    fun decryptFile(fileData: ByteArray, password: String) {
        viewModelScope.launch {
            cryptoRepository.decryptFile(fileData = fileData, password = password, keySize = 256, iterations = 10000, mode = 1)
                .catch { e ->
                    _decryptState.value = Result.failure(e) // Gérer l'erreur et exposer le résultat
                }
                .collect { (progress, _) ->
                    _decryptProgress.value = progress // Mise à jour de la progression du décryptage
                }
        }
    }
}