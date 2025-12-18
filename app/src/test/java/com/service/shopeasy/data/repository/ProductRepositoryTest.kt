package com.service.shopeasy.data.repository

import com.google.common.truth.Truth.assertThat
import com.service.shopeasy.CoroutinesTestRule
import com.service.shopeasy.data.api.ShopEasyApiService
import com.service.shopeasy.data.dto.ProductDto
import com.service.shopeasy.data.repository.impl.NetworkRepositoryImpl
import com.service.shopeasy.domain.mapper.toDomain
import com.service.shopeasy.domain.model.Product
import com.squareup.moshi.JsonDataException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ProductRepositoryTest {

    @get:Rule
    val rule = CoroutinesTestRule()

    private val apiService = mockk<ShopEasyApiService>()
    private lateinit var productRepository: ProductRepository

    @Before
    fun setup() {
        productRepository = NetworkRepositoryImpl(apiService)
    }


    @Test
    fun `getProducts successful response with multiple products`() = runTest {
        // Call getProducts and verify it returns a list containing multiple products.
        //Give data
        coEvery {
            apiService.getProducts()
        } returns listOf(
            ProductDto(1, "Product 1", 10.0, "Description 1", "Category 1", "image1.jpg"),
            ProductDto(2, "Product 2", 20.0, "Description 2", "Category 2", "image2.jpg"),
            ProductDto(3, "Product 3", 30.0, "Description 3", "Category 3", "image3.jpg")
        )

        //when
        val productList = productRepository.getProducts()

        //Then
        assertThat(productList.size).isEqualTo(3)
        assertThat(productList[2].description).isEqualTo("Description 3")

        coVerify(exactly = 1) { apiService.getProducts() }

    }

    @Test
    fun `getProducts successful response with a single product`() = runTest {
        // Call getProducts and verify it returns a list containing exactly one product.
        coEvery {
            apiService.getProducts()
        } returns listOf(
            ProductDto(1, "Product 1", 10.0, "Description 1", "Category 1", "image1.jpg")
        )

        val productOneItemList = productRepository.getProducts()

        assertThat(productOneItemList.size).isEqualTo(1)
        coVerify(exactly = 1) {
            apiService.getProducts()
        }
    }

    @Test
    fun `getProducts successful response with no products`() = runTest {
        // Call getProducts and verify it returns an empty list when no products are available.
        coEvery {
            apiService.getProducts()
        } returns listOf()

        val noProductList = productRepository.getProducts()

        assertThat(noProductList.size).isEqualTo(0)
        coVerify(exactly = 1) {
            apiService.getProducts()
        }
    }

    @Test
    fun `getProducts network error`() = runTest {
        // Simulate a network failure and verify that getProducts throws an appropriate exception (e.g., IOException).
        coEvery {
            apiService.getProducts()
        } throws IOException("Network failed")

        assertThrows<IOException> {
            productRepository.getProducts()
        }
        coVerify(exactly = 1) {
            apiService.getProducts()
        }
    }

    @Test
    fun `getProducts server error`() = runTest {
        // Simulate a server-side error (e.g., 500 Internal Server Error) and verify that getProducts throws a corresponding HttpException or custom server exception.

        val errorResponse = Response.error<List<ProductDto>>(
            500,
            "Internal Server Error".toResponseBody("text/plain".toMediaTypeOrNull())
        )
        coEvery {
            apiService.getProducts()
        } throws HttpException(errorResponse)

        assertThrows<HttpException> {
            productRepository.getProducts()
        }
        coVerify(exactly = 1) {
            apiService.getProducts()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getProducts coroutine cancellation`() = runTest {
        // Launch the getProducts call in a coroutine, cancel the coroutine, and verify that the function call is properly cancelled and does not complete.
        coEvery {
            apiService.getProducts()
        } coAnswers {
            delay(1000)
            emptyList()
        }

        val job = async {
            productRepository.getProducts()
        }
        advanceTimeBy(500)
        job.cancel()
        assertThrows<CancellationException> {
            job.await()
        }
        coVerify(atLeast = 1) {
            apiService.getProducts()
        }
    }

    @Test
    fun `getProducts with timeout`() = runTest {
        // Call getProducts and simulate a response delay that exceeds a defined timeout, verifying that a TimeoutCancellationException is thrown.
        assertThrows<TimeoutCancellationException> {
            withTimeout(500) {
                coEvery {
                    apiService.getProducts()
                } coAnswers {
                    delay(1000)
                    emptyList()
                }

                productRepository.getProducts()
            }
        }

    }

    @Test
    fun `getProducts with malformed JSON response`() = runTest {
        // Simulate a response from the server with malformed JSON and verify that a deserialization exception is thrown.
        coEvery {
            apiService.getProducts()
        } throws JsonDataException("Malformed JSON response")
        assertThrows<JsonDataException> {
            productRepository.getProducts()
        }
    }

    @Test
    fun `getProduct successful response`() = runTest {
        // Call getProduct with a valid and existing product ID and verify that the correct Product object is returned.
        val productId = 3
        val expectedProduct = ProductDto(
            id = productId,
            title = "Product 3",
            price = 30.0,
            description = "Description 3",
            category = "Category 3",
            image = "image3.jpg"
        )
        coEvery {
            apiService.getProduct(productId)
        } returns expectedProduct

        val resultProduct = productRepository.getProduct(productId)

        assertThat(resultProduct).isNotNull()
        assertThat(resultProduct.id).isEqualTo(productId)
        assertThat(resultProduct.title).isEqualTo(expectedProduct.title)

        coVerify(exactly = 1) {
            apiService.getProduct(productId)
        }
    }

    @Test
    fun `getProduct with non existent ID`() = runTest {
        // Call getProduct with a product ID that does not exist and verify that an appropriate exception is thrown (e.g., NotFoundException or returning null).
        val nonExistentProductId = 999
        val errorResponse = Response.error<ProductDto>(
            404,
            "Not Found".toResponseBody("text/plain".toMediaTypeOrNull())
        )
        val httpException = HttpException(errorResponse)
        coEvery {
            apiService.getProduct(nonExistentProductId)
        } throws httpException

        val exception = assertThrows<HttpException> {
            productRepository.getProduct(nonExistentProductId)
        }
        assertThat(exception.code()).isEqualTo(404)

        coVerify(exactly = 1) {
            apiService.getProduct(nonExistentProductId)
        }
    }

    @Test
    fun `getProduct with invalid ID format  negative number `() = runTest {
        // Call getProduct with a negative integer as the ID and verify it handles the invalid input gracefully, likely throwing an IllegalArgumentException.
        val negativeProductId = -1
        val errorResponse = Response.error<ProductDto>(
            404,
            "Invalid Id".toResponseBody("text/plain".toMediaTypeOrNull())
        )
        val httpException = HttpException(errorResponse)
        coEvery {
            apiService.getProduct(negativeProductId)
        } throws httpException

        val negativeException = assertThrows<HttpException> {
            productRepository.getProduct(negativeProductId)
        }

        assertThat(negativeException.message()).isEqualTo(errorResponse.message())
        coVerify(exactly = 1) {
            apiService.getProduct(negativeProductId)
        }

    }

    @Test
    fun `getProduct network error`() = runTest {
        // Simulate a network failure while calling getProduct and verify it throws an appropriate exception like IOException.
        val productId = 1
        val networkException = IOException("Network failed")
        coEvery {
            apiService.getProduct(any())
        } throws networkException
        assertThrows<IOException> {
            productRepository.getProduct(productId)
        }
        coVerify {
            apiService.getProduct(productId)
        }
    }

    @Test
    fun `getProduct server error`() = runTest {
        // Simulate a server-side error (e.g., 500 Internal Server Error) for a getProduct call and verify it throws a corresponding HttpException or custom exception.
        val productId = 1
        val serverError = Response.error<Product>(
            500,
            "Internal Server Error".toResponseBody("text/plain".toMediaTypeOrNull())
        )
        coEvery {
            apiService.getProduct(productId)
        } throws HttpException(serverError)

        val resultServerException = assertThrows<HttpException> {
            productRepository.getProduct(productId)
        }
        assertThat(resultServerException.message()).isEqualTo(serverError.message())
        coVerify(exactly = 1) {
            apiService.getProduct(productId)
        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getProduct coroutine cancellation`() = runTest {
        // Launch getProduct in a coroutine, cancel it, and verify the function execution is properly cancelled.
        val productId = 1
        coEvery {
            apiService.getProduct(productId)
        } coAnswers {
            delay(1000)
            ProductDto(productId, "", 0.0, "", "", "")
        }

        val deferred = async {
            productRepository.getProduct(productId)
        }
        advanceTimeBy(500)
        deferred.cancel()
        assertThrows<CancellationException> {
            deferred.await()
        }
        coVerify(atLeast = 1) { apiService.getProduct(productId) }
    }

    @Test
    fun `getProduct with timeout`() = runTest {
        // Call getProduct and simulate a response delay that exceeds a defined timeout, ensuring a TimeoutCancellationException is thrown.
        val productId = 1
        assertThrows<TimeoutCancellationException> {
            withTimeout(500) {
                coEvery { apiService.getProduct(productId) } coAnswers {
                    delay(1000)
                    ProductDto(productId, "", 0.0, "", "", "")
                }

                productRepository.getProduct(productId)
            }
        }
        coVerify(atLeast = 1) {
            apiService.getProduct(productId)
        }
    }

    @Test
    fun `getProduct with malformed JSON response`() = runTest {
        // Simulate a server response for a single product with malformed JSON and verify a deserialization exception occurs.
        val productId = 1
        coEvery {
            apiService.getProduct(productId)
        } throws JsonDataException("Malformed Json Response")

        assertThrows<JsonDataException> {
            productRepository.getProduct(productId)
        }
        coVerify(atLeast = 1) {
            apiService.getProduct(productId)
        }

    }

    @Test
    fun `Concurrent calls to getProducts`() = runTest {
        // Make multiple concurrent calls to getProducts and verify that each call receives the correct, complete list of products without data corruption or race conditions.
        val expectedProductList = listOf(
            ProductDto(1, "Product 1", 10.0, "Description 1", "Category 1", "image"),
            ProductDto(2, "Product 2", 20.0, "Description 2", "Category 2", "image"),
            ProductDto(3, "Product 3", 30.0, "Description 3", "Category 3", "image")
        )

        coEvery {
            apiService.getProducts()
        } returns expectedProductList

        val deferred1 = async { productRepository.getProducts() }
        val deferred2 = async { productRepository.getProducts() }
        val deferred3 = async { productRepository.getProducts() }

        val result1 = deferred1.await()
        val result2 = deferred2.await()
        val result3 = deferred3.await()

        assertThat(result1).hasSize(3)
        assertThat(result1[0].id).isEqualTo(1)
        assertThat(result2).isEqualTo(expectedProductList.map { it.toDomain() })
        assertThat(result3).isEqualTo(expectedProductList.map { it.toDomain() })

        coVerify(exactly = 3) {
            apiService.getProducts()
        }
    }

    @Test
    fun `Concurrent calls to getProduct with same ID`() = runTest {
        // Make multiple concurrent calls to getProduct using the same ID and verify that each call successfully returns the correct product.
        val productId = 1
        val expectedProduct =
            ProductDto(productId, "Product 1", 10.0, "Description 1", "Category 1", "image")
        coEvery {
            apiService.getProduct(productId)
        } returns expectedProduct

        val deferred1 = async { productRepository.getProduct(productId) }
        val deferred2 = async { productRepository.getProduct(productId) }

        val result1 = deferred1.await()
        val result2 = deferred2.await()

        assertThat(result1.id).isEqualTo(productId)
        assertThat(result2.id).isEqualTo(productId)

        coVerify(exactly = 2) {
            apiService.getProduct(productId)
        }
    }

    @Test
    fun `Concurrent calls to getProduct with different IDs`() = runTest {
        // Make multiple concurrent calls to getProduct using different, valid IDs and verify that each call returns the correct corresponding product.
        val productId1 = 1
        val productId2 = 2
        val expectedProduct1 =
            ProductDto(productId1, "Product 1", 10.0, "Description 1", "Category 1", "image")
        val expectedProduct2 =
            ProductDto(productId2, "Product 2", 20.0, "Description 2", "Category 2", "image")

        coEvery {
            apiService.getProduct(productId1)
        } returns expectedProduct1
        coEvery {
            apiService.getProduct(productId2)
        } returns expectedProduct2

        val deferred1 = async {
            productRepository.getProduct(productId1)
        }
        val deferred2 = async {
            productRepository.getProduct(productId2)
        }

        val result1 = deferred1.await()
        val result2 = deferred2.await()

        assertThat(result1.id).isEqualTo(1)
        assertThat(result2.id).isEqualTo(2)
        assertThat(result2.title).isEqualTo("Product 2")

        coVerify(exactly = 1) {
            apiService.getProduct(productId1)
            apiService.getProduct(productId2)
        }
    }

}