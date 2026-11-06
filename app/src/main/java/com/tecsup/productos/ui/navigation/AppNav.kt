package com.tecsup.productos.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.productos.AppContainer
import com.tecsup.productos.ui.detalle.DetalleScreen
import com.tecsup.productos.ui.detalle.DetalleViewModel
import com.tecsup.productos.ui.detalle.DetalleViewModelFactory
import com.tecsup.productos.ui.lista.ListaScreen
import com.tecsup.productos.ui.lista.ListaViewModel
import com.tecsup.productos.ui.lista.ListaViewModelFactory

@Composable
fun AppNav(container: AppContainer, modifier: Modifier = Modifier) {
    var productoId by rememberSaveable { mutableStateOf<Int?>(null) }
    val listaFactory = remember(container) { ListaViewModelFactory(container) }
    val listaVm: ListaViewModel = viewModel(factory = listaFactory)
    val id = productoId
    BackHandler(enabled = id != null) { productoId = null }
    if (id == null) {
        ListaScreen(vm = listaVm, modifier = modifier, onProducto = { productoId = it })
    } else {
        val factory = remember(container, id) { DetalleViewModelFactory(container, id) }
        val vm: DetalleViewModel = viewModel(key = "detalle_$id", factory = factory)
        DetalleScreen(vm = vm, onVolver = { productoId = null }, modifier = modifier)
    }
}
