package com.service.shopeasy.data.api

import com.service.shopeasy.data.dto.UserDto
import retrofit2.http.GET

interface ShopEasyApiService {

    @GET("users")
    suspend fun getUsers(): List<UserDto>

}