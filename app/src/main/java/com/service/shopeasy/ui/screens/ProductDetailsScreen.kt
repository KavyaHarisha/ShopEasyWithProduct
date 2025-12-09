package com.service.shopeasy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.service.shopeasy.domain.model.Product
import com.service.shopeasy.ui.viewmodel.ProductDetailsEffect
import com.service.shopeasy.ui.viewmodel.ProductDetailsIntent
import com.service.shopeasy.ui.viewmodel.ProductViewDetailsViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProductDetailsScreen(
    detailsViewModel: ProductViewDetailsViewModel,
    productId: Int,
    onAddFavorite: (product: Product) -> Unit
) {
    val productDetailsState by detailsViewModel.productDetailsState.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        detailsViewModel.onProductDetailsIntent(ProductDetailsIntent.LoadProductDetails(productId))
    }

    LaunchedEffect(detailsViewModel.productDetailsEffect) {
        detailsViewModel.productDetailsEffect.collectLatest { effect ->
            when (effect) {
                is ProductDetailsEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    when {
        productDetailsState.loading -> CenteredLoading()
        productDetailsState.error != null -> ErrorWithRetry(
            productDetailsState.error ?: "Unknown error"
        ) {
            detailsViewModel.onProductDetailsIntent(
                ProductDetailsIntent.LoadProductDetails(
                    productId
                )
            )
        }

        else -> {
            productDetailsState.productDetails?.let { product ->
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    val productImage = rememberAsyncImagePainter(model = product.image)
                    Image(
                        painter = productImage,
                        contentDescription = product.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = product.title, style = MaterialTheme.typography.headlineLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${product.price}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = product.description ?: "")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        onAddFavorite(product)
                        detailsViewModel.onProductDetailsIntent(
                            ProductDetailsIntent.AddToFavorites(product)
                        )
                    })
                    { Text(text = "Add to favorites") }
                }
            }
        }

    }
}