package com.service.shopeasy.data.api

import com.service.shopeasy.data.dto.ProductDto
import com.service.shopeasy.data.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ShopEasyApiService {

    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

}