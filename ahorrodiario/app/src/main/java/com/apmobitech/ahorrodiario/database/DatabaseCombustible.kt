package com.apmobitech.ahorrodiario.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. LA TABLA (Lo que guardamos de cada gasolinera)
@Entity(tableName = "gasolineras_favoritas")
data class GasolineraFavorita(
    @PrimaryKey val idEstacion: String,
    val rotulo: String,
    val latitud: String,
    val longitud: String
)

// 2. LAS ACCIONES (SQL)
// 2. LAS ACCIONES (SQL)
@Dao
interface GasolineraDao {
    @Query("SELECT * FROM gasolineras_favoritas")
    fun obtenerTodas(): List<GasolineraFavorita> // <-- Sin suspend

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertar(gasolinera: GasolineraFavorita) // <-- Sin suspend

    @Query("DELETE FROM gasolineras_favoritas WHERE idEstacion = :idEstacion")
    fun eliminar(idEstacion: String) // <-- Sin suspend
}

// 3. EL MOTOR DE LA BASE DE DATOS
@Database(entities = [GasolineraFavorita::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gasolineraDao(): GasolineraDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ahorro_diario_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}