package com.tecsup.productos.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
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

    private val _recuperacion = MutableStateFlow<UiState<Unit>?>(null)
    val recuperacion = _recuperacion.asStateFlow()

    private val _registro = MutableStateFlow<UiState<Unit>?>(null)
    val registro = _registro.asStateFlow()

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

    fun limpiarRegistro() {
        if (_registro.value !is UiState.Cargando) _registro.value = null
    }

    fun registrar(nombre: String, correo: String, clave: String, confirmacion: String) {
        if (_registro.value is UiState.Cargando ||
            _estado.value is UiState.Cargando ||
            _recuperacion.value is UiState.Cargando
        ) return

        val nombreLimpio = nombre.trim()
        val correoLimpio = correo.trim()
        val error = when {
            nombreLimpio.isBlank() -> "Ingresa tu nombre."
            nombreLimpio.length > 100 -> "El nombre puede tener hasta 100 caracteres."
            !Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches() -> "Ingresa un correo válido."
            clave.isBlank() -> "Ingresa una contraseña."
            clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            clave != confirmacion -> "Las contraseñas no coinciden."
            else -> null
        }
        if (error != null) {
            _registro.value = UiState.Error(error)
            return
        }

        _registro.value = UiState.Cargando
        viewModelScope.launch {
            authRepository.registrar(nombreLimpio, correoLimpio, clave).fold(
                onSuccess = { sesion ->
                    _recuperacion.value = null
                    _estado.value = UiState.Exito(sesion)
                    _registro.value = null
                },
                onFailure = { _registro.value = UiState.Error(mensajeRegistro(it)) },
            )
        }
    }

    private fun mensajeRegistro(error: Throwable): String = when (error) {
        is FirebaseAuthWeakPasswordException -> "La contraseña no cumple los requisitos de seguridad."
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthUserCollisionException -> "No se pudo registrar la cuenta. Si ya tienes una, inicia sesión."
        is FirebaseNetworkException -> "Revisa tu conexión e inténtalo de nuevo."
        is FirebaseFirestoreException -> when (error.code) {
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> "Ya tienes una cuenta. Inicia sesión."
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "El registro de perfiles aún no está habilitado."
            else -> "No se pudo completar el perfil. Reintenta con los mismos datos."
        }
        else -> "No se pudo completar el registro. Inténtalo de nuevo."
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _estado.value = UiState.Exito(null)
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is FirebaseNetworkException -> "No pudimos conectarnos. Revisa tu conexión e inténtalo de nuevo."
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> "El correo o la contraseña son incorrectos."
        is IllegalStateException -> "Tu cuenta aún no tiene acceso. Comunícate con el administrador."
        is FirebaseFirestoreException -> "No pudimos cargar tu perfil. Revisa tu conexión o comunícate con el administrador."
        else -> "No se pudo iniciar sesión. Revisa tu correo, contraseña y conexión."
    }
}
