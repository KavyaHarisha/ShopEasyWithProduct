package com.service.shopeasy.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.service.shopeasy.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController){
    TopAppBar(
        title = { Text("ShopEasy") },
        actions = {
            IconButton(onClick = { navController.navigate(Screen.User.route) }) {
                Icon(Icons.Default.Person, contentDescription = Screen.User.route)
            }
        }
    )
}