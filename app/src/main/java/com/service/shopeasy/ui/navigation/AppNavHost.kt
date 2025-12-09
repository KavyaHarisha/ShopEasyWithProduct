package com.service.shopeasy.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.service.shopeasy.ui.components.BottomBar
import com.service.shopeasy.ui.components.TopBar
import com.service.shopeasy.ui.screens.FavoritesScreen
import com.service.shopeasy.ui.screens.ProductDetailsScreen
import com.service.shopeasy.ui.screens.ProductListScreen
import com.service.shopeasy.ui.screens.UserListScreen
import com.service.shopeasy.ui.viewmodel.FavoritesViewModel
import com.service.shopeasy.ui.viewmodel.ProductViewDetailsViewModel
import com.service.shopeasy.ui.viewmodel.ProductsViewModel
import com.service.shopeasy.ui.viewmodel.UsersViewModel

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()){

    Scaffold(
        topBar = {TopBar(navController)},
        bottomBar = { BottomBar(navController) }
    ) { inner ->
        NavHost(navController = navController, startDestination = Screen.Product.route,
            modifier = Modifier.padding(inner)){
            composable(Screen.User.route){
                val userViewModel: UsersViewModel = hiltViewModel()
                UserListScreen(userViewModel)
            }
            composable(Screen.Product.route){
                val productViewModel: ProductsViewModel = hiltViewModel()
                ProductListScreen(productViewModel) { id ->
                    navController.navigate(Screen.ProductDetails.createRoute(id))
                }
            }
            composable(Screen.ProductDetails.route, arguments = listOf(navArgument("productId"){
                type = NavType.IntType
            })){
                val id = requireNotNull(it.arguments?.getInt("productId"))
                val productDetailsViewModel: ProductViewDetailsViewModel = hiltViewModel()
                ProductDetailsScreen(productDetailsViewModel,id) {

                }
            }

            composable(Screen.Favorite.route){
                val favoriteViewModel: FavoritesViewModel = hiltViewModel()
                FavoritesScreen(favoriteViewModel) { productId ->
                    navController.navigate(Screen.ProductDetails.createRoute(productId))
                }
            }
        }
    }

}