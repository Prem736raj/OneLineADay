package com.onelineaday.dailydiary.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.onelineaday.dailydiary.R
import com.onelineaday.dailydiary.data.JournalEntry
import com.onelineaday.dailydiary.data.Mood
import com.onelineaday.dailydiary.ui.components.*
import com.onelineaday.dailydiary.ui.components.PhotoAttachment
import com.onelineaday.dailydiary.ui.components.PremiumDialog
import com.onelineaday.dailydiary.ui.components.AudioAttachment
import com.onelineaday.dailydiary.ui.theme.*
import com.onelineaday.dailydiary.viewmodel.JournalViewModel
import com.onelineaday.dailydiary.ads.InterstitialAdManager
import com.onelineaday.dailydiary.ads.BannerAdView
import android.app.Activity
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JournalViewModel,
    uiState: com.onelineaday.dailydiary.viewmodel.JournalUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    var entryText by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(Mood.NEUTRAL) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    
    var showPremiumDialog by remember { mutableStateOf(false) }
    
    // When entry is saved, clear the form completely for a fresh start
    LaunchedEffect(uiState.entrySaved) {
        if (uiState.entrySaved) {
            // Clear the form after saving
            entryText = ""
            selectedMood = Mood.NEUTRAL
            photoUri = null
            viewModel.clearEntrySavedFlag()
        }
    }
    
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    
    val throwback = remember(uiState.entries, today) {
        val throwbackEntry = uiState.entries.firstOrNull { 
            it.date.monthValue == today.monthValue && 
            it.date.dayOfMonth == today.dayOfMonth && 
            it.date.year < today.year 
        }
        if (throwbackEntry != null) {
            val yearsAgo = today.year - throwbackEntry.date.year
            Pair(yearsAgo, throwbackEntry)
        } else {
            null
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BannerAdView()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.today),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = today.format(dateFormatter),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                val isPremium by com.onelineaday.dailydiary.PremiumManager.isPremium.collectAsState()
                if (!isPremium) {
                    IconButton(
                        onClick = { showPremiumDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = "Go Premium",
                            tint = Color(0xFFFFA500)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Throwback Card
            if (throwback != null) {
                val (yearsAgo, entry) = throwback
                Text(
                    text = "🕰️ $yearsAgo Year${if (yearsAgo > 1) "s" else ""} Ago Today...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                EntryCard(
                    entry = entry,
                    onClick = { /* Could open detail view, but for now just display */ }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streak mini card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SunsetOrange.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${uiState.currentStreak}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SunsetOrange
                            )
                            Text(
                                text = stringResource(R.string.home_day_streak),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Total entries mini card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentTeal.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📝", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${uiState.totalEntries}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal
                            )
                            Text(
                                text = stringResource(R.string.home_memories),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main Entry Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Mood Picker
                    MoodPicker(
                        selectedMood = selectedMood,
                        onMoodSelected = { selectedMood = it }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Stylish Text Input with Gradient Colors
                    StylishTextInput(
                        value = entryText,
                        onValueChange = { entryText = it },
                        placeholder = stringResource(R.string.write_your_line)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    val audioFile = remember(uiState.selectedDate) { java.io.File(context.filesDir, "audio_${uiState.selectedDate}.m4a") }
                    var hasAudio by remember(uiState.selectedDate) { androidx.compose.runtime.mutableStateOf(audioFile.exists()) }
                    
                    // Photo Attachment
                    PhotoAttachment(
                        photoUri = photoUri,
                        onPhotoSelected = { uri ->
                            val savedPath = viewModel.savePhotoToInternal(context, uri)
                            photoUri = savedPath
                        },
                        onPhotoRemoved = { photoUri = null }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Audio Attachment
                    AudioAttachment(
                        dateKey = uiState.selectedDate.toString(),
                        onAudioSaved = { hasAudio = true },
                        onAudioDeleted = { hasAudio = false }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Save Button
                    val canSave = entryText.isNotBlank() || photoUri != null || hasAudio
                    Button(
                        onClick = {
                            if (canSave) {
                                viewModel.saveEntry(entryText, selectedMood, photoUri)
                                val activity = context as? Activity
                                if (activity != null) {
                                    InterstitialAdManager.showAdIfTimePassed(activity)
                                }
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (uiState.todayEntry != null) Icons.Rounded.Edit else Icons.Rounded.Check,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.todayEntry != null) stringResource(R.string.home_update_entry) else stringResource(R.string.home_save_entry),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Motivational Message
            if (uiState.totalEntries == 0) {
                EmptyStateMessage()
            } else if (uiState.todayEntry != null) {
                Text(
                    text = stringResource(R.string.home_today_captured),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    // Show snackbar for messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }
    
    if (showPremiumDialog) {
        PremiumDialog(
            onDismiss = { showPremiumDialog = false }
        )
    }
}

@Composable
fun EmptyStateMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌟",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.home_start_journey),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.home_start_writing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
