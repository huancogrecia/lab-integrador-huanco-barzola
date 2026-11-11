package com.tecsup.productos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ItemEntity::class, FavoritoEntity::class, CarritoEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun carritoDao(): CarritoDao

    companion object {
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE items_nueva (id INTEGER NOT NULL PRIMARY KEY, titulo TEXT NOT NULL, subtitulo TEXT NOT NULL, imagenUrl TEXT)")
                db.execSQL("INSERT INTO items_nueva SELECT id, titulo, subtitulo, imagenUrl FROM items")
                db.execSQL("DROP TABLE items")
                db.execSQL("ALTER TABLE items_nueva RENAME TO items")
                db.execSQL("CREATE TABLE favoritos (uid TEXT NOT NULL, productoId INTEGER NOT NULL, PRIMARY KEY(uid, productoId))")
            }
        }

        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE carrito (
                        uid TEXT NOT NULL,
                        productoId INTEGER NOT NULL,
                        producto TEXT NOT NULL,
                        cantidad INTEGER NOT NULL,
                        precioCentavos INTEGER NOT NULL,
                        solicitudId TEXT,
                        PRIMARY KEY(uid, productoId)
                    )
                """.trimIndent())
            }
        }

        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "productos.db"
            ).addMigrations(MIGRACION_1_2, MIGRACION_2_3).build().also { instancia = it }
        }
    }
}
