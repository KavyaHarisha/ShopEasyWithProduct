package com.service.shopeasy.di

import androidx.room.Room
import android.content.Context
import com.service.shopeasy.data.api.ShopEasyApiService
import com.service.shopeasy.data.local.ShopEasyDatabase
import com.service.shopeasy.data.local.dao.FavoriteDao
import com.service.shopeasy.data.repository.FavoritesRepository
import com.service.shopeasy.data.repository.ProductRepository
import com.service.shopeasy.data.repository.UserRepository
import com.service.shopeasy.data.repository.impl.NetworkRepositoryImpl
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideProductRepository(impl: NetworkRepositoryImpl): ProductRepository = impl


    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): ShopEasyDatabase =
        Room.databaseBuilder(ctx, ShopEasyDatabase::class.java, "shopeasy_db").fallbackToDestructiveMigration(true).build()

    @Singleton
    @Provides
    fun provideFavoriteDao(database: ShopEasyDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Singleton
    @Provides
    fun provideFavoritesRepository(favoriteDao: FavoriteDao): FavoritesRepository {
        return FavoritesRepository(favoriteDao)
    }

}