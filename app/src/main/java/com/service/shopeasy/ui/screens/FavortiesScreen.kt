package com.service.shopeasy.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.service.shopeasy.ui.components.ProductCard
import com.service.shopeasy.ui.viewmodel.FavoritesViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(favoritesViewModel: FavoritesViewModel, onItemClick: (Int) -> Unit) {

    /*
    * For better performance.
    * https://www.linkedin.com/posts/ibrahim-asgari_performanceoptimization-jetpackcompose-androiddevelopment-share-7421845377026494464-d229?utm_source=share&utm_medium=member_desktop&rcm=ACoAAA0h4zkBHeoH2TunSUAjl_H-BJ9b0AhLYeI
    * */
    val allFavorites by favoritesViewModel.allFavorites.collectAsStateWithLifecycle()
    val cacheState = remember {
        LazyLayoutCacheWindow(
            ahead = 240.dp,
            behind = 120.dp
        )
    }

    val state = rememberLazyListState(cacheWindow = cacheState)


    if (allFavorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No favorites yet")
        }
    } else {
        LazyColumn(state = state) {
            items(allFavorites) { favProduct ->
                ProductCard(
                    favProduct, onClick = { onItemClick(favProduct.id) },
                    onFavorite = { favoritesViewModel.deleteFavorite(favProduct.id) })
            }
        }
    }

}