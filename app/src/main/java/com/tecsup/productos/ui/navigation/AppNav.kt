package com.tecsup.productos.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
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
import com.tecsup.productos.ui.favoritos.FavoritosScreen
import com.tecsup.productos.ui.favoritos.FavoritosViewModel
import com.tecsup.productos.ui.favoritos.FavoritosViewModelFactory
import com.tecsup.productos.ui.lista.ListaScreen
import com.tecsup.productos.ui.lista.ListaViewModel
import com.tecsup.productos.ui.lista.ListaViewModelFactory

private const val TAB_CATALOGO = "catalogo"
private const val TAB_FAVORITOS = "favoritos"

@Composable
fun AppNav(container: AppContainer, modifier: Modifier = Modifier) {
    var productoId by rememberSaveable { mutableStateOf<Int?>(null) }
    var tabSeleccionada by rememberSaveable { mutableStateOf(TAB_CATALOGO) }
    val listaFactory = remember(container) { ListaViewModelFactory(container) }
    val favoritosFactory = remember(container) { FavoritosViewModelFactory(container) }
    val listaVm: ListaViewModel = viewModel(factory = listaFactory)
    val favoritosVm: FavoritosViewModel = viewModel(factory = favoritosFactory)
    val id = productoId

    BackHandler(enabled = id != null || tabSeleccionada != TAB_CATALOGO) {
        if (id != null) productoId = null else tabSeleccionada = TAB_CATALOGO
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            if (id != null) {
                val factory = remember(container, id) { DetalleViewModelFactory(container, id) }
                val vm: DetalleViewModel = viewModel(key = "detalle_$id", factory = factory)
                DetalleScreen(vm = vm, onVolver = { productoId = null })
            } else if (tabSeleccionada == TAB_FAVORITOS) {
                FavoritosScreen(
                    vm = favoritosVm,
                    onProducto = { productoId = it },
                )
            } else {
                ListaScreen(
                    vm = listaVm,
                    onProducto = { productoId = it },
                )
            }
        }

        if (id == null) {
            NavigationBar {
                NavigationBarItem(
                    selected = tabSeleccionada == TAB_CATALOGO,
                    onClick = { tabSeleccionada = TAB_CATALOGO },
                    icon = { Text("▦") },
                    label = { Text("Catálogo") },
                )
                NavigationBarItem(
                    selected = tabSeleccionada == TAB_FAVORITOS,
                    onClick = { tabSeleccionada = TAB_FAVORITOS },
                    icon = { Text("♥") },
                    label = { Text("Favoritos") },
                )
            }
        }
    }
}
