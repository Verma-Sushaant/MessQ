package io.github.sushaant.messq.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.sushaant.messq.data.local.room.dao.MenuDao
import io.github.sushaant.messq.data.local.room.entity.MenuItemEntity

@Database(entities = [MenuItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun menuDao(): MenuDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "messq_local_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}