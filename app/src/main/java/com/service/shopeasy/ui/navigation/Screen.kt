package com.service.shopeasy.ui.navigation

sealed class Screen(val route: String) {
    object User: Screen("User")
}