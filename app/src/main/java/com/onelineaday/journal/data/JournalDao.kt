package com.onelineaday.journal.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for journal entries
 */
@Dao
interface JournalDao {
    
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>
    
    @Query("SELECT * FROM journal_entries WHERE date = :date")
    suspend fun getEntryByDate(date: LocalDate): JournalEntry?
    
    @Query("SELECT * FROM journal_entries WHERE date = :date")
    fun getEntryByDateFlow(date: LocalDate): Flow<JournalEntry?>
    
    @Query("SELECT * FROM journal_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getEntriesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<JournalEntry>>
    
    @Query("SELECT * FROM journal_entries WHERE mood = :mood ORDER BY date DESC")
    fun getEntriesByMood(mood: Mood): Flow<List<JournalEntry>>
    
    @Query("SELECT COUNT(*) FROM journal_entries")
    fun getTotalEntryCount(): Flow<Int>
    
    @Query("SELECT * FROM journal_entries ORDER BY date DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): Flow<List<JournalEntry>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)
    
    @Update
    suspend fun updateEntry(entry: JournalEntry)
    
    @Delete
    suspend fun deleteEntry(entry: JournalEntry)
    
    @Query("DELETE FROM journal_entries WHERE date = :date")
    suspend fun deleteEntryByDate(date: LocalDate)
    
    @Query("SELECT * FROM journal_entries WHERE content LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchEntries(query: String): Flow<List<JournalEntry>>
    
    // Stats queries
    @Query("""
        SELECT COUNT(DISTINCT date) FROM journal_entries 
        WHERE date >= :startDate
    """)
    suspend fun getEntryCountSince(startDate: LocalDate): Int
    
    @Query("SELECT mood, COUNT(*) as count FROM journal_entries GROUP BY mood ORDER BY count DESC")
    fun getMoodDistribution(): Flow<List<MoodCount>>
}

data class MoodCount(
    val mood: Mood,
    val count: Int
)
