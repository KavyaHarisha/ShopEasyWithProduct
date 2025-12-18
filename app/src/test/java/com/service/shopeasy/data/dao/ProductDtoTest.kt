package com.service.shopeasy.data.dao

import com.google.common.truth.Truth.assertThat
import com.service.shopeasy.data.dto.ProductDto
import com.service.shopeasy.domain.mapper.toDomain
import org.junit.Test

class ProductDtoTest {

    @Test
    fun `ProductDto maps Product correctly`(){
        val productDto = ProductDto(
            id = 1,
            title = "Product 1",
            price = 10.0,
            description = "Description 1",
            category = "Category 1",
            image = "image1.jpg"
        )

        val productResult = productDto.toDomain()

        assertThat(productResult.id).isEqualTo(1)
        assertThat(productResult.title).isEqualTo("Product 1")
        assertThat(productResult.price).isEqualTo(10.0)
    }

}