package com.tecsup.productos.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY titulo")
    fun todos(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE esFavorito = 1 ORDER BY titulo")
    fun favoritos(): Flow<List<ItemEntity>>

    @Query("SELECT id FROM items WHERE esFavorito = 1")
    suspend fun idsFavoritos(): List<Int>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM items WHERE esFavorito = 1")
    suspend fun contarFavoritos(): Int

    @Upsert
    suspend fun guardarTodos(items: List<ItemEntity>)

    @Query("DELETE FROM items WHERE id NOT IN (:ids)")
    suspend fun borrarFueraDeLista(ids: List<Int>)

    @Transaction
    suspend fun sincronizar(items: List<ItemEntity>) {
        val idsFavoritos = idsFavoritos().toSet()
        val sincronizados = items.map { item ->
            item.copy(esFavorito = item.id in idsFavoritos)
        }
        if (sincronizados.isEmpty()) {
            borrarTodo()
        } else {
            guardarTodos(sincronizados)
            borrarFueraDeLista(sincronizados.map(ItemEntity::id))
        }
    }

    @Query("UPDATE items SET esFavorito = NOT esFavorito WHERE id = :id")
    suspend fun alternarFavorito(id: Int)

    @Query("DELETE FROM items")
    suspend fun borrarTodo()
}
