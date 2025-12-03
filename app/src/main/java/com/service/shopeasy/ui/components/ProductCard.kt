package com.service.shopeasy.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.service.shopeasy.domain.model.Product

@Composable
fun ProductCard(product: Product, onClick: () -> Unit, onFavorite: () -> Unit,modifier: Modifier = Modifier){
    Card(modifier = modifier.fillMaxWidth().padding(8.dp).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(6.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val rememberAsyncImagePainter = rememberAsyncImagePainter(model = product.image)
            Image(painter = rememberAsyncImagePainter,
                contentDescription = product.title, modifier = Modifier.size(88.dp))

            Spacer(modifier = Modifier.padding(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.title, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "$${product.price}")
            }

            IconButton(onClick = { onFavorite }){
                Icon(Icons.Default.Favorite, contentDescription = "favorite")
            }
        }
    }
}