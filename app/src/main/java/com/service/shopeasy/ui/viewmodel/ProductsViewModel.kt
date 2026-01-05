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

sealed interface ProductIntent{
    object LoadProducts: ProductIntent
    data class AddToFavorites(val product: Product): ProductIntent
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
class ProductsViewModel @Inject constructor(private val productRepository: ProductRepository,
    private val favRepo: FavoritesRepository) :
    ViewModel() {
    private val _productListState = MutableStateFlow(ProductState())
    val productListState = _productListState.asStateFlow()

    private val _uiEffectState = MutableSharedFlow<UiEffect>()
    val uiEffectState = _uiEffectState.asSharedFlow()


    fun onProductListIntent(productIntent: ProductIntent) {
        when (productIntent) {
            is ProductIntent.LoadProducts -> loadProducts()
            is ProductIntent.AddToFavorites -> addFavorite(productIntent.product)
        }
    }

    private fun loadProducts() =
        viewModelScope.launch {
            /*
            * https://www.linkedin.com/feed/update/urn:li:activity:7413559045883592704?updateEntityUrn=urn%3Ali%3Afs_updateV2%3A%28urn%3Ali%3Aactivity%3A7413559045883592704%2CFEED_DETAIL%2CEMPTY%2CDEFAULT%2Cfalse%29
            * Use .update instead of .copy for state flow object for Thread Safety,Consistency, and No Lost Data
            * */
            _productListState.update { currentState ->
                currentState.copy(loading = true, error = null)
            }
            try {
                val productsResult = productRepository.getProducts()
                _productListState.update { currentState ->
                    currentState.copy(loading = false, products = productsResult)
                }
            }catch (e: Exception){
                _productListState.update { currentState ->
                    currentState.copy(
                        loading = false,
                        error = e.localizedMessage ?: "Unknown error"
                    )
                }
            }
        }

    private fun addFavorite(product: Product) = viewModelScope.launch {
        favRepo.saveFavorite(product.toFavoriteEntity())
        _uiEffectState.emit(UiEffect.ShowToast("Added to favorites"))
    }
}