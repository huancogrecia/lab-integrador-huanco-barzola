package com.tecsup.productos.ui.pedidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.domain.model.EstadoPedido
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView

@Composable
fun DetallePedidoScreen(
    vm: PedidosViewModel,
    id: String,
    esAdmin: Boolean,
    recienCreado: Boolean,
    onVolver: () -> Unit,
) {
    val detalle = remember(vm, id) { vm.observarDetalle(id) }
    val estado by detalle.collectAsStateWithLifecycle(initialValue = UiState.Cargando)
    val actualizando by vm.actualizando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        TextButton(onClick = onVolver, modifier = Modifier.padding(horizontal = 12.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text("Volver a pedidos", Modifier.padding(start = 8.dp))
        }
        when (val actual = estado) {
            UiState.Cargando -> Cargando()
            is UiState.Error -> ErrorView(actual.mensaje, vm::reintentar)
            is UiState.Exito -> {
                val pedido = actual.datos
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (recienCreado) Text("Pedido realizado", color = MaterialTheme.colorScheme.primary)
                            Text("Pedido #${pedido.numero}", style = MaterialTheme.typography.headlineLarge)
                            if (esAdmin) Text(pedido.correo)
                            if (pedido.fecha > 0) Text(formatoFecha(pedido.fecha))
                        }
                    }
                    item {
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Seguimiento", style = MaterialTheme.typography.titleMedium)
                                EstadoPedido.entries.forEach { paso ->
                                    val marca = when {
                                        paso == pedido.estado -> "●"
                                        paso.ordinal < pedido.estado.ordinal -> "✓"
                                        else -> "○"
                                    }
                                    Text(
                                        "$marca ${paso.etiqueta}",
                                        fontWeight = if (paso == pedido.estado) FontWeight.Bold else FontWeight.Normal,
                                        color = if (paso == pedido.estado) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                    items(pedido.lineas) { linea ->
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(linea.producto, style = MaterialTheme.typography.titleMedium)
                                Text("Unidades · ${linea.cantidad}")
                                linea.precioCentavos?.let { Text("${formatoImporte(it)} por unidad") }
                                linea.subtotalCentavos?.let { Text("Subtotal · ${formatoImporte(it)}") }
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total (USD)", style = MaterialTheme.typography.titleMedium)
                            Text(formatoImporte(pedido.totalCentavos), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    if (esAdmin) {
                        item {
                            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            AccionPedido(pedido, actualizando, vm::avanzar)
                        }
                    }
                }
            }
        }
    }
}
