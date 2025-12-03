package com.service.shopeasy.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.service.shopeasy.ui.navigation.Screen

data class BottomNavItem (
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomBar(navController: NavController){
    val navItems = listOf(
        BottomNavItem(label = Screen.Product.route, Icons.Default.Store, Screen.Product.route)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        navItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                  navController.navigate(item.route){
                      popUpTo(navController.graph.startDestinationId){
                          saveState = true
                      }
                      launchSingleTop = true
                      restoreState = true
                  }
                }
            )
        }
    }
}