package com.service.shopeasy.ui.navigation

sealed class Screen(val route: String) {
    object User: Screen("users")
    object Product: Screen("products")
    object ProductDetails: Screen("product/{productId}"){
        fun createRoute(productId: Int) = "product/$productId"
    }
}