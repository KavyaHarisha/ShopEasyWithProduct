package com.service.shopeasy.data.repository

import com.google.common.truth.Truth.assertThat
import com.service.shopeasy.CoroutinesTestRule
import com.service.shopeasy.data.api.ShopEasyApiService
import com.service.shopeasy.data.dto.NameDto
import com.service.shopeasy.data.dto.UserDto
import com.service.shopeasy.data.repository.impl.NetworkRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.cancellation.CancellationException

class UserRepositoryTest {

    @get:Rule
    val rule = CoroutinesTestRule()

    private lateinit var userRepository: UserRepository

    private val apiService = mockk<ShopEasyApiService>()


    @Before
    fun setUpRepository() {
        userRepository = NetworkRepositoryImpl(apiService)
    }


    @Test
    fun `getUsers successful response with a non empty list`() = runTest {
        // Given the repository is mocked to return a successful result with a list containing multiple User objects,
        val usersList = listOf(
            UserDto(1, "user 1", "user1@xyz.com", NameDto("user", "1")),
            UserDto(2, "user 2", "user2@xyz.com", NameDto("user", "2")),
            UserDto(3, "user 3", "user3@xyz.com", NameDto("user", "3"))
        )

        coEvery {
            apiService.getUsers()
        } returns usersList
        // When getUsers is called,
        val resultUserList = userRepository.getUsers()

        // Then verify the method returns the expected list of users.
        assertThat(resultUserList.size).isEqualTo(3)
        assertThat(resultUserList[0].id).isEqualTo(1)
        assertThat(resultUserList[1].email).isEqualTo("user2@xyz.com")
        assertThat(resultUserList[2].name).isEqualTo("user 3")
        coVerify(exactly = 1) {
            apiService.getUsers()
        }
    }

    @Test
    fun `getUsers successful response with an empty list`() = runTest {
        // Given the repository is mocked to return a successful result with an empty list,
        coEvery {
            apiService.getUsers()
        } returns emptyList()
        // When getUsers is called,
        val resultUserList = userRepository.getUsers()
        // Then verify the method returns an empty list.
        assertThat(resultUserList.size).isEqualTo(0)
        coVerify(exactly = 1) {
            apiService.getUsers()
        }
    }

    @Test
    fun `getUsers throws a generic Exception`() = runTest {
        // Given the repository is mocked to throw a generic Exception (e.g., IOException for a network error),
        coEvery {
            apiService.getUsers()
        } throws RuntimeException("Something went wrong")
        // When getUsers is called,
        val resultUserList = assertThrows<RuntimeException> {
            userRepository.getUsers()
        }
        // Then verify that the specific exception is thrown.
        assertThat(resultUserList.message).isEqualTo("Something went wrong")

        coVerify(exactly = 1) {
            apiService.getUsers()
        }
    }

