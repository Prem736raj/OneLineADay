package com.onelineaday.dailydiary.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.onelineaday.dailydiary.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.io.File
import java.io.FileOutputStream

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val todayEntry: JournalEntry? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalEntries: Int = 0,
    val moodDistribution: List<MoodCount> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val searchQuery: String = "",
    val searchResults: List<JournalEntry> = emptyList(),
    val entrySaved: Boolean = false  // Flag to trigger form reset
)

class JournalViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = JournalDatabase.getDatabase(application)
    private val repository = JournalRepository(database.journalDao())
    
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    
    init {
        loadData()
    }
    
    private fun loadData() {
        // Load all entries
        viewModelScope.launch {
            repository.allEntries.collect { entries ->
                val streak = repository.calculateStreak(entries)
                val longestStreak = repository.calculateLongestStreak(entries)
                
                _uiState.update { 
                    it.copy(
                        entries = entries,
                        currentStreak = streak,
                        longestStreak = longestStreak,
                        totalEntries = entries.size,
                        isLoading = false
                    )
                }
            }
        }
        
        // Load today's entry
        viewModelScope.launch {
            repository.getEntryByDate(LocalDate.now()).collect { entry ->
                _uiState.update { it.copy(todayEntry = entry) }
            }
        }
        
        // Load mood distribution
        viewModelScope.launch {
            repository.moodDistribution.collect { distribution ->
                _uiState.update { it.copy(moodDistribution = distribution) }
            }
        }
    }
    
    fun saveEntry(content: String, mood: Mood, photoUri: String? = null) {
        viewModelScope.launch {
            val existingEntry = repository.getEntryByDateOnce(_selectedDate.value)
            
            val entry = if (existingEntry != null) {
                existingEntry.copy(
                    content = content,
                    mood = mood,
                    photoUri = photoUri,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                JournalEntry(
                    date = _selectedDate.value,
                    content = content,
                    mood = mood,
                    photoUri = photoUri
                )
            }
            
            repository.saveEntry(entry)
            _uiState.update { it.copy(message = "Entry saved!", entrySaved = true) }
        }
    }
    
    fun clearEntrySavedFlag() {
        _uiState.update { it.copy(entrySaved = false) }
    }
    
    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            _uiState.update { it.copy(message = "Entry deleted") }
        }
    }
    
    fun updateEntry(entry: JournalEntry, newContent: String, newMood: Mood, newPhotoUri: String?) {
        viewModelScope.launch {
            val updatedEntry = entry.copy(
                content = newContent,
                mood = newMood,
                photoUri = newPhotoUri,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveEntry(updatedEntry)
            _uiState.update { it.copy(message = "Entry updated!") }
        }
    }
    
    fun deleteEntryByDate(date: LocalDate) {
        viewModelScope.launch {
            repository.deleteEntryByDate(date)
            _uiState.update { it.copy(message = "Entry deleted") }
        }
    }
    
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        _uiState.update { it.copy(selectedDate = date) }
        
        viewModelScope.launch {
            repository.getEntryByDate(date).collect { entry ->
                _uiState.update { it.copy(todayEntry = entry) }
            }
        }
    }
    
    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        
        viewModelScope.launch {
            repository.searchEntries(query).collect { results ->
                _uiState.update { it.copy(searchResults = results) }
            }
        }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    fun savePhotoToInternal(context: Context, sourceUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun getEntriesForMonth(year: Int, month: Int): List<JournalEntry> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        
        return _uiState.value.entries.filter { entry ->
            entry.date >= startDate && entry.date <= endDate
        }
    }
    
    fun getFormattedDate(date: LocalDate): String {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        return when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
        }
    }
    
    fun getRelativeDate(date: LocalDate): String {
        val today = LocalDate.now()
        val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(date, today).toInt()
        
        return when {
            daysDiff == 0 -> "Today"
            daysDiff == 1 -> "Yesterday"
            daysDiff < 7 -> "$daysDiff days ago"
            daysDiff < 30 -> "${daysDiff / 7} weeks ago"
            daysDiff < 365 -> "${daysDiff / 30} months ago"
            else -> "${daysDiff / 365} years ago"
        }
    }
    
    fun exportBackup(context: Context): Uri? {
        return try {
            val dbFolder = context.getDatabasePath("journal_database").parentFile
            val filesDir = context.filesDir
            
            val backupFile = File(context.cacheDir, "OneLineADay_Backup_${System.currentTimeMillis()}.zip")
            val zipOut = java.util.zip.ZipOutputStream(FileOutputStream(backupFile))
            
            // Backup DB files
            dbFolder?.listFiles()?.forEach { file ->
                if (file.name.startsWith("journal_database")) {
                    val entry = java.util.zip.ZipEntry("db/${file.name}")
                    zipOut.putNextEntry(entry)
                    file.inputStream().copyTo(zipOut)
                    zipOut.closeEntry()
                }
            }
            
            // Backup Images
            filesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("photo_") && file.name.endsWith(".jpg")) {
                    val entry = java.util.zip.ZipEntry("photos/${file.name}")
                    zipOut.putNextEntry(entry)
                    file.inputStream().copyTo(zipOut)
                    zipOut.closeEntry()
                }
            }
            
            zipOut.close()
            
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                backupFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun importBackup(context: Context, backupUri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val dbFolder = context.getDatabasePath("journal_database").parentFile
                val filesDir = context.filesDir
                
                context.contentResolver.openInputStream(backupUri)?.use { input ->
                    val zipIn = java.util.zip.ZipInputStream(input)
                    var entry = zipIn.nextEntry
                    
                    while (entry != null) {
                        val file = if (entry.name.startsWith("db/")) {
                            File(dbFolder, entry.name.removePrefix("db/"))
                        } else if (entry.name.startsWith("photos/")) {
                            File(filesDir, entry.name.removePrefix("photos/"))
                        } else {
                            null
                        }
                        
                        file?.let { f ->
                            if (f.exists()) f.delete()
                            FileOutputStream(f).use { output ->
                                zipIn.copyTo(output)
                            }
                        }
                        
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
                
                _uiState.update { it.copy(message = "Backup restored successfully! Please restart the app.") }
                
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(message = "Failed to restore backup.") }
            }
        }
    }
    
    suspend fun exportToPdf(context: Context): android.net.Uri? {
        val entries = _uiState.value.entries
        return com.onelineaday.dailydiary.utils.PdfExportHelper.generateJournalPdf(context, entries)
    }
}
