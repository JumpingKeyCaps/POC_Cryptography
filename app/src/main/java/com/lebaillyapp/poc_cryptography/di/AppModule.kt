package com.lebaillyapp.poc_cryptography.di

import com.lebaillyapp.poc_cryptography.data.repository.CryptoRepository
import com.lebaillyapp.poc_cryptography.data.service.CryptoService
import com.lebaillyapp.poc_cryptography.data.service.CryptoServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)  // Utilise SingletonComponent si c'est une dépendance globale
object AppModule {

    @Provides
    @Singleton
    fun provideCryptoService(): CryptoService {
        return CryptoServiceImpl()  // Implémentation du service de cryptage
    }

    @Provides
    @Singleton
    fun provideCryptoRepository(cryptoService: CryptoService): CryptoRepository {
        return CryptoRepository(cryptoService)
    }
}