package com.service.shopeasy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.service.shopeasy.data.repository.ProductRepository
import com.service.shopeasy.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProductDetailsIntent{
    data class LoadProductDetails(val id: Int): ProductDetailsIntent
}

data class ProductDetailsState(
    val loading: Boolean = false,
    val productDetails: Product? = null,
    val error: String? = null
)

@HiltViewModel
class ProductViewDetailsViewModel @Inject constructor(private val productRepository: ProductRepository): ViewModel() {
    private val _productDetailsState = MutableStateFlow(ProductDetailsState())
    val productDetailsState = _productDetailsState.asStateFlow()

    fun onProductDetailsIntent(detailsIntent: ProductDetailsIntent){
        when(detailsIntent){
            is ProductDetailsIntent.LoadProductDetails -> loadDetails(detailsIntent.id)
        }
    }

    private fun loadDetails(productId: Int) = viewModelScope.launch{
        _productDetailsState.value = _productDetailsState.value.copy(loading = true,error = null)
        try {
            _productDetailsState.value = _productDetailsState.value.copy(
                loading = false, productDetails = productRepository.getProduct(productId))
        }catch (e: Exception){
            _productDetailsState.value = _productDetailsState.value.copy(loading = false, error = e.localizedMessage ?: "Unknown error")
        }
    }

}