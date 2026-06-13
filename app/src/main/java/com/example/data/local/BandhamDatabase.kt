package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FamilyMember
import com.example.data.model.EventReminder
import com.example.data.model.MoneyTransaction

@Database(
    entities = [FamilyMember::class, EventReminder::class, MoneyTransaction::class],
    version = 2,
    exportSchema = false
)
abstract class BandhamDatabase : RoomDatabase() {
    abstract fun bandhamDao(): BandhamDao

    companion object {
        @Volatile
        private var INSTANCE: BandhamDatabase? = null

        fun getDatabase(context: Context): BandhamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BandhamDatabase::class.java,
                    "bandham_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