    @Test
    fun `getUsers throws a specific HTTP exception  e g   404 Not Found `() = runTest {
        // Given the repository is mocked to throw a specific HttpException (e.g., 404 Not Found),
        val httpErrorResponse = Response.error<UserDto>(
            404,
            "Not found user list".toResponseBody("text/plain".toMediaTypeOrNull())
        )
        coEvery {
            apiService.getUsers()
        } throws HttpException(httpErrorResponse)
        // When getUsers is called,
        val resultUserList = assertThrows<HttpException> {
            userRepository.getUsers()
        }
        // Then verify that the HttpException is caught and handled appropriately.
        assertThat(resultUserList.code()).isEqualTo(404)
        coVerify(exactly = 1) {
            apiService.getUsers()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getUsers handles coroutine cancellation`() = runTest {
        // Given the coroutine job running getUsers is cancelled before completion,
        coEvery {
            apiService.getUsers()
        } coAnswers {
            delay(1000)
            emptyList()
        }
        // When getUsers is executing,
        val jod = async {
            userRepository.getUsers()
        }
        advanceTimeBy(500)
        jod.cancel()
        assertThrows<CancellationException> {
            jod.await()
        }
        // Then verify that the coroutine is properly cancelled and does not complete or throw an unhandled exception. [9]
        coVerify(atLeast = 1) {
            apiService.getUsers()
        }
    }

    @Test
    fun `getUsers response with a list containing a user with null properties`() = runTest {
        // Given the repository returns a list where a User object has nullable fields set to null,
        val usersListWithNulls = listOf(
            UserDto(1, "user 1", "user1@xyz.com", NameDto("user", "1")),
            UserDto(2, null, "user2@xyz.com", NameDto("user", "2")),
            UserDto(3, "user 3", null, null)
        )
        coEvery {
            apiService.getUsers()
        } returns usersListWithNulls
        // When getUsers is called,
        val resultUserList = userRepository.getUsers()
        // Then verify the method correctly deserializes and returns the list with the user object containing nulls.
        assertThat(resultUserList.size).isEqualTo(3)
        assertThat(resultUserList[0].name).isEqualTo("user 1")
        assertThat(resultUserList[2].email).isEmpty()
        assertThat(resultUserList[2].name).isEmpty()
        coVerify(exactly = 1) {
            apiService.getUsers()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getUsers with a significant delay in response`() = runTest {
        // Given the repository is mocked to have a significant delay before returning a result,
        val usersList = listOf(
            UserDto(1, "user 1", "user1@xyz.com", NameDto("user", "1"))
        )
        val longDelay = 10_000L
        coEvery {
            apiService.getUsers()
        } coAnswers {
            delay(longDelay)
            usersList
        }
        // When getUsers is called,
        val deferredResult = async {
            userRepository.getUsers()
        }
        val resultUserList = deferredResult.await()
        advanceTimeBy(longDelay)
        // Then use runTest with virtual time control to verify that the test completes quickly without waiting for the actual delay. [5, 7]
        assertThat(resultUserList).isNotEmpty()
        assertThat(resultUserList.size).isEqualTo(1)
        assertThat(resultUserList.first().name).isEqualTo("user 1")

        coVerify(exactly = 1) {
            apiService.getUsers()
        }

    }

    @Test
    fun `getUsers called multiple times concurrently`() = runTest {
        // Given multiple coroutines call getUsers simultaneously,
        val usersList = listOf(
            UserDto(1, "user 1", "user1@xyz.com", NameDto("user", "1")),
            UserDto(2, "user 2", "user2@xyz.com", NameDto("user", "2"))
        )
        coEvery {
            apiService.getUsers()
        } returns usersList

        // When getUsers is invoked concurrently,
        val deferred1 = async { userRepository.getUsers() }
        val deferred2 = async { userRepository.getUsers() }
        val result1 = deferred1.await()
        val result2 = deferred2.await()

        // Then verify that each coroutine receives the correct, independent result and that there are no race conditions (using async/await). [17]
        assertThat(result1).hasSize(2)
        assertThat(result2).hasSize(2)
        assertThat(result1).isEqualTo(result2)
        coVerify(exactly = 2) {
            apiService.getUsers()
        }
    }

    @Test
    fun `getUsers response with a very large list of users`() = runTest {
        // Given the repository returns a very large list of users to test for performance and memory constraints,
        val largeListSize = 10_000
        val largeUserList = (1..largeListSize).map { index ->
            UserDto(index, "user $index", "user$index@xyz.com", NameDto("user", "$index"))
        }
        coEvery {
            apiService.getUsers()
        } returns largeUserList
        // When getUsers is called,
        val resultUserList = userRepository.getUsers()
        // Then verify that the method can handle the large data set without causing OutOfMemoryError or significant performance degradation.
        assertThat(resultUserList).hasSize(largeListSize)
        assertThat(resultUserList.last().name).isEqualTo("user $largeListSize")
        coVerify(exactly = 1) {
            apiService.getUsers()
        }
    }

    @Test
    fun `getUsers when the underlying data source is empty or non existent`() = runTest {
        // Given the underlying data source (e.g., a database table or a remote endpoint) is empty or doesn't exist,
        val httpErrorResponse = Response.error<UserDto>(
            404,
            "Not found user".toResponseBody("text/plain".toMediaTypeOrNull())
        )
        coEvery {
            apiService.getUsers()
        } throws HttpException(httpErrorResponse)
        // When getUsers is called,
        val resultUserList = assertThrows<HttpException> {
            userRepository.getUsers()
        }
        // Then verify the repository implementation correctly returns an empty list or throws an appropriate exception.
        assertThat(resultUserList.code()).isEqualTo(404)
        coVerify(exactly = 1) {
            apiService.getUsers()
        }
    }

}