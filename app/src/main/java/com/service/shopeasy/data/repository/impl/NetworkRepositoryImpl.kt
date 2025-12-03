package com.service.shopeasy.data.repository.impl

import com.service.shopeasy.data.api.ShopEasyApiService
import com.service.shopeasy.data.repository.ProductRepository
import com.service.shopeasy.data.repository.UserRepository
import com.service.shopeasy.domain.mapper.toDomain
import com.service.shopeasy.domain.model.Product
import com.service.shopeasy.domain.model.User
import javax.inject.Inject

class NetworkRepositoryImpl @Inject constructor(val shopEasyApiService: ShopEasyApiService): UserRepository,
    ProductRepository {
    override suspend fun getUsers(): List<User> =  shopEasyApiService.getUsers().map { it.toDomain() }
    override suspend fun getProducts(): List<Product> = shopEasyApiService.getProducts().map { it.toDomain() }
    override suspend fun getProduct(id: Int): Product = shopEasyApiService.getProduct(id).toDomain()
}