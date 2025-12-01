package com.service.shopeasy.data.repository

import com.service.shopeasy.domain.model.User

interface UserRepository {

    suspend fun getUsers(): List<User>

}