package com.lawrence.binariasendmoney.di

import com.lawrence.data.networking.PenguinNetworkService
import com.lawrence.data.networking.data.Endpoints.BASE_URL
import com.lawrence.binariasendmoney.repo.PenguinRepoImpl
import com.lawrence.binariasendmoney.repo.PenguinRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providePenguinService(): PenguinNetworkService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PenguinNetworkService::class.java)

    @Singleton
    @Provides
    fun providePenguinRepo(networkService: PenguinNetworkService): PenguinRepository = PenguinRepoImpl(networkService)

}