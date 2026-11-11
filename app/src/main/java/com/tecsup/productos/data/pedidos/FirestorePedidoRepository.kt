package com.tecsup.productos.data.pedidos

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.tecsup.productos.data.inventario.existencias
import com.tecsup.productos.domain.model.EstadoPedido
import com.tecsup.productos.domain.model.LineaPedido
import com.tecsup.productos.domain.model.Pedido
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestorePedidoRepository(db: FirebaseFirestore) : PedidoRepository {
    private val pedidos = db.collection("pedidos")
    private val inventario = db.collection("inventario").document("catalogo")

    override fun observar(sesion: Sesion): Flow<List<Pedido>> = callbackFlow {
        val consulta = if (sesion.rol == Rol.ADMIN) pedidos else pedidos.whereEqualTo("uid", sesion.uid)
        val registro = consulta.addSnapshotListener { datos, error ->
            if (error != null) {
                close(error)
            } else if (datos != null) {
                runCatching { datos.documents.map { it.toPedido() }.sortedByDescending(Pedido::fecha) }
                    .onSuccess { trySend(it) }
                    .onFailure { close(it) }
            }
        }
        awaitClose { registro.remove() }
    }

    override fun observarUno(id: String): Flow<Pedido?> = callbackFlow {
        val registro = pedidos.document(id).addSnapshotListener { dato, error ->
            if (error != null) {
                close(error)
            } else if (dato != null) {
                if (dato.exists()) {
                    runCatching { dato.toPedido() }.onSuccess { trySend(it) }.onFailure { close(it) }
                } else {
                    trySend(null)
                }
            }
        }
        awaitClose { registro.remove() }
    }

    override suspend fun crear(sesion: Sesion, solicitudId: String, lineas: List<LineaPedido>): String {
        require(sesion.rol == Rol.USUARIO)
        require(lineas.size in 1..Pedido.MAX_PRODUCTOS)
        require(lineas.distinctBy(LineaPedido::productoId).size == lineas.size)
        lineas.forEach(LineaPedido::validar)
        val productos = lineas.map { linea ->
            mapOf(
                "productoId" to linea.productoId.toLong(),
                "producto" to linea.producto,
                "cantidad" to linea.cantidad.toLong(),
                "precioCentavos" to requireNotNull(linea.precioCentavos),
            )
        }
        val total = lineas.sumOf { requireNotNull(it.subtotalCentavos) }
        val referencia = pedidos.document("${sesion.uid}:$solicitudId")
        try {
            val faltante = pedidos.firestore.runTransaction { transaccion ->
                val anterior = transaccion.get(referencia)
                if (anterior.exists()) {
                    check(anterior.get("lineas") == productos && anterior.getLong("totalCentavos") == total)
                } else {
                    val existencias = transaccion.get(inventario).existencias()
                    productoSinStock(lineas, existencias)?.let { return@runTransaction it }
                    transaccion.set(
                        referencia,
                        mapOf(
                            "uid" to sesion.uid,
                            "correo" to sesion.correo,
                            "lineas" to productos,
                            "totalCentavos" to total,
                            "estado" to EstadoPedido.PENDIENTE.name,
                            "fecha" to FieldValue.serverTimestamp(),
                        ),
                    )
                }
                null
            }.await()
            if (faltante != null) throw StockInsuficiente(faltante)
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) throw PedidoSinPermiso(e)
            throw e
        }
        return referencia.id
    }

    override suspend fun avanzar(id: String, estadoActual: EstadoPedido) {
        val siguiente = requireNotNull(estadoActual.siguiente())
        val referencia = pedidos.document(id)
        val faltante = pedidos.firestore.runTransaction { transaccion ->
            val documento = transaccion.get(referencia)
            if (documento.getString("estado") != estadoActual.name) return@runTransaction null

            if (estadoActual == EstadoPedido.PENDIENTE) {
                val lineas = documento.toPedido().lineas
                val existencias = transaccion.get(inventario).existencias()
                productoSinStock(lineas, existencias)?.let { return@runTransaction it }
                val saldos = lineas.associate { linea ->
                    val id = linea.productoId.toString()
                    id to (existencias.getValue(id) - linea.cantidad)
                }
                transaccion.update(inventario, "existencias", existencias + saldos)
            }
            transaccion.update(referencia, "estado", siguiente.name)
            null
        }.await()
        if (faltante != null) throw StockInsuficiente(faltante)
    }

    private fun productoSinStock(lineas: List<LineaPedido>, existencias: Map<String, Int>): String? =
        lineas.firstOrNull { it.cantidad > (existencias[it.productoId.toString()] ?: 0) }?.producto

    private fun DocumentSnapshot.toPedido(): Pedido {
        val lineas = (get("lineas") as? List<*>)?.map { dato ->
            val linea = dato as Map<*, *>
            LineaPedido(
                productoId = (linea["productoId"] as Long).toInt(),
                producto = linea["producto"] as String,
                cantidad = (linea["cantidad"] as Long).toInt(),
                precioCentavos = linea["precioCentavos"] as Long,
            )
        } ?: listOf(
            LineaPedido(
                productoId = requireNotNull(getLong("productoId")).toInt(),
                producto = requireNotNull(getString("producto")),
                cantidad = requireNotNull(getLong("cantidad")).toInt(),
                precioCentavos = null,
            ),
        )
        return Pedido(
            id = id,
            correo = requireNotNull(getString("correo")),
            lineas = lineas,
            totalCentavos = getLong("totalCentavos"),
            estado = EstadoPedido.valueOf(requireNotNull(getString("estado"))),
            fecha = getTimestamp("fecha")?.toDate()?.time ?: 0L,
        )
    }
}
