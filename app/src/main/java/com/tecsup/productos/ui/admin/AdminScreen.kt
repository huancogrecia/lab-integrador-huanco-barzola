package com.tecsup.productos.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando

@Composable
fun AdminScreen(
    vm: AdminViewModel,
    onVerPedidos: () -> Unit,
    onVerInventario: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    var confirmarLimpieza by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Panel de administración", style = MaterialTheme.typography.headlineMedium)
        when (val actual = estado) {
            UiState.Cargando -> Cargando()
            is UiState.Error -> {
                Text(actual.mensaje, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = vm::observarProductos) { Text("Reintentar") }
            }
            is UiState.Exito -> Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Productos guardados", style = MaterialTheme.typography.bodyMedium)
                    Text("${actual.datos}", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
        Button(
            onClick = vm::sincronizar,
            enabled = !ocupado,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sincronizar")
        }
        OutlinedButton(
            onClick = { confirmarLimpieza = true },
            enabled = !ocupado,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Limpiar caché")
        }
        Button(onClick = onVerPedidos, modifier = Modifier.fillMaxWidth()) {
            Text("Ver pedidos de clientes")
        }
        Button(onClick = onVerInventario, modifier = Modifier.fillMaxWidth()) {
            Text("Gestionar stock")
        }
        if (ocupado) CircularProgressIndicator()
        if (mensaje != null) Text(mensaje.orEmpty(), style = MaterialTheme.typography.bodyMedium)
    }

    if (confirmarLimpieza) {
        AlertDialog(
            onDismissRequest = { confirmarLimpieza = false },
            title = { Text("¿Limpiar caché?") },
            text = { Text("Se eliminarán los productos guardados en este dispositivo.") },
            confirmButton = {
                Button(onClick = {
                    confirmarLimpieza = false
                    vm.limpiarCache()
                }) { Text("Limpiar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpieza = false }) { Text("Cancelar") }
            },
        )
    }
}
