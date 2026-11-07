package com.tecsup.productos.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion

@Composable
fun PerfilScreen(
    sesion: Sesion,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium)
        Text(sesion.correo, style = MaterialTheme.typography.titleMedium)
        Text(
            if (sesion.rol == Rol.ADMIN) "Administrador" else "Usuario",
            color = MaterialTheme.colorScheme.primary,
        )
        Button(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
    }
}
