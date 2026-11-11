package com.tecsup.productos.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.productos.AppContainer
import com.tecsup.productos.domain.model.Sesion

class DetalleViewModelFactory(private val container: AppContainer, private val sesion: Sesion, private val id: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetalleViewModel::class.java)) {
            return modelClass.cast(
                DetalleViewModel(
                    container.itemRepository(sesion.uid),
                    container.carritoRepository(sesion.uid),
                    container.inventarioRepository,
                    sesion,
                    id,
                )
            )
                ?: throw IllegalArgumentException("No se pudo crear DetalleViewModel")
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
