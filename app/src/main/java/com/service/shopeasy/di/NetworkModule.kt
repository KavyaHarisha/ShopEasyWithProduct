package com.service.shopeasy.di

import com.service.shopeasy.data.api.ShopEasyApiService
import com.service.shopeasy.data.repository.UserRepository
import com.service.shopeasy.data.repository.impl.NetworkRepositoryImpl
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

private const val BASE_URL = "https://fakestoreapi.com/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor()
            .apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideShopEasyApiService(retrofit: Retrofit): ShopEasyApiService {
        return retrofit.create(ShopEasyApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkRepository(shopEasyApiService: ShopEasyApiService): NetworkRepositoryImpl {
        return NetworkRepositoryImpl(shopEasyApiService)
    }

    @Singleton
    @Provides
    fun provideUserRepository(impl: NetworkRepositoryImpl): UserRepository = impl


}