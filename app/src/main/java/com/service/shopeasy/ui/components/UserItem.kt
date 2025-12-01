package com.service.shopeasy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.service.shopeasy.domain.model.User

@Composable
fun UserItem(user: User,modifier: Modifier){
    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(Icons.Default.Person,
                contentDescription = "user", modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.padding(12.dp))
            Column {
                Text(text = user.name)
                Text(text = user.email)
            }
        }
    }
}