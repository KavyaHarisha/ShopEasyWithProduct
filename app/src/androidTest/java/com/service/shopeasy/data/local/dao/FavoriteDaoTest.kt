package com.service.shopeasy.data.local.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.service.shopeasy.data.local.ShopEasyDatabase
import com.service.shopeasy.data.local.entity.FavoriteEntity
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    @get:Rule(order = 0)
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ShopEasyDatabase
    private lateinit var favoriteDao: FavoriteDao

    @Before
    fun setUp(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ShopEasyDatabase::class.java)
            .allowMainThreadQueries().build()
        favoriteDao = database.favoriteDao()
    }

    @After
    fun tearDown(){
        database.close()
    }

    @Test
    fun getAllFavorites_initial_returnsEmptyList() = runTest{
        favoriteDao.getAllFavorites().test {
            val list = awaitItem()
            assertThat(list).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertFavorite_successfullyStoreItem() = runTest{
        val favorite = FavoriteEntity(1, "Product", "image.jpg", 10.0, "desc", "category")
        favoriteDao.insertFavorite(favorite)
        favoriteDao.getAllFavorites().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list[0].price).isEqualTo(10.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllFavorites_ordersBySavedAtDescending() = runTest {
        val fav1 = createFavorite(1,"Oldest",1000L)
        val fav2 = createFavorite(2,"Newest",5000L)
        val fav3 = createFavorite(3, "Middle", 3000L)

        favoriteDao.insertFavorite(fav1)
        favoriteDao.insertFavorite(fav2)
        favoriteDao.insertFavorite(fav3)
        favoriteDao.getAllFavorites().test {
            val list = awaitItem()
            assertThat(list).hasSize(3)
            assertThat(list[2].id).isEqualTo(1)
            assertThat(list[1].id).isEqualTo(3)
            assertThat(list[0].id).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertFavorite_withSameId_replaceOldData() = runTest {
        val orgFavorite = createFavorite(1,"Original Name")
        val newFavorite = createFavorite(1,"New Name")
        favoriteDao.insertFavorite(orgFavorite)

        favoriteDao.getAllFavorites().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list[0].title).isEqualTo("Original Name")
            favoriteDao.insertFavorite(newFavorite)
            val list2 = awaitItem()
            assertThat(list2).hasSize(1)
            assertThat(list2[0].title).isEqualTo("New Name")
            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun deleteFavorite_successfullyDeleteItem() = runTest {
        val favorite = FavoriteEntity(1, "Product", "image.jpg", 10.0, "desc", "category")
        val favorite2 = FavoriteEntity(2, "Product", "image.jpg", 10.0, "desc", "category")
        favoriteDao.insertFavorite(favorite)
        favoriteDao.insertFavorite(favorite2)
        favoriteDao.getAllFavorites().test {
            val list = awaitItem()
            assertThat(list).hasSize(2)
            favoriteDao.deleteFavorite(1)
            val list2 = awaitItem()
            assertThat(list2).hasSize(1)
            assertThat(list2[0].id).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()

        }
    }

    @Test
    fun deleteNonExistentFavorite_doesNothing() = runTest {
        val favorite = createFavorite(1,"Product")
        favoriteDao.insertFavorite(favorite)
        favoriteDao.getAllFavorites().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            favoriteDao.deleteFavorite(2)

            assertThat(list).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createFavorite(id: Int, title: String, savedAt: Long = System.currentTimeMillis()) =
        FavoriteEntity(
            id = id,
            title = title,
            image = "image.jpg",
            price = 10.0,
            description = "desc",
            category = "category",
            savedAt = savedAt
        )

}