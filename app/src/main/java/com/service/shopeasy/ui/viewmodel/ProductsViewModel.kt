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

sealed interface ProductIntent{
    object LoadProducts: ProductIntent
}

sealed interface UiEffect {
    data class ShowToast(val message: String): UiEffect
}

data class ProductState(
    val loading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProductsViewModel @Inject constructor(private val productRepository: ProductRepository) :
    ViewModel() {
    private val _productListState = MutableStateFlow(ProductState())
    val productListState = _productListState.asStateFlow()

    fun onProductListIntent(productIntent: ProductIntent) {
        when (productIntent) {
            is ProductIntent.LoadProducts -> loadProducts()
        }
    }

    private fun loadProducts() =
        viewModelScope.launch {
            _productListState.value = _productListState.value.copy(loading = true, error = null)
            try {
                val productsResult = productRepository.getProducts()
                _productListState.value = _productListState.value.copy(loading = false,products = productsResult)
            }catch (e: Exception){
                _productListState.value= _productListState.value.copy(loading = false, error = e.localizedMessage ?: "Unknown error")
            }
        }
}