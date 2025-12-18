package com.service.shopeasy.data.dao

import com.google.common.truth.Truth.assertThat
import com.service.shopeasy.data.dto.NameDto
import com.service.shopeasy.data.dto.UserDto
import com.service.shopeasy.domain.mapper.toDomain
import org.junit.Test

class UserDtoTest {

    @Test
    fun `UserDto maps User correctly`(){
        val userDto = UserDto(
            id = 1,
            username = "Jhon Smith",
            email = "william.henry.harrison@example-pet-store.com",
            name = NameDto("Jhon","Smith")
        )

        val userResult = userDto.toDomain()

        assertThat(userResult.id).isEqualTo(1)
        assertThat(userResult.name).isEqualTo("Jhon Smith")
        assertThat(userResult.email).isEqualTo("william.henry.harrison@example-pet-store.com")
    }

}