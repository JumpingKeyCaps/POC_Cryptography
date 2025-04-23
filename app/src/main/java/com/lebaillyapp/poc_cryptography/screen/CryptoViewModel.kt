package com.lebaillyapp.poc_cryptography.screen

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lebaillyapp.poc_cryptography.data.repository.CryptoRepository
import com.lebaillyapp.poc_cryptography.model.CryptoConfig
import com.lebaillyapp.poc_cryptography.model.SelectedFile
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
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

    private val _isEncryptionCancelled = MutableStateFlow(false)
    val isEncryptionCancelled: StateFlow<Boolean> = _isEncryptionCancelled

    private val _selectedFile = MutableStateFlow<SelectedFile?>(null)
    val selectedFile: StateFlow<SelectedFile?> = _selectedFile


    fun onFileSelected(uri: Uri, context: Context) {
        val name = uri.getFileName(context) ?: return
        val extension = name.substringAfterLast('.', "")
        val size = uri.getFileSize(context)

        // Ajout de récupération du fichier local (copie temporaire si nécessaire)
        val file = uri.getFile(context)

        val selected = SelectedFile(
            name = name,
            extension = extension,
            uri = uri,
            size = size,
            file = file
        )
        _selectedFile.value = selected
        _uiState.value = CryptoUiState.FileSelectedToEncrypt
    }

    fun resetFile() {
        _selectedFile.value = null
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

    fun Uri.getFileExtension(context: Context): String? {
        return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex).substringAfterLast('.', "")
        }
    }

    fun Uri.getFile(context: Context): File? {
        return when (this.scheme) {
            "content" -> {
                // Si c'est une URI de type 'content' (ex. téléchargements, documents)
                getContentFile(context)
            }
            "file" -> {
                // Si c'est une URI de type 'file' (ex. fichiers locaux)
                File(this.path)
            }
            else -> null
        }
    }

    // Récupère le fichier d'une URI de type 'content'
    private fun Uri.getContentFile(context: Context): File? {
        return try {
            // Accède au contenu via le ContentResolver
            context.contentResolver.openInputStream(this)?.use { inputStream ->
                val tempFile = File.createTempFile("tmp_", null, context.cacheDir)
                tempFile.deleteOnExit()
                inputStream.copyTo(tempFile.outputStream())
                tempFile
            }
        } catch (e: Exception) {
            null
        }
    }


    // Fonction pour annuler l'encryption
    fun cancelEncryption() {
        _isEncryptionCancelled.value = true
    }

    /**
     * Permet de modifier la configuration du cryptage/décryptage.
     * @param newConfig La nouvelle configuration à appliquer.
     */
    fun updateConfig(newConfig: CryptoConfig) {
        _defaultConfig.value = newConfig
    }

    fun encryptFile(fileData: ByteArray, password: String, extension: String, context: Context) {
        _isEncryptionCancelled.value = false

        viewModelScope.launch {
            try {
                val config = _defaultConfig.value
                Log.d("EncryptFile", "Starting file encryption with config: $config")

                cryptoRepository.encryptFile(
                    fileData = fileData,
                    password = password,
                    keySize = config.keySize,
                    iterations = config.iterations,
                    mode = config.mode,
                    extension = extension
                ).catch { e ->
                    Log.e("EncryptFile", "Encryption failed: ${e.message}", e)
                    _encryptState.value = Result.failure(e)
                }.collect { (progress, encryptedData) ->

                    if (_isEncryptionCancelled.value) {
                        Log.d("EncryptFile", "Encryption cancelled.")
                        return@collect
                    }

                    Log.d("EncryptFile", "Encryption progress: $progress")
                    _encryptProgress.value = progress

                    if (progress >= 1f && encryptedData.isNotEmpty()) {
                        Log.d("EncryptFile", "Encryption completed successfully, saving encrypted data...")

                        val selected = _selectedFile.value
                        val file = selected?.file
                        val uri = selected?.uri

                        // Sauvegarde dans le répertoire des téléchargements
                        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                        // Vérifie si le fichier sélectionné existe
                        if (file != null) {
                            // Utilise le nom d'origine du fichier sans extension
                            val originalFileName = file.nameWithoutExtension
                            val encryptedFile = File(downloadDir, "$originalFileName.$extension")

                            Log.d("EncryptFile", "Saving encrypted file to: ${encryptedFile.absolutePath}")
                            encryptedFile.writeBytes(encryptedData)
                            _encryptState.value = Result.success(encryptedData)

                            // Log de suppression du fichier original si nécessaire
                            if (file.absolutePath != encryptedFile.absolutePath) {
                                val isDeleted = file.delete()
                                Log.d("EncryptFile", "Original file deleted: ${file.absolutePath}, success: $isDeleted")
                            }
                        } else if (uri != null) {
                            // Sauvegarde dans un fichier via le URI
                            try {
                                Log.d("EncryptFile", "Saving encrypted data to URI: $uri")
                                context.contentResolver.openOutputStream(uri)?.use { output ->
                                    output.write(encryptedData)
                                }
                                _encryptState.value = Result.success(encryptedData)
                                Log.d("EncryptFile", "Encrypted data saved to URI: $uri")
                            } catch (e: Exception) {
                                Log.e("EncryptFile", "Error saving encrypted data to URI: $uri", e)
                                _encryptState.value = Result.failure(e)
                            }
                        } else {
                            Log.e("EncryptFile", "No file or URI to save encrypted data.")
                            _encryptState.value = Result.failure(Exception("Aucun fichier ni URI pour écrire"))
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("EncryptFile", "Encryption failed: ${e.message}", e)
                _encryptState.value = Result.failure(e)
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
            val config = _defaultConfig.value
            cryptoRepository.decryptFile(fileData, password, config.keySize, config.iterations, config.mode)
                .catch { e -> _decryptState.value = Result.failure(e) }
                .collect { (progress, _) -> _decryptProgress.value = progress }
        }
    }
}

