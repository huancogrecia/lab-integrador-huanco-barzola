package com.tecsup.productos.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("""
        SELECT items.*, favoritos.productoId IS NOT NULL AS esFavorito
        FROM items LEFT JOIN favoritos ON favoritos.productoId = items.id AND favoritos.uid = :uid
        ORDER BY items.titulo
    """)
    fun todos(uid: String): Flow<List<ItemConFavorito>>

    @Query("""
        SELECT items.*, 1 AS esFavorito FROM items
        INNER JOIN favoritos ON favoritos.productoId = items.id
        WHERE favoritos.uid = :uid ORDER BY items.titulo
    """)
    fun favoritos(uid: String): Flow<List<ItemConFavorito>>

    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE uid = :uid AND productoId = :id)")
    suspend fun esFavorito(uid: String, id: Int): Boolean

    @Query("SELECT COUNT(*) FROM items")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM favoritos INNER JOIN items ON items.id = favoritos.productoId WHERE favoritos.uid = :uid")
    suspend fun contarFavoritos(uid: String): Int

    @Upsert
    suspend fun guardarTodos(items: List<ItemEntity>)

    @Query("DELETE FROM items WHERE id NOT IN (:ids)")
    suspend fun borrarFueraDeLista(ids: List<Int>)

    @Transaction
    suspend fun sincronizar(items: List<ItemEntity>) {
        if (items.isEmpty()) {
            borrarTodo()
        } else {
            guardarTodos(items)
            borrarFueraDeLista(items.map(ItemEntity::id))
        }
    }

    @Query("DELETE FROM favoritos WHERE uid = :uid AND productoId = :id")
    suspend fun quitarFavorito(uid: String, id: Int): Int

    @Query("INSERT OR IGNORE INTO favoritos (uid, productoId) SELECT :uid, id FROM items WHERE id = :id")
    suspend fun guardarFavorito(uid: String, id: Int)

    @Transaction
    suspend fun alternarFavorito(uid: String, id: Int) {
        if (quitarFavorito(uid, id) == 0) guardarFavorito(uid, id)
    }

    @Query("DELETE FROM items")
    suspend fun borrarTodo()
}
