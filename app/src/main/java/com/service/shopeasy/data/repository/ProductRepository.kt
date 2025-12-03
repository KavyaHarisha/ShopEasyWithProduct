package com.service.shopeasy.data.repository

import com.service.shopeasy.domain.model.Product

interface ProductRepository {

    suspend fun getProducts(): List<Product>
    suspend fun getProduct(id: Int): Product

}