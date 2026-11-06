package com.tecsup.productos.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tecsup.productos.R
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView

@Composable
fun DetalleScreen(vm: DetalleViewModel, onVolver: () -> Unit, modifier: Modifier = Modifier) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onVolver) {
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
                    Text("Detalle del producto", style = MaterialTheme.typography.headlineMedium)
                    AsyncImage(
                        model = item.imagenUrl,
                        contentDescription = item.titulo,
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(R.drawable.ic_laptop),
                        error = painterResource(R.drawable.ic_laptop),
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    )
                    Text(item.titulo, style = MaterialTheme.typography.titleLarge)
                    Text(item.subtitulo, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
