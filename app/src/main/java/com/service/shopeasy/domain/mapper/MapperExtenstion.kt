package com.service.shopeasy.domain.mapper

import com.service.shopeasy.data.dto.ProductDto
import com.service.shopeasy.data.dto.UserDto
import com.service.shopeasy.domain.model.Product
import com.service.shopeasy.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    name = "${name.firstname} ${name.lastname}",
    email = email
)

fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title,
    price = price,
    description = description,
    category = category,
    image = image,

)