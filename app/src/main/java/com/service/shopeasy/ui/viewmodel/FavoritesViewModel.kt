package com.service.shopeasy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.service.shopeasy.data.repository.FavoritesRepository
import com.service.shopeasy.domain.mapper.toProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val favoritesRepo: FavoritesRepository): ViewModel() {

    val allFavorites = favoritesRepo.getAllFavorites().map { it.map { entity -> entity.toProduct() } }
        .distinctUntilChanged() //If you want to avoid re-emitting identical lists, add distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly,emptyList())

    fun deleteFavorite(id: Int) = viewModelScope.launch {
        favoritesRepo.deleteFavorite(id)
    }

}