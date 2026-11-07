package com.tecsup.productos.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AuthRepository {
    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> = try {
        val user = auth.signInWithEmailAndPassword(correo, clave).await().user
            ?: throw IllegalStateException("Firebase no devolvió un usuario autenticado.")
        Result.success(leerSesion(user.uid, user.email ?: correo))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        auth.signOut()
        Result.failure(e)
    }

    override suspend fun recuperarContrasena(correo: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(correo).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (_: FirebaseAuthInvalidUserException) {
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sesionActual(): Sesion? {
        val user = auth.currentUser ?: return null
        return leerSesion(user.uid, user.email.orEmpty())
    }

    override fun cerrarSesion() {
        auth.signOut()
    }

    private suspend fun leerSesion(uid: String, correo: String): Sesion {
        val documento = db.collection("usuarios").document(uid).get().await()
        check(documento.exists()) {
            "No existe el perfil de este usuario en Firestore."
        }
        val rol = when (documento.getString("rol")?.trim()?.lowercase(Locale.ROOT)) {
            "admin" -> Rol.ADMIN
            "usuario" -> Rol.USUARIO
            else -> throw IllegalStateException(
                "El perfil de Firestore debe tener rol admin o usuario."
            )
        }
        return Sesion(uid = uid, correo = correo, rol = rol)
    }
}
