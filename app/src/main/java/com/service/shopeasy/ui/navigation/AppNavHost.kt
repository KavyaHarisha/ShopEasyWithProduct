package com.service.shopeasy.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.service.shopeasy.ui.components.TopBar
import com.service.shopeasy.ui.screens.UserListScreen
import com.service.shopeasy.ui.viewmodel.UsersViewModel

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()){

    Scaffold(
        topBar = {TopBar(navController)}
    ) { inner ->
        NavHost(navController = navController, startDestination = Screen.User.route,
            modifier = Modifier.padding(inner)){
            composable(Screen.User.route){
                val userViewModel: UsersViewModel = hiltViewModel()
                UserListScreen(userViewModel)
            }
        }
    }

}