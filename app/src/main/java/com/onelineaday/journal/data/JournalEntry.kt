package com.onelineaday.journal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Represents a single journal entry - one line about the user's day
 */
@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey
    val date: LocalDate,
    val content: String,
    val mood: Mood = Mood.NEUTRAL,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Available mood options
 */
enum class Mood(val emoji: String, val label: String) {
    HAPPY("😊", "Happy"),
    NEUTRAL("😐", "Neutral"),
    SAD("😢", "Sad"),
    ANGRY("😤", "Angry"),
    EXCITED("🤩", "Excited"),
    TIRED("🥱", "Tired"),
    MOTIVATED("💪", "Motivated"),
    LOVED("❤️", "Loved"),
    ANXIOUS("😰", "Anxious"),
    GRATEFUL("🙏", "Grateful")
}
