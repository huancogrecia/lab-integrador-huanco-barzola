package com.tecsup.productos.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.productos.AppContainer

class ListaViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListaViewModel::class.java)) {
            return modelClass.cast(ListaViewModel(container.itemRepository))
                ?: throw IllegalArgumentException("No se pudo crear ListaViewModel")
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
