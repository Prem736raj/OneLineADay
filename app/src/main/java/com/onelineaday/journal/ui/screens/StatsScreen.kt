package com.onelineaday.journal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onelineaday.journal.ui.components.*
import com.onelineaday.journal.ui.theme.*
import com.onelineaday.journal.viewmodel.JournalUiState
import com.onelineaday.journal.viewmodel.JournalViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: JournalViewModel,
    uiState: JournalUiState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    // Calculate additional stats
    val firstEntryDate = uiState.entries.minByOrNull { it.date }?.date
    val journeyDays = firstEntryDate?.let { 
        ChronoUnit.DAYS.between(it, LocalDate.now()).toInt() + 1 
    } ?: 0
    
    val thisMonthEntries = uiState.entries.filter { 
        it.date.month == LocalDate.now().month && 
        it.date.year == LocalDate.now().year 
    }.size
    
    val thisWeekEntries = uiState.entries.filter { entry ->
        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        entry.date >= weekStart && entry.date <= today
    }.size
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Journey",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Streak Card (prominent)
            StreakCard(
                currentStreak = uiState.currentStreak,
                longestStreak = uiState.longestStreak
            )
            
            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    title = "Total Entries",
                    value = uiState.totalEntries.toString(),
                    subtitle = "memories captured",
                    icon = Icons.Rounded.Book,
                    gradientColors = listOf(AccentTeal, AccentTeal.copy(blue = 0.6f)),
                    modifier = Modifier.weight(1f)
                )
                
                StatsCard(
                    title = "Journey",
                    value = journeyDays.toString(),
                    subtitle = "days since start",
                    icon = Icons.Rounded.Timeline,
                    gradientColors = listOf(LavenderMid, LavenderDark),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Weekly & Monthly Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    title = "This Week",
                    value = thisWeekEntries.toString(),
                    subtitle = "${7 - thisWeekEntries} days left",
                    icon = Icons.Rounded.DateRange,
                    gradientColors = listOf(MoodMotivated, MoodMotivated.copy(green = 0.6f)),
                    modifier = Modifier.weight(1f)
                )
                
                StatsCard(
                    title = "This Month",
                    value = thisMonthEntries.toString(),
                    subtitle = "entries",
                    icon = Icons.Rounded.CalendarMonth,
                    gradientColors = listOf(SunsetRose, SunsetPink),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Mood Distribution
            MoodDistributionCard(
                moodDistribution = uiState.moodDistribution
            )
            
            // Milestones Section
            MilestonesCard(
                totalEntries = uiState.totalEntries,
                currentStreak = uiState.currentStreak,
                journeyDays = journeyDays
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun MilestonesCard(
    totalEntries: Int,
    currentStreak: Int,
    journeyDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Milestones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Entry milestones
            val entryMilestones = listOf(
                Triple(7, "First Week", "📅"),
                Triple(30, "One Month", "📆"),
                Triple(100, "Century Club", "💯"),
                Triple(365, "One Year", "🎉"),
                Triple(1000, "Legendary", "🏆")
            )
            
            entryMilestones.forEach { (target, label, emoji) ->
                MilestoneItem(
                    emoji = emoji,
                    label = label,
                    current = totalEntries,
                    target = target,
                    isCompleted = totalEntries >= target
                )
                
                if (target != 1000) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun MilestoneItem(
    emoji: String,
    label: String,
    current: Int,
    target: Int,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = (current.toFloat() / target).coerceIn(0f, 1f)
    
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isCompleted) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (isCompleted) "✓" else "$current/$target",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isCompleted) 
                        Success 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (isCompleted) Success else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
