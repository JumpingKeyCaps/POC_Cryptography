package com.lebaillyapp.poc_cryptography.model

import android.net.Uri

data class SAFFile(
    val name: String,
    val size: Long,
    val uri: Uri
)