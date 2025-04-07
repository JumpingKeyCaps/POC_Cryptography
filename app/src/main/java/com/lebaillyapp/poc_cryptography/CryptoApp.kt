package com.lebaillyapp.poc_cryptography

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the CryptoApp.
 */
@HiltAndroidApp
class CryptoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialisation si nécessaire
    }
}