package com.service.shopeasy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.service.shopeasy.ui.components.ProductCard
import com.service.shopeasy.ui.viewmodel.ProductIntent
import com.service.shopeasy.ui.viewmodel.ProductsViewModel
import com.service.shopeasy.ui.viewmodel.UiEffect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProductListScreen(viewModel: ProductsViewModel = hiltViewModel(), onProductClick: (Int) -> Unit){

    val productsState by viewModel.productListState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onProductListIntent(ProductIntent.LoadProducts)
    }

    LaunchedEffect(viewModel.uiEffectState) {
        viewModel.uiEffectState.collectLatest { effect ->
            when (effect) {
                is UiEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
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
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onFavorite = {
                            viewModel.onProductListIntent(
                                ProductIntent.AddToFavorites(product)
                            )
                        })
                }
            }
        }
    }

}