package com.service.shopeasy.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Int,
    val username: String?,
    val email: String?,
    val name: NameDto?
)

@JsonClass(generateAdapter = true)
data class NameDto(
    val firstname: String?,
    val lastname: String?
)
