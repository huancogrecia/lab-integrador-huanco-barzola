package com.tecsup.productos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.auth.AuthRepository
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<Sesion?>>(UiState.Cargando)
    val estado = _estado.asStateFlow()

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

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _estado.value = UiState.Exito(null)
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IllegalStateException -> error.message ?: "El perfil de Firestore no está configurado."
        else -> "No se pudo iniciar sesión. Verifica las credenciales y la conexión."
    }
}
