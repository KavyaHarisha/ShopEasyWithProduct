package com.service.shopeasy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val image: String,
    val price: Double,
    val description: String? = null,
    val category: String? = null,
    val savedAt: Long = System.currentTimeMillis()
)
