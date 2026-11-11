package com.tecsup.productos.data.auth

import com.tecsup.productos.domain.model.Sesion

interface AuthRepository {
    suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion>
    suspend fun registrar(nombre: String, correo: String, clave: String): Result<Sesion>
    suspend fun recuperarContrasena(correo: String): Result<Unit>
    suspend fun sesionActual(): Sesion?
    fun cerrarSesion()
}
