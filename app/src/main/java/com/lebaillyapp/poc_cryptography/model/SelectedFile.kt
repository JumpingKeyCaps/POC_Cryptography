package com.lebaillyapp.poc_cryptography.model

import android.net.Uri
import java.io.File

data class SelectedFile(
    val name: String,
    val extension: String,
    val uri: Uri,
    val size: Long, // taille en octets
    val file: File? = null
)