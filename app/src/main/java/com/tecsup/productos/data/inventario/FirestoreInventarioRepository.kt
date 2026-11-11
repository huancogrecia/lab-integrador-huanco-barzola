package com.tecsup.productos.data.inventario

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreInventarioRepository(db: FirebaseFirestore) : InventarioRepository {
    private val catalogo = db.collection("inventario").document("catalogo")

    override fun observar(): Flow<Map<Int, Int>> = callbackFlow {
        val registro = catalogo.addSnapshotListener(MetadataChanges.INCLUDE) { dato, error ->
            when {
                error != null -> close(error)
                dato != null && (dato.exists() || !dato.metadata.isFromCache) -> {
                    trySend(dato.existencias().mapKeys { (id, _) -> id.toInt() })
                }
            }
        }
        awaitClose { registro.remove() }
    }

    override suspend fun guardar(productoId: Int, unidades: Int, unidadesEsperadas: Int?) {
        require(productoId > 0 && unidades in 0..9999)
        val id = productoId.toString()
        val actualizado = catalogo.firestore.runTransaction { transaccion ->
            val actual = transaccion.get(catalogo).existencias()[id]
            if (actual != unidadesEsperadas) return@runTransaction false
            transaccion.set(catalogo, mapOf("existencias" to mapOf(id to unidades)), SetOptions.merge())
            true
        }.await()
        if (!actualizado) throw StockModificado()
    }
}

internal fun DocumentSnapshot.existencias(): Map<String, Int> =
    (get("existencias") as? Map<*, *>).orEmpty().mapNotNull { (clave, valor) ->
        val id = clave as? String ?: return@mapNotNull null
        val unidades = valor as? Long ?: return@mapNotNull null
        if (id.toIntOrNull()?.let { it > 0 } != true || unidades !in 0L..9999L) return@mapNotNull null
        id to unidades.toInt()
    }.toMap()
