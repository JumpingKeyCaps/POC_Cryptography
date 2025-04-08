package com.lebaillyapp.poc_cryptography.model

data class CryptoConfig(
    val keySize: Int = 256,
    val iterations: Int = 10000,
    val mode: Int = 1
)