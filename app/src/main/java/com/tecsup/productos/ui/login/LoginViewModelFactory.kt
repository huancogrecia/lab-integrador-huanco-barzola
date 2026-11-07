package com.tecsup.productos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.productos.AppContainer

class LoginViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return modelClass.cast(LoginViewModel(container.authRepository))
                ?: throw IllegalArgumentException("No se pudo crear LoginViewModel")
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
