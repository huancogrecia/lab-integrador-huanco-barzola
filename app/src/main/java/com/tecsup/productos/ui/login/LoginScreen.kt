package com.tecsup.productos.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    estado: UiState<Sesion?>,
    onCrearCuenta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    val error = (estado as? UiState.Error)?.mensaje
    val cargando = estado is UiState.Cargando
    val recuperacion by vm.recuperacion.collectAsStateWithLifecycle()
    val enviando = recuperacion is UiState.Cargando

    Column(
        modifier = modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1.25f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("Productos", style = MaterialTheme.typography.headlineMedium)
                    Text("Catálogo de laptops", style = MaterialTheme.typography.bodyMedium)
                }
                Image(
                    painter = painterResource(R.drawable.laptop_login),
                    contentDescription = null,
                    modifier = Modifier.weight(1f).height(108.dp),
                )
            }
        }
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { vm.iniciarSesion(correo, clave) },
            enabled = !cargando && !enviando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text("Ingresar")
        }
        if (cargando) CircularProgressIndicator()
        TextButton(
            onClick = { vm.recuperarContrasena(correo) },
            enabled = !cargando && !enviando,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Olvidé mi contraseña")
        }
        when (val solicitud = recuperacion) {
            UiState.Cargando -> Text("Solicitando enlace…")
            is UiState.Error -> Text(solicitud.mensaje, color = MaterialTheme.colorScheme.error)
            is UiState.Exito -> Text("Si el correo está registrado, recibirás un enlace para cambiar tu contraseña.")
            null -> Unit
        }
        TextButton(
            onClick = onCrearCuenta,
            enabled = !cargando && !enviando,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("¿No tienes cuenta? Crear cuenta")
        }
    }
}
