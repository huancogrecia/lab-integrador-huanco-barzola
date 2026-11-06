package com.tecsup.productos.ui.lista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.TarjetaProducto

@Composable
fun ListaScreen(vm: ListaViewModel, modifier: Modifier = Modifier, onProducto: (Int) -> Unit) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Text("Productos", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Catálogo de productos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )
        when (val actual = estado) {
            UiState.Cargando -> Cargando()
            is UiState.Error -> ErrorView(actual.mensaje, vm::cargar)
            is UiState.Exito -> {
                if (actual.datos.isEmpty()) {
                    Text("No hay productos disponibles")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(actual.datos, key = { it.id }) { item ->
                            TarjetaProducto(
                                item = item,
                                onClick = { onProducto(item.id) },
                                onAlternarFavorito = { vm.alternarFavorito(item.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
