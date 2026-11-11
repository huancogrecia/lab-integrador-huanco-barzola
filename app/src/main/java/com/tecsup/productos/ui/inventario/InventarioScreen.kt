package com.tecsup.productos.ui.inventario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView

@Composable
fun InventarioScreen(vm: InventarioViewModel, onVolver: () -> Unit) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val guardado by vm.guardado.collectAsStateWithLifecycle()
    var idEditado by rememberSaveable { mutableStateOf<Int?>(null) }
    var unidadesEsperadas by rememberSaveable { mutableStateOf<Int?>(null) }
    var unidades by rememberSaveable { mutableStateOf("") }
    val guardando = guardado is UiState.Cargando

    LaunchedEffect(guardado) {
        if (guardado is UiState.Exito) {
            idEditado = null
            vm.limpiarGuardado()
        }
    }

    Column(Modifier.fillMaxSize()) {
        TextButton(onClick = onVolver, enabled = !guardando) { Text("Volver al panel") }
        Text("Inventario", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(20.dp))
        when (val actual = estado) {
            UiState.Cargando -> Cargando()
            is UiState.Error -> ErrorView(actual.mensaje, vm::reintentar)
            is UiState.Exito -> {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (actual.datos.isEmpty()) {
                        item { Text("Sincroniza el catálogo desde el panel para ver los productos.") }
                    }
                    items(actual.datos, key = { it.id }) { producto ->
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    when (val cantidad = producto.unidades) {
                                        null -> "Stock sin configurar"
                                        0 -> "Agotado"
                                        1 -> "1 unidad disponible"
                                        else -> "$cantidad unidades disponibles"
                                    }
                                )
                                TextButton(onClick = {
                                    vm.limpiarGuardado()
                                    idEditado = producto.id
                                    unidadesEsperadas = producto.unidades
                                    unidades = producto.unidades?.toString().orEmpty()
                                }) { Text("Ajustar stock") }
                            }
                        }
                    }
                }
                actual.datos.firstOrNull { it.id == idEditado }?.let { producto ->
                    AlertDialog(
                        onDismissRequest = { if (!guardando) idEditado = null },
                        title = { Text("Ajustar stock") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(producto.nombre)
                                OutlinedTextField(
                                    value = unidades,
                                    onValueChange = { unidades = it },
                                    enabled = !guardando,
                                    label = { Text("Unidades disponibles") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                )
                                (guardado as? UiState.Error)?.let {
                                    Text(it.mensaje, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { vm.guardar(producto.id, unidades, unidadesEsperadas) }, enabled = !guardando) {
                                Text(if (guardando) "Guardando…" else "Guardar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { idEditado = null }, enabled = !guardando) { Text("Cancelar") }
                        },
                    )
                }
            }
        }
    }
}
