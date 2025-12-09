package com.service.shopeasy.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.service.shopeasy.ui.components.ProductCard
import com.service.shopeasy.ui.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(favoritesViewModel: FavoritesViewModel, onItemClick: (Int) -> Unit) {

    val allFavorites by favoritesViewModel.allFavorites.collectAsState()

    if (allFavorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No favorites yet")
        }
    } else {
        LazyColumn {
            items(allFavorites) { favProduct ->
                ProductCard(
                    favProduct, onClick = { onItemClick(favProduct.id) },
                    onFavorite = { favoritesViewModel.deleteFavorite(favProduct.id) })
            }
        }
    }

}