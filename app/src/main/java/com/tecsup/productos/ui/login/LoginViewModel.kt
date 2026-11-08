package com.tecsup.productos.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.tecsup.productos.data.auth.AuthRepository
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<Sesion?>>(UiState.Cargando)
    val estado = _estado.asStateFlow()

    private val _recuperacion = MutableStateFlow<UiState<Unit>?>(null)
    val recuperacion = _recuperacion.asStateFlow()

    init {
        verificarSesion()
    }

    fun verificarSesion() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            try {
                _estado.value = UiState.Exito(authRepository.sesionActual())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = UiState.Error(mensajeDe(e))
            }
        }
    }

    fun iniciarSesion(correo: String, clave: String) {
        if (_estado.value is UiState.Cargando || _recuperacion.value is UiState.Cargando) return
        _recuperacion.value = null
        if (correo.isBlank() || clave.isBlank()) {
            _estado.value = UiState.Error("Ingresa tu correo y contraseña.")
            return
        }
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            val resultado = authRepository.iniciarSesion(correo.trim(), clave)
            _estado.value = resultado.fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(mensajeDe(it)) },
            )
        }
    }

    fun recuperarContrasena(correo: String) {
        if (_estado.value is UiState.Cargando || _recuperacion.value is UiState.Cargando) return
        val correoLimpio = correo.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            _recuperacion.value = UiState.Error("Ingresa un correo válido para recuperar tu contraseña.")
            return
        }
        _recuperacion.value = UiState.Cargando
        viewModelScope.launch {
            _recuperacion.value = authRepository.recuperarContrasena(correoLimpio).fold(
                onSuccess = { UiState.Exito(Unit) },
                onFailure = { UiState.Error("No se pudo solicitar el enlace. Revisa tu conexión e inténtalo de nuevo.") },
            )
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _estado.value = UiState.Exito(null)
    }

    private fun mensajeDe(error: Throwable): String = when {
        error is FirebaseNetworkException ||
        error is UnknownHostException ||
        error is SocketTimeoutException -> 
            "No hay conexión a internet. Verifica tu red e inténtalo de nuevo."

        error is IllegalStateException ||
        error is FirebaseFirestoreException -> 
            "No se pudo cargar el perfil de usuario. Contacta al administrador."

        error is FirebaseAuthInvalidCredentialsException ||
        error is FirebaseAuthInvalidUserException -> 
            "Correo o contraseña incorrectos."

        else -> 
            "No se pudo iniciar sesión. Verifica tus datos e inténtalo de nuevo."
    }
}
