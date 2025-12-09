package com.service.shopeasy.data.repository

import com.service.shopeasy.data.local.dao.FavoriteDao
import com.service.shopeasy.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(private val favoriteDao: FavoriteDao) {
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()
    suspend fun saveFavorite(favoriteItem: FavoriteEntity) = favoriteDao.insertFavorite(favoriteItem)
    suspend fun deleteFavorite(favoriteId: Int) = favoriteDao.deleteFavorite(favoriteId)
}