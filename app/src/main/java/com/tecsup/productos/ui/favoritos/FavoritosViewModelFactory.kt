package com.tecsup.productos.ui.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.productos.AppContainer

class FavoritosViewModelFactory(private val container: AppContainer, private val uid: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritosViewModel::class.java)) {
            return modelClass.cast(FavoritosViewModel(container.itemRepository(uid)))
                ?: throw IllegalArgumentException("No se pudo crear FavoritosViewModel")
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
