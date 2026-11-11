package com.tecsup.productos.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {
    @Query("SELECT * FROM carrito WHERE uid = :uid ORDER BY productoId")
    fun observar(uid: String): Flow<List<CarritoEntity>>

    @Query("SELECT * FROM carrito WHERE uid = :uid ORDER BY productoId")
    suspend fun leer(uid: String): List<CarritoEntity>

    @Upsert
    suspend fun guardar(linea: CarritoEntity)

    @Query("DELETE FROM carrito WHERE uid = :uid AND productoId = :productoId")
    suspend fun quitar(uid: String, productoId: Int)

    @Query("UPDATE carrito SET solicitudId = :solicitudId WHERE uid = :uid")
    suspend fun reservar(uid: String, solicitudId: String)

    @Query("UPDATE carrito SET solicitudId = NULL WHERE uid = :uid AND solicitudId = :solicitudId")
    suspend fun liberar(uid: String, solicitudId: String)

    @Query("DELETE FROM carrito WHERE uid = :uid AND solicitudId = :solicitudId")
    suspend fun vaciarEnviado(uid: String, solicitudId: String)
}
