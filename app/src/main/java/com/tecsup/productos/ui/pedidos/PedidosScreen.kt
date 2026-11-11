package com.tecsup.productos.ui.pedidos

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.domain.model.EstadoPedido
import com.tecsup.productos.domain.model.Pedido
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import java.text.DateFormat
import java.util.Date

@Composable
fun PedidosScreen(vm: PedidosViewModel, esAdmin: Boolean, onVolver: () -> Unit, onPedido: (String) -> Unit) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val actualizando by vm.actualizando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val filtro by vm.filtro.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        if (esAdmin) TextButton(onClick = onVolver) { Text("Volver al panel") }
        Text(
            text = if (esAdmin) "Pedidos de clientes" else "Mis pedidos",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
        if (esAdmin) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = filtro == null, onClick = { vm.filtrar(null) }, label = { Text("Todos") })
                EstadoPedido.entries.forEach { opcion ->
                    FilterChip(
                        selected = filtro == opcion,
                        onClick = { vm.filtrar(opcion) },
                        label = { Text(opcion.etiqueta) },
                    )
                }
            }
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 20.dp))
        }
        when (val actual = estado) {
            UiState.Cargando -> Cargando()
            is UiState.Error -> ErrorView(actual.mensaje, vm::reintentar)
            is UiState.Exito -> LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (actual.datos.isEmpty()) {
                    item {
                        Text(
                            when {
                                filtro != null -> "No hay pedidos con ese estado."
                                esAdmin -> "Aún no hay pedidos de clientes."
                                else -> "Aún no tienes pedidos."
                            }
                        )
                    }
                }
                items(actual.datos, key = { it.id }) { pedido ->
                    OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ) {
                                Text(
                                    pedido.estado.etiqueta,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                )
                            }
                            Text("Pedido #${pedido.numero}", style = MaterialTheme.typography.titleMedium)
                            Text(pedido.lineas.first().producto)
                            if (pedido.lineas.size > 1) Text("+ ${pedido.lineas.size - 1} productos")
                            Text("Unidades · ${pedido.cantidad}")
                            if (esAdmin) Text(pedido.correo)
                            Text(formatoImporte(pedido.totalCentavos), style = MaterialTheme.typography.titleMedium)
                            if (pedido.fecha > 0) {
                                Text(
                                    formatoFecha(pedido.fecha),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { onPedido(pedido.id) }) { Text("Ver detalle") }
                            if (esAdmin) AccionPedido(pedido, actualizando, vm::avanzar)
                        }
                    }
                }
            }
        }
    }
}


@Composable
internal fun AccionPedido(pedido: Pedido, actualizando: String?, onAvanzar: (Pedido) -> Unit) {
    val accion = when (pedido.estado) {
        EstadoPedido.PENDIENTE -> "Confirmar"
        EstadoPedido.CONFIRMADO -> "Marcar como entregado"
        EstadoPedido.ENTREGADO -> return
    }
    Button(onClick = { onAvanzar(pedido) }, enabled = actualizando == null) {
        Text(if (actualizando == pedido.id) "Actualizando…" else accion)
    }
}

internal fun formatoFecha(fecha: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(fecha))
