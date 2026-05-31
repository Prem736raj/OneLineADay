package com.onelineaday.journal.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository for managing journal entries
 */
class JournalRepository(private val journalDao: JournalDao) {
    
    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()
    
    val totalEntryCount: Flow<Int> = journalDao.getTotalEntryCount()
    
    val moodDistribution: Flow<List<MoodCount>> = journalDao.getMoodDistribution()
    
    fun getEntryByDate(date: LocalDate): Flow<JournalEntry?> {
        return journalDao.getEntryByDateFlow(date)
    }
    
    suspend fun getEntryByDateOnce(date: LocalDate): JournalEntry? {
        return journalDao.getEntryByDate(date)
    }
    
    fun getEntriesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<JournalEntry>> {
        return journalDao.getEntriesBetweenDates(startDate, endDate)
    }
    
    fun getEntriesByMood(mood: Mood): Flow<List<JournalEntry>> {
        return journalDao.getEntriesByMood(mood)
    }
    
    fun getRecentEntries(limit: Int = 30): Flow<List<JournalEntry>> {
        return journalDao.getRecentEntries(limit)
    }
    
    fun searchEntries(query: String): Flow<List<JournalEntry>> {
        return journalDao.searchEntries(query)
    }
    
    suspend fun saveEntry(entry: JournalEntry) {
        journalDao.insertEntry(entry)
    }
    
    suspend fun updateEntry(entry: JournalEntry) {
        journalDao.updateEntry(entry.copy(updatedAt = System.currentTimeMillis()))
    }
    
    suspend fun deleteEntry(entry: JournalEntry) {
        journalDao.deleteEntry(entry)
    }
    
    suspend fun deleteEntryByDate(date: LocalDate) {
        journalDao.deleteEntryByDate(date)
    }
    
    suspend fun getEntryCountSince(startDate: LocalDate): Int {
        return journalDao.getEntryCountSince(startDate)
    }
    
    /**
     * Calculate current streak - consecutive days with entries
     */
    suspend fun calculateStreak(entries: List<JournalEntry>): Int {
        if (entries.isEmpty()) return 0
        
        val sortedDates = entries.map { it.date }.sortedDescending()
        val today = LocalDate.now()
        
        // Check if there's an entry for today or yesterday to start the streak
        val firstDate = sortedDates.firstOrNull() ?: return 0
        if (firstDate != today && firstDate != today.minusDays(1)) {
            return 0
        }
        
        var streak = 1
        var currentDate = firstDate
        
        for (i in 1 until sortedDates.size) {
            val nextDate = sortedDates[i]
            if (nextDate == currentDate.minusDays(1)) {
                streak++
                currentDate = nextDate
            } else if (nextDate != currentDate) {
                break
            }
        }
        
        return streak
    }
    
    /**
     * Get longest streak ever achieved
     */
    suspend fun calculateLongestStreak(entries: List<JournalEntry>): Int {
        if (entries.isEmpty()) return 0
        
        val sortedDates = entries.map { it.date }.distinct().sorted()
        var maxStreak = 1
        var currentStreak = 1
        
        for (i in 1 until sortedDates.size) {
            if (sortedDates[i] == sortedDates[i-1].plusDays(1)) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }
        
        return maxStreak
    }
}
