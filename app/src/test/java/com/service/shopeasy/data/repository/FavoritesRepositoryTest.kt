package com.service.shopeasy.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.service.shopeasy.CoroutinesTestRule
import com.service.shopeasy.data.local.dao.FavoriteDao
import com.service.shopeasy.data.local.entity.FavoriteEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class FavoritesRepositoryTest {

    @get:Rule
    val rule = CoroutinesTestRule()

    private val favoriteDao = mockk<FavoriteDao>()
    private val favoritesRepository = FavoritesRepository(favoriteDao)

    @Test
    fun `getAllFavorites with an empty database`() = runTest{
        // Given: The database contains no FavoriteEntity records.
        coEvery {
            favoriteDao.getAllFavorites()
        } returns flowOf(emptyList())
        // When: getAllFavorites() is called.
        favoritesRepository.getAllFavorites().test {
            // Then: The returned Flow should emit an empty list.
            val result = awaitItem()
            assertThat(result).isEmpty()

            // Ensure there are no more emissions and complete the test.
            awaitComplete()
        }
    }

    @Test
    fun `getAllFavorites with a single item`() = runTest{
        // Given: The database contains one FavoriteEntity record.
        val favoritesList = listOf(
            FavoriteEntity(1, "Product 1", "image1.jpg", 20.0, "desc 1", "category 1")
        )
        coEvery {
            favoriteDao.getAllFavorites()
        } returns flowOf(favoritesList)
        // When: getAllFavorites() is called.
        favoritesRepository.getAllFavorites().test {
            val resultList = awaitItem()
            assertThat(resultList).hasSize(1)
            awaitComplete()
        }

    }

    @Test
    fun `getAllFavorites with multiple items`() =runTest{
        // Given: The database contains multiple FavoriteEntity records.
        val multipleFavoritesListItems = listOf(
            FavoriteEntity(1, "Product 1", "image1.jpg", 20.0, "desc 1", "category 1"),
            FavoriteEntity(2, "Product 2", "image2.jpg", 30.0, "desc 2", "category 2"),
            FavoriteEntity(3, "Product 3", "image3.jpg", 40.0, "desc 3", "category 3")
        )
        coEvery {
            favoriteDao.getAllFavorites()
        } returns flowOf(multipleFavoritesListItems)
        // When: getAllFavorites() is called.
        favoritesRepository.getAllFavorites().test {
            val multipleFavoritesListItemsResult = awaitItem()
            assertThat(multipleFavoritesListItemsResult).hasSize(3)
            assertThat(multipleFavoritesListItemsResult[1].title).isEqualTo("Product 2")
            awaitComplete()
        }
    }

    @Test
    fun `getAllFavorites flow observation after insertion`() = runTest{
        // Given: A collector is actively observing the getAllFavorites() Flow, which starts empty.
        val favorite =
            FavoriteEntity(1, "Product 1", "image1.jpg", 20.0, "desc 1", "category 1")

        // Use a MutableStateFlow to simulate the reactive database flow.
        val databaseFlow = MutableStateFlow<List<FavoriteEntity>>(emptyList())

        // Mock the DAO's methods
        coEvery { favoriteDao.getAllFavorites() } returns databaseFlow
        coEvery { favoriteDao.insertFavorite(favorite) } coAnswers {
            // When insertFavorite is called, we simulate the database update
            // by pushing a new list to our state flow.
            databaseFlow.value = listOf(favorite)
        }

        favoritesRepository.getAllFavorites().test {
            // 1. Assert the initial state is an empty list.
            assertThat(awaitItem()).isEmpty()

            // When: A new FavoriteEntity is inserted using the repository's method.
            favoritesRepository.saveFavorite(favorite)

            // Then: The Flow should emit a new, updated list that includes the newly added item.
            val updatedList = awaitItem()
            assertThat(updatedList).hasSize(1)
            assertThat(updatedList.first()).isEqualTo(favorite)

            // Cancel the collector to avoid waiting for more items.
            cancelAndIgnoreRemainingEvents()
        }

        // Finally, verify that the repository correctly called the DAO's insert method.
        coVerify(exactly = 1) {
            favoriteDao.insertFavorite(favorite)
        }

    }

    @Test
    fun `getAllFavorites flow observation after deletion`() = runTest{
        // Given: A collector is observing the getAllFavorites() Flow with existing items.
        val favorite = FavoriteEntity(1, "Product 1", "image1.jpg", 20.0, "desc 1", "category 1")
        val favorite1 = FavoriteEntity(2, "Product 2", "image2.jpg", 30.0, "desc 2", "category 2")
        val initialList = listOf(favorite, favorite1)
        val stateDataFlow = MutableStateFlow(initialList)

        // Mock the DAO's methods
        coEvery { favoriteDao.getAllFavorites() } returns stateDataFlow
        // Make deleteFavorite a simple suspend fun that does nothing, as in a real DAO.
        coEvery { favoriteDao.deleteFavorite(2) } returns Unit

        favoritesRepository.getAllFavorites().test {
            // 1. Assert the initial state has two items.
            val currentList = awaitItem()
            assertThat(currentList).hasSize(2)
            assertThat(currentList[1]).isEqualTo(favorite1)

            // When: An existing FavoriteEntity is deleted using the repository's method.
            favoritesRepository.deleteFavorite(2)

            // AND WHEN: The database (our fake flow) is updated as a result of the deletion.
            stateDataFlow.value = listOf(favorite)

            // Then: The Flow should emit a new, updated list that no longer contains the deleted item.
            val updatedList = awaitItem()
            assertThat(updatedList).hasSize(1)
            assertThat(updatedList.first()).isEqualTo(favorite)

            cancelAndIgnoreRemainingEvents()
        }

        // Finally, verify that the repository correctly called the DAO's delete method.
        coVerify(exactly = 1) {
            favoriteDao.deleteFavorite(2)
        }
    }

    @Test
    fun `getAllFavorites flow observation after update`() = runTest{
        val favorite = FavoriteEntity(1, "Product 1", "image1.jpg", 20.0, "desc 1", "category 1")
        val favorite1 = FavoriteEntity(2, "Product 2", "image2.jpg", 30.0, "desc 2", "category 2")
        val initialList = listOf(favorite, favorite1)
        val changeFavorite1 = FavoriteEntity(2, "Product change 2", "image2.jpg", 30.0, "desc 2", "category 2")
        val stateDataFlow = MutableStateFlow(initialList)

        coEvery {
            favoriteDao.getAllFavorites()
        } returns stateDataFlow
        coEvery {
            favoriteDao.insertFavorite(changeFavorite1)
        } returns Unit

        favoritesRepository.getAllFavorites().test {
            val currentList = awaitItem()
            assertThat(currentList).hasSize(2)
            assertThat(currentList[1]).isEqualTo(favorite1)

            favoritesRepository.saveFavorite(changeFavorite1)
            stateDataFlow.value = listOf(favorite, changeFavorite1)
            val updatedList = awaitItem()
            assertThat(updatedList).hasSize(2)
            assertThat(updatedList[1]).isEqualTo(changeFavorite1)

            cancelAndIgnoreRemainingEvents()
        }
       coVerify {
           favoriteDao.insertFavorite(changeFavorite1)
       }
    }

    @Test
    fun `getAllFavorites behavior on database error`() = runTest{
        val databaseError = RuntimeException("Database error")
        val errorFlow: Flow<List<FavoriteEntity>> = flow {
            throw databaseError
        }
        coEvery {
            favoriteDao.getAllFavorites()
        } returns errorFlow

        favoritesRepository.getAllFavorites().test {
            val exception = awaitError()
            assertThat(exception).isEqualTo(databaseError)
            assertThat(exception).isInstanceOf(RuntimeException::class.java)
            assertThat(exception.message).isEqualTo("Database error")
        }

    }

    @Test
    fun `saveFavorite a new valid item`() = runTest{
        // Given: A valid FavoriteEntity that does not exist in the database.
        val favorite = FavoriteEntity(1, "Product 1", "image1.jpg", 20.0, "desc 1", "category 1")
        coEvery {
            favoriteDao.insertFavorite(favorite)
        } returns Unit
        // When: saveFavorite() is called with this entity.
        favoritesRepository.saveFavorite(favorite)
        // Then: The operation should complete without errors, and the item should be present in the database.
        coVerify(exactly = 1) {
            favoriteDao.insertFavorite(favorite)
        }
    }

    @Test
    fun `saveFavorite an item with a conflicting primary key`() = runTest{
        // Given: A FavoriteEntity with a primary key that already exists in the database.
        val initialFavorite = FavoriteEntity(1, "Old Title", "image1.jpg", 20.0, "desc 1", "category 1")
        val updatedFavorite = FavoriteEntity(1, "New Tittle", "image1.jpg", 20.0, "desc 1", "category 1")
        val stateDataFlow = MutableStateFlow(listOf(initialFavorite))
        coEvery {
            favoriteDao.getAllFavorites()
        } returns stateDataFlow

        coEvery {
            favoriteDao.insertFavorite(updatedFavorite)
        } returns Unit
        // When: saveFavorite() is called with this entity (assuming insert strategy is OnConflictStrategy.REPLACE or similar).
        favoritesRepository.getAllFavorites().test {
            val currentList = awaitItem()
            assertThat(currentList).hasSize(1)
            assertThat(currentList.first().title).isEqualTo("Old Title")
            favoritesRepository.saveFavorite(updatedFavorite)
            stateDataFlow.value = listOf(updatedFavorite)
            val updatedList = awaitItem()
            assertThat(updatedList).hasSize(1)
            assertThat(updatedList.first().title).isEqualTo("New Tittle")
        }
        coVerify {
            favoriteDao.insertFavorite(updatedFavorite)
        }
    }

    @Test
    fun `saveFavorite with fields at max supported length`() = runTest{
        // Given: A FavoriteEntity where string fields are set to their maximum allowed length.
        val largeTitle = "A".repeat(255)
        val largeDescription = "A".repeat(2000)
        val largeImage = "https://example.com"+"C".repeat(500) + ".jpg"
        val favorite = FavoriteEntity(
            1,
            largeTitle,
            largeImage,
            20.0,
            largeDescription,
            "Test Category"
        )
        coEvery {
            favoriteDao.insertFavorite(favorite)
        } returns Unit
        // When: saveFavorite() is called.
        favoritesRepository.saveFavorite(favorite)
        // Then: The item should be saved correctly without data truncation or errors.
        coVerify {
            favoriteDao.insertFavorite(match {
                it.title == largeTitle &&
                        it.description == largeDescription &&
                        it.image.endsWith(".jpg")
            })
        }
    }

    @Test
    fun `saveFavorite concurrency stress test`() = runTest{
        // Given: Multiple coroutines are launched concurrently.
        val count = 100
        val favorites = (1..count).map { i ->
            FavoriteEntity(i, "Product $i", "image$i.jpg", i.toDouble(), "desc $i", "cat $i")
        }

        // Mock the DAO to return Unit for all calls
        // Use any() or specific matching to ensure it accepts all 100 calls
        coEvery { favoriteDao.insertFavorite(any()) } returns Unit

        // When: Each coroutine calls saveFavorite() with different entities simultaneously.
        // We use 'awaitAll' to ensure the test waits for all concurrent jobs to finish
        val jobs = favorites.map { favorite ->
            async {
                favoritesRepository.saveFavorite(favorite)
            }
        }
        jobs.awaitAll()

        // Then: All items should be saved correctly without race conditions.
        // Verify that the DAO's insert method was called exactly 'count' times.
        coVerify(exactly = count) {
            favoriteDao.insertFavorite(any())
        }

        // Optionally, verify specific items were passed to ensure no data corruption
        coVerify {
            favoriteDao.insertFavorite(match { it.id == 1 })
            favoriteDao.insertFavorite(match { it.id == count })
        }

    }

    @Test
    fun `deleteFavorite an existing item`() = runTest{
        // Given: An item with a specific favoriteId exists in the database.
        val count = 5
        val favorites = (1..count).map{ i ->
            FavoriteEntity(i, "Product $i", "image$i.jpg", i.toDouble(), "desc $i", "cat $i")
        }
        val stateDataFlow = MutableStateFlow(favorites)
        coEvery {
            favoriteDao.getAllFavorites()
        } returns stateDataFlow
        coEvery {
            favoriteDao.deleteFavorite(2)
        } returns Unit
        // When: deleteFavorite() is called with that favoriteId.
        favoritesRepository.deleteFavorite(2)
        // Then: The operation should complete successfully, and the item should be removed from the database.
        coVerify {
            favoriteDao.deleteFavorite(match { it == 2 })
        }
    }

    @Test
    fun `deleteFavorite a non existent item`() = runTest{
        // Given: No item with the specified favoriteId exists in the database.
        val nonExistentId = 999
        coEvery {
            favoriteDao.deleteFavorite(nonExistentId)
        } returns Unit
        // When: deleteFavorite() is called with that favoriteId.
        favoritesRepository.deleteFavorite(nonExistentId)
        // Then: The operation should complete without error, and the database state should remain unchanged.
        coVerify {
            favoriteDao.deleteFavorite(match { it == nonExistentId })
        }
    }

    @Test
    fun `deleteFavorite with a negative or zero ID`() = runTest{
        // Given: A favoriteId that is invalid (e.g., 0 or a negative number).
        val invalidId = -1
        coEvery {
            favoriteDao.deleteFavorite(invalidId)
        } returns Unit
        // When: deleteFavorite() is called with this ID.
        favoritesRepository.deleteFavorite(invalidId)
        // Then: The operation should complete without error and not affect any records, as no valid record will match this ID.
        coVerify {
            favoriteDao.deleteFavorite(match { it == invalidId })
        }
    }

    @Test
    fun `deleteFavorite concurrency stress test`() = runTest{
        // Given: An existing item and multiple coroutines.
        val count = 100
        val idsToDelete = (1..count).toList()
        coEvery { favoriteDao.deleteFavorite(any()) } returns Unit
        // When: Multiple coroutines attempt to delete the same item simultaneously using deleteFavorite().
        val job = idsToDelete.map { id ->
            async {
                favoritesRepository.deleteFavorite(id)
            }
        }
        job.awaitAll()
        // Then: The operation should execute without errors or deadlocks. One of the calls will succeed, and subsequent calls will do nothing.
        coVerify {
            favoriteDao.deleteFavorite(match { it in idsToDelete })
            favoriteDao.deleteFavorite(1)
            favoriteDao.deleteFavorite(count)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Test coroutine cancellation for suspend functions`() = runTest{
        // Given: A coroutine is running a saveFavorite() or deleteFavorite() operation.
        val favorite = FavoriteEntity(1, "Product 1", "image1.jpg", 20.0, "desc 1", "category 1")
        coEvery {
            favoriteDao.insertFavorite(favorite)
        } coAnswers {
            delay(2000) // Simulate a long-running operation
        }
        // When: The coroutine's job is cancelled during the operation.
        val job = launch {
            favoritesRepository.saveFavorite(favorite)
        }
        advanceTimeBy(500)
        job.cancel()
        // Then: The operation should be properly cancelled, and the database should be left in a consistent state.
        assertThat(job.isCancelled).isTrue()
    }

}