package com.tecsup.productos

import android.content.Context
import com.tecsup.productos.data.auth.AuthRepository
import com.tecsup.productos.data.auth.FirebaseAuthRepository
import com.tecsup.productos.data.local.AppDatabase
import com.tecsup.productos.data.preferences.PreferenciasRepository
import com.tecsup.productos.data.remote.RetrofitClient
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.data.repository.ItemRepositoryImpl
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.notification.NotificadorAndroid

class AppContainer(context: Context) {
    private val itemDao = AppDatabase.obtener(context.applicationContext).itemDao()

    val itemRepository: ItemRepository = ItemRepositoryImpl(
        api = RetrofitClient.apiService,
        dao = itemDao,
    )
    val authRepository: AuthRepository = FirebaseAuthRepository()
    val notificador: Notificador = NotificadorAndroid(context.applicationContext)
    val preferenciasRepository: PreferenciasRepository =
        PreferenciasRepository(context.applicationContext)
}
