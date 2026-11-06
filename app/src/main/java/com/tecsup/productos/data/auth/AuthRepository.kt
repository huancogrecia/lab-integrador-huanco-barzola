package com.tecsup.productos.data.auth

import com.tecsup.productos.domain.model.Sesion

interface AuthRepository {
    suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion>
    suspend fun sesionActual(): Sesion?
    fun cerrarSesion()
}
