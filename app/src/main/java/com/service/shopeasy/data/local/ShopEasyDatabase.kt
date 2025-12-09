package com.service.shopeasy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.service.shopeasy.data.local.dao.FavoriteDao
import com.service.shopeasy.data.local.entity.FavoriteEntity

@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = false)
abstract class ShopEasyDatabase(): RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}