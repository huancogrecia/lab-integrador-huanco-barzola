package com.tecsup.productos.ui.lista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.TarjetaProducto

@Composable
fun ListaScreen(
    vm: ListaViewModel,
    modifier: Modifier = Modifier,
    puedeGuardarFavoritos: Boolean = true,
    onProducto: (Int) -> Unit,
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val errorFavorito by vm.errorFavorito.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val orden by vm.orden.collectAsStateWithLifecycle()
    var mostrarOrden by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Productos", style = MaterialTheme.typography.headlineLarge)
                Text(
                    text = "Catálogo de laptops",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            OutlinedTextField(
                value = busqueda,
                onValueChange = vm::cambiarBusqueda,
                label = { Text("Buscar por nombre o marca") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        TextButton(onClick = { vm.cambiarBusqueda("") }) { Text("Limpiar") }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Box {
                OutlinedButton(onClick = { mostrarOrden = true }) {
                    Text("Ordenar: ${orden.etiqueta}")
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(expanded = mostrarOrden, onDismissRequest = { mostrarOrden = false }) {
                    OrdenProductos.entries.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion.etiqueta) },
                            onClick = {
                                vm.cambiarOrden(opcion)
                                mostrarOrden = false
                            },
                        )
                    }
                }
            }
        }
        aviso?.let { mensaje ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(text = mensaje, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = vm::cargar) { Text("Reintentar") }
                    }
                }
            }
        }
        errorFavorito?.let { mensaje ->
            item { Text(mensaje, color = MaterialTheme.colorScheme.error) }
        }
        when (val actual = estado) {
            UiState.Cargando -> item { Cargando() }
            is UiState.Error -> item { ErrorView(actual.mensaje, vm::cargar) }
            is UiState.Exito -> {
                if (actual.datos.isEmpty()) {
                    item {
                        Text(
                            text = if (busqueda.isBlank()) "No hay productos disponibles"
                            else "No se encontraron productos",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(actual.datos, key = { it.id }) { producto ->
                    TarjetaProducto(
                        item = producto,
                        onClick = { onProducto(producto.id) },
                        onAlternarFavorito = if (puedeGuardarFavoritos) {
                            { vm.alternarFavorito(producto.id) }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}
