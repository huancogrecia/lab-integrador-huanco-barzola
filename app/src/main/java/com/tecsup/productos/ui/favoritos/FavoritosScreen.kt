package com.tecsup.productos.ui.favoritos

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
fun FavoritosScreen(
    vm: FavoritosViewModel,
    onProducto: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val errorFavorito by vm.errorFavorito.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Text("Favoritos", style = MaterialTheme.typography.headlineMedium)
        errorFavorito?.let { mensaje ->
            Text(mensaje, color = MaterialTheme.colorScheme.error)
        }
        when (val actual = estado) {
            UiState.Cargando -> Cargando()
            is UiState.Error -> ErrorView(actual.mensaje, vm::observar)
            is UiState.Exito -> {
                if (actual.datos.isEmpty()) {
                    Text(
                        text = "Aún no tienes productos favoritos",
                        modifier = Modifier.padding(top = 16.dp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(top = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
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
