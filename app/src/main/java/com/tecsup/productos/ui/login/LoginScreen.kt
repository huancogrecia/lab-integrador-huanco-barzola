package com.tecsup.productos.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    estado: UiState<Sesion?>,
    modifier: Modifier = Modifier,
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val error = (estado as? UiState.Error)?.mensaje
    val cargando = estado is UiState.Cargando
    val recuperacion by vm.recuperacion.collectAsStateWithLifecycle()
    val enviando = recuperacion is UiState.Cargando

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Productos", style = MaterialTheme.typography.headlineLarge)
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.semantics { contentDescription = description }
                ) {
                    Text(
                        text = if (passwordVisible) "Ocultar" else "Ver",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { vm.iniciarSesion(correo, clave) },
            enabled = !cargando && !enviando,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ingresando...")
            } else {
                Text("Ingresar")
            }
        }
        TextButton(
            onClick = { vm.recuperarContrasena(correo) },
            enabled = !cargando && !enviando,
        ) {
            Text("Olvidé mi contraseña")
        }
        when (val solicitud = recuperacion) {
            UiState.Cargando -> Text("Solicitando enlace…")
            is UiState.Error -> Text(solicitud.mensaje, color = MaterialTheme.colorScheme.error)
            is UiState.Exito -> Text("Si el correo está registrado, recibirás un enlace para cambiar tu contraseña.")
            null -> Unit
        }
    }
}
