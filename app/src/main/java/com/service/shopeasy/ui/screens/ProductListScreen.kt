package com.service.shopeasy.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.service.shopeasy.ui.components.ProductCard
import com.service.shopeasy.ui.viewmodel.ProductIntent
import com.service.shopeasy.ui.viewmodel.ProductsViewModel

@Composable
fun ProductListScreen(viewModel: ProductsViewModel = hiltViewModel(), onProductClick: () -> Unit){

    val productsState by viewModel.productListState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onProductListIntent(ProductIntent.LoadProducts)
    }

    when {
        productsState.loading -> CenteredLoading()
        productsState.error != null -> ErrorWithRetry(productsState.error!!){
            viewModel.onProductListIntent(ProductIntent.LoadProducts)
        }
        else -> {
            LazyColumn {
                items(
                    items = productsState.products,
                    key = {product -> product.id}
                ){product ->
                    ProductCard(product = product, onClick = onProductClick, onFavorite = {})
                }
            }
        }
    }

}