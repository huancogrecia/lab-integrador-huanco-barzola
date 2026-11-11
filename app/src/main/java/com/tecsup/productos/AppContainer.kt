package com.tecsup.productos

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.productos.data.auth.AuthRepository
import com.tecsup.productos.data.auth.FirebaseAuthRepository
import com.tecsup.productos.data.carrito.CarritoRepository
import com.tecsup.productos.data.carrito.CarritoRepositoryRoom
import com.tecsup.productos.data.local.AppDatabase
import com.tecsup.productos.data.inventario.FirestoreInventarioRepository
import com.tecsup.productos.data.inventario.InventarioRepository
import com.tecsup.productos.data.pedidos.FirestorePedidoRepository
import com.tecsup.productos.data.pedidos.PedidoRepository
import com.tecsup.productos.data.preferences.PreferenciasRepository
import com.tecsup.productos.data.remote.RetrofitClient
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.data.repository.ItemRepositoryImpl
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.notification.NotificadorAndroid

class AppContainer(context: Context) {
    private val db = AppDatabase.obtener(context.applicationContext)
    private val itemDao = db.itemDao()

    fun carritoRepository(uid: String): CarritoRepository = CarritoRepositoryRoom(db, uid)

    fun itemRepository(uid: String): ItemRepository = ItemRepositoryImpl(
        api = RetrofitClient.apiService,
        dao = itemDao,
        uid = uid,
    )
    private val firestore = FirebaseFirestore.getInstance()
    val pedidoRepository: PedidoRepository = FirestorePedidoRepository(firestore)
    val inventarioRepository: InventarioRepository = FirestoreInventarioRepository(firestore)
    val authRepository: AuthRepository = FirebaseAuthRepository()
    val notificador: Notificador = NotificadorAndroid(context.applicationContext)
    val preferenciasRepository: PreferenciasRepository =
        PreferenciasRepository(context.applicationContext)
}
