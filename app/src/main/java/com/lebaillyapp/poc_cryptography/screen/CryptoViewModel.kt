package com.lebaillyapp.poc_cryptography.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lebaillyapp.poc_cryptography.data.repository.CryptoRepository
import com.lebaillyapp.poc_cryptography.model.CryptoConfig
import com.lebaillyapp.poc_cryptography.model.SelectedFile
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


    private val _uiState = MutableStateFlow(CryptoUiState.Idle)
    val uiState: StateFlow<CryptoUiState> = _uiState

    private val _defaultConfig = MutableStateFlow(CryptoConfig())
    val defaultConfig: StateFlow<CryptoConfig> = _defaultConfig



    private val _selectedFile = MutableStateFlow<SelectedFile?>(null)
    val selectedFile: StateFlow<SelectedFile?> = _selectedFile


    fun onFileSelected(uri: Uri, context: Context) {
        // Récupère les informations du fichier
        val name = uri.getFileName(context) ?: return
        val extension = name.substringAfterLast('.', "")
        val size = uri.getFileSize(context)

        // Crée l'objet SelectedFile
        val file = SelectedFile(
            name = name,
            extension = extension,
            uri = uri,
            size = size
        )
        _selectedFile.value = file
        _uiState.value =  CryptoUiState.FileSelectedToEncrypt
    }

    private fun Uri.getFileName(context: Context): String? {
        return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }

    private fun Uri.getFileSize(context: Context): Long {
        return context.contentResolver.openFileDescriptor(this, "r")?.use { fileDescriptor ->
            fileDescriptor.statSize
        } ?: 0L
    }



    /**
     * Permet de modifier la configuration du cryptage/décryptage.
     * @param newConfig La nouvelle configuration à appliquer.
     */
    fun updateConfig(newConfig: CryptoConfig) {
        _defaultConfig.value = newConfig
    }

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
            val config = _defaultConfig.value
            cryptoRepository.encryptFile(fileData, password, config.keySize, config.iterations, config.mode)
                .catch { e -> _encryptState.value = Result.failure(e) }
                .collect { (progress, _) -> _encryptProgress.value = progress }
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
            val config = _defaultConfig.value
            cryptoRepository.decryptFile(fileData, password, config.keySize, config.iterations, config.mode)
                .catch { e -> _decryptState.value = Result.failure(e) }
                .collect { (progress, _) -> _decryptProgress.value = progress }
        }
    }
}

