package com.tecsup.productos.ui.detalle

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Pedido
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.MarcaYPrecio
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DetalleScreen(
    vm: DetalleViewModel,
    onVolver: () -> Unit,
    onVerCarrito: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val solicitud by vm.solicitud.collectAsStateWithLifecycle()
    val stock by vm.stock.collectAsStateWithLifecycle()
    var cantidad by rememberSaveable { mutableIntStateOf(1) }
    val enviando = solicitud is UiState.Cargando
    val disponibles = (stock as? UiState.Exito)?.datos
    val limite = minOf(disponibles ?: 0, Pedido.CANTIDAD_MAXIMA)

    LaunchedEffect(limite) {
        if (limite > 0) cantidad = cantidad.coerceAtMost(limite)
    }

    BackHandler(enabled = enviando) {}
    LaunchedEffect(solicitud) {
        if (solicitud is UiState.Exito) {
            vm.limpiarSolicitud()
            onVerCarrito()
        }
    }
    Column(modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onVolver, enabled = !enviando) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text("Volver al catálogo", modifier = Modifier.padding(start = 8.dp))
        }
        when (val actual = estado) {
            UiState.Cargando -> Cargando()
            is UiState.Error -> ErrorView(actual.mensaje, vm::cargar)
            is UiState.Exito -> {
                val item = actual.datos
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Detalle del producto", style = MaterialTheme.typography.headlineLarge)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        AsyncImage(
                            model = item.imagenUrl,
                            contentDescription = item.titulo,
                            contentScale = ContentScale.Fit,
                            placeholder = painterResource(R.drawable.ic_laptop),
                            error = painterResource(R.drawable.ic_laptop),
                            fallback = painterResource(R.drawable.ic_laptop),
                            modifier = Modifier.fillMaxWidth().height(280.dp).padding(16.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.titulo, style = MaterialTheme.typography.headlineMedium)
                        MarcaYPrecio(item.subtitulo, MaterialTheme.typography.headlineLarge)
                    }
                    when (val actualStock = stock) {
                        UiState.Cargando -> Text("Consultando disponibilidad…")
                        is UiState.Error -> {
                            Text(actualStock.mensaje, color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = vm::reintentarStock) { Text("Reintentar") }
                        }
                        is UiState.Exito -> Text(
                            when (actualStock.datos) {
                                0 -> "Agotado"
                                1 -> "1 unidad disponible"
                                else -> "${actualStock.datos} unidades disponibles"
                            }
                        )
                    }
                    if (vm.puedePedir && limite > 0) {
                        val precio = item.precioUnitario
                        if (precio == null) {
                            Text("Precio no disponible.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val formato = remember { NumberFormat.getCurrencyInstance(Locale.US) }
                            val total = vm.subtotal(cantidad)
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Cantidad", style = MaterialTheme.typography.titleMedium)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            TextButton(
                                                onClick = { cantidad-- },
                                                enabled = !enviando && cantidad > 1,
                                                modifier = Modifier.semantics { contentDescription = "Disminuir cantidad" },
                                            ) { Text("−") }
                                            Text(cantidad.toString(), style = MaterialTheme.typography.titleMedium)
                                            TextButton(
                                                onClick = { cantidad++ },
                                                enabled = !enviando && cantidad < limite,
                                                modifier = Modifier.semantics { contentDescription = "Aumentar cantidad" },
                                            ) { Text("+") }
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text("Precio unitario")
                                        Text(formato.format(precio))
                                    }
                                    HorizontalDivider()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Total estimado", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            total?.let { formato.format(it) }.orEmpty(),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    (solicitud as? UiState.Error)?.let {
                                        Text(it.mensaje, color = MaterialTheme.colorScheme.error)
                                    }
                                    Button(
                                        onClick = { vm.agregarAlCarrito(cantidad) },
                                        enabled = !enviando && solicitud !is UiState.Exito && cantidad <= limite,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(if (enviando) "Agregando…" else "Agregar al carrito")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
