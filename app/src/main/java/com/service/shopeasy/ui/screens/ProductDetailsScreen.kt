package com.service.shopeasy.ui.screens

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
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.service.shopeasy.ui.viewmodel.ProductDetailsIntent
import com.service.shopeasy.ui.viewmodel.ProductViewDetailsViewModel

@Composable
fun ProductDetailsScreen(
    detailsViewModel: ProductViewDetailsViewModel,
    productId: Int,
    onAddFavorite: () -> Unit
) {
    val productDetailsState by detailsViewModel.productDetailsState.collectAsState()

    LaunchedEffect(Unit) {
        detailsViewModel.onProductDetailsIntent(ProductDetailsIntent.LoadProductDetails(productId))
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
                    Button(onClick = onAddFavorite) { Text(text = "Add to favorites") }
                }
            }
        }

    }
}