package com.service.shopeasy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.service.shopeasy.data.repository.FavoritesRepository
import com.service.shopeasy.data.repository.ProductRepository
import com.service.shopeasy.domain.mapper.toFavoriteEntity
import com.service.shopeasy.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProductDetailsIntent{
    data class LoadProductDetails(val id: Int): ProductDetailsIntent
    data class AddToFavorites(val favProduct: Product): ProductDetailsIntent
}

data class ProductDetailsState(
    val loading: Boolean = false,
    val productDetails: Product? = null,
    val error: String? = null
)

sealed interface ProductDetailsEffect{
    data class ShowToast(val message: String): ProductDetailsEffect
}


@HiltViewModel
class ProductViewDetailsViewModel @Inject constructor(private val productRepository: ProductRepository,
                                                      private val favRepo: FavoritesRepository): ViewModel() {
    private val _productDetailsState = MutableStateFlow(ProductDetailsState())
    val productDetailsState = _productDetailsState.asStateFlow()

    private val _productDetailsEffect = MutableSharedFlow<ProductDetailsEffect>()
    val productDetailsEffect = _productDetailsEffect.asSharedFlow()


    fun onProductDetailsIntent(detailsIntent: ProductDetailsIntent){
        when(detailsIntent){
            is ProductDetailsIntent.LoadProductDetails -> loadDetails(detailsIntent.id)
            is ProductDetailsIntent.AddToFavorites -> addToFavorites(detailsIntent.favProduct)
        }
    }

    private fun loadDetails(productId: Int) = viewModelScope.launch{
        _productDetailsState.update { currentState ->
            currentState.copy(loading = true, error = null)
        }
        try {
            _productDetailsState.update { currentState ->
                currentState.copy(loading = false, productDetails = productRepository.getProduct(productId))
            }
        }catch (e: Exception){
            _productDetailsState.update { currentState ->
                currentState.copy(loading = false, error = e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private fun addToFavorites(product: Product) = viewModelScope.launch {
        favRepo.saveFavorite(favoriteItem = product.toFavoriteEntity())
        _productDetailsEffect.emit(ProductDetailsEffect.ShowToast("Added to favorites"))
    }

}