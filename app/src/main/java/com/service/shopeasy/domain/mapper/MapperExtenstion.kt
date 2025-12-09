package com.service.shopeasy.domain.mapper

import com.service.shopeasy.data.dto.ProductDto
import com.service.shopeasy.data.dto.UserDto
import com.service.shopeasy.domain.model.Product
import com.service.shopeasy.domain.model.User
import com.service.shopeasy.data.local.entity.FavoriteEntity

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

fun Product.toFavoriteEntity(): FavoriteEntity = FavoriteEntity(
    id = id,
    title = title,
    image = image,
    price = price,
    description = description,
    category = category
)

fun FavoriteEntity.toProduct(): Product = Product(
    id = id,
    title = title,
    price = price,
    description = description,
    category = category,
    image = image
)