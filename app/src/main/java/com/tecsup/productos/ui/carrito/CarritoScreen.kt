package com.tecsup.productos.ui.carrito

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.domain.model.Pedido
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.pedidos.formatoImporte

@Composable
fun CarritoScreen(vm: CarritoViewModel, onCatalogo: () -> Unit) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    BackHandler(enabled = ocupado) {}
    Column(Modifier.fillMaxSize()) {
        Text("Carrito", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(20.dp))
        when (val actual = estado) {
            UiState.Cargando -> Cargando()
            is UiState.Error -> ErrorView(actual.mensaje, vm::reintentarCarga)
            is UiState.Exito -> {
                val carrito = actual.datos
                val pendiente = carrito.solicitudId != null
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (carrito.lineas.isEmpty()) {
                        item {
                            Text("Tu carrito está vacío.")
                            TextButton(onClick = onCatalogo) { Text("Explorar catálogo") }
                        }
                    }
                    items(carrito.lineas, key = { it.productoId }) { linea ->
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(linea.producto, style = MaterialTheme.typography.titleMedium)
                                Text("${formatoImporte(linea.precioCentavos)} por unidad")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { vm.cambiarCantidad(linea.productoId, linea.cantidad - 1) },
                                        enabled = !ocupado && !pendiente && linea.cantidad > 1,
                                        modifier = Modifier.semantics { contentDescription = "Disminuir cantidad" },
                                    ) { Text("−") }
                                    Text(linea.cantidad.toString())
                                    TextButton(
                                        onClick = { vm.cambiarCantidad(linea.productoId, linea.cantidad + 1) },
                                        enabled = !ocupado && !pendiente && linea.cantidad < Pedido.CANTIDAD_MAXIMA,
                                        modifier = Modifier.semantics { contentDescription = "Aumentar cantidad" },
                                    ) { Text("+") }
                                }
                                Text("Subtotal · ${formatoImporte(linea.subtotalCentavos)}")
                                TextButton(
                                    onClick = { vm.quitar(linea.productoId) },
                                    enabled = !ocupado && !pendiente,
                                ) { Text("Quitar") }
                            }
                        }
                    }
                }
                if (carrito.lineas.isNotEmpty()) {
                    HorizontalDivider()
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total (USD)", style = MaterialTheme.typography.titleLarge)
                            Text(formatoImporte(carrito.totalCentavos), style = MaterialTheme.typography.titleLarge)
                        }
                        if (pendiente && !ocupado) Text("Reintenta el envío pendiente para completar el pedido.")
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Button(onClick = vm::enviar, enabled = !ocupado, modifier = Modifier.fillMaxWidth()) {
                            if (ocupado && pendiente) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text("Enviando…", Modifier.padding(start = 8.dp))
                            } else {
                                Text(if (pendiente) "Reintentar pedido" else "Hacer pedido")
                            }
                        }
                    }
                }
            }
        }
    }
}
