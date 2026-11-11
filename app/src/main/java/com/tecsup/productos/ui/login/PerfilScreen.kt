package com.tecsup.productos.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.ui.UiState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion

@Composable
fun PerfilScreen(
    sesion: Sesion,
    vm: PerfilViewModel,
    onCerrarSesion: () -> Unit,
    onVerFavoritos: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val modoOscuro by vm.modoOscuro.collectAsStateWithLifecycle()
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    val formaTarjeta = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineLarge)
        OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = formaTarjeta) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconoPerfil { Icon(Icons.Filled.Person, contentDescription = null) }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        Text(
                            if (sesion.rol == Rol.ADMIN) "Administrador" else "Usuario",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    Text(sesion.correo, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        onVerFavoritos?.let { verFavoritos ->
            OutlinedCard(onClick = verFavoritos, modifier = Modifier.fillMaxWidth(), shape = formaTarjeta) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconoPerfil { Icon(Icons.Filled.FavoriteBorder, contentDescription = null) }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Favoritos guardados", style = MaterialTheme.typography.titleMedium)
                        when (val actual = estado) {
                            UiState.Cargando -> CircularProgressIndicator()
                            is UiState.Error -> {
                                Text(actual.mensaje, color = MaterialTheme.colorScheme.error)
                                TextButton(onClick = vm::observar) { Text("Reintentar") }
                            }
                            is UiState.Exito -> Text(
                                text = actual.datos.toString(),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text("Ver favoritos", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = formaTarjeta) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Modo oscuro", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = modoOscuro,
                    onCheckedChange = vm::actualizarModoOscuro,
                )
            }
        }
        OutlinedButton(onClick = { confirmarSalida = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
    }
    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Quieres cerrar sesión?") },
            text = { Text("Podrás volver a ingresar con tu correo y contraseña.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    onCerrarSesion()
                }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun IconoPerfil(contenido: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(14.dp),
    ) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { contenido() }
    }
}
