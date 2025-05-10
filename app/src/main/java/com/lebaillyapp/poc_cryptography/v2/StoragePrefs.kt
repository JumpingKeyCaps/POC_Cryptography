package com.lebaillyapp.poc_cryptography.v2

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ## StoragePrefs
 *
 * Classe utilitaire pour gérer l'URI du dossier sélectionné via le Storage Access Framework (SAF).
 * Elle permet à l'application de retenir le dossier choisi par l'utilisateur pour stocker ses fichiers.
 *
 * ---
 *
 * ### Fonctionnalités
 * - Sauvegarder un URI SAF dans les `SharedPreferences`.
 * - Vérifier si un URI est déjà enregistré.
 * - Récupérer l'URI pour une utilisation ultérieure.
 *
 */
class StoragePrefs(context: Context) {

    private val prefs = context.getSharedPreferences("storage_prefs", Context.MODE_PRIVATE)

    private val _encryptedDirUriFlow = MutableStateFlow(getEncryptedDirUri())
    val encryptedDirUriFlow: StateFlow<Uri?> = _encryptedDirUriFlow

    fun saveEncryptedDirUri(uri: Uri) {
        prefs.edit().putString("encrypted_dir_uri", uri.toString()).apply()
        _encryptedDirUriFlow.value = uri
    }

    fun getEncryptedDirUri(): Uri? {
        return prefs.getString("encrypted_dir_uri", null)?.let(Uri::parse)
    }

    fun hasEncryptedDirUri(): Boolean {
        return prefs.contains("encrypted_dir_uri")
    }
}