package com.onelineaday.journal.data

import android.content.Context
import androidx.room.*
import java.time.LocalDate

/**
 * Room database for storing journal entries
 */
@Database(
    entities = [JournalEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class JournalDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
    
    companion object {
        @Volatile
        private var INSTANCE: JournalDatabase? = null
        
        fun getDatabase(context: Context): JournalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JournalDatabase::class.java,
                    "journal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Type converters for Room to handle LocalDate and Mood
 */
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
    
    @TypeConverter
    fun fromMood(mood: Mood): String {
        return mood.name
    }
    
    @TypeConverter
    fun toMood(moodName: String): Mood {
        return Mood.valueOf(moodName)
    }
}
