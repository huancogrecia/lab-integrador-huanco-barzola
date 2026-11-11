package com.tecsup.productos.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tecsup.productos.ui.UiState

@Composable
fun RegistroScreen(
    estado: UiState<Unit>?,
    onRegistrar: (String, String, String, String) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    val enviando = estado is UiState.Cargando
    val formaCampo = RoundedCornerShape(14.dp)

    BackHandler { if (!enviando) onVolver() }
    Column(
        modifier = modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TextButton(onClick = onVolver, enabled = !enviando) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text("Volver al inicio de sesión", modifier = Modifier.padding(start = 8.dp))
        }
        Text("Crear cuenta", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Crea una cuenta para explorar el catálogo y guardar favoritos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            enabled = !enviando,
            singleLine = true,
            shape = formaCampo,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !enviando,
            singleLine = true,
            shape = formaCampo,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            supportingText = { Text("Al menos 6 caracteres") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !enviando,
            singleLine = true,
            shape = formaCampo,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = confirmacion,
            onValueChange = { confirmacion = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !enviando,
            singleLine = true,
            shape = formaCampo,
            modifier = Modifier.fillMaxWidth(),
        )
        if (estado is UiState.Error) {
            Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { onRegistrar(nombre, correo, clave, confirmacion) },
            enabled = !enviando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(if (enviando) "Creando cuenta…" else "Crear cuenta")
        }
    }
}
