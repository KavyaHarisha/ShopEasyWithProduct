package com.service.shopeasy.domain.mapper

import com.service.shopeasy.data.dto.UserDto
import com.service.shopeasy.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    name = "${name.firstname} ${name.lastname}",
    email = email
)