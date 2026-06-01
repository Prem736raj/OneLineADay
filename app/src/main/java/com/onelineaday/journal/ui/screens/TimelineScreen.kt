package com.onelineaday.journal.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.onelineaday.journal.R
import com.onelineaday.journal.data.JournalEntry
import com.onelineaday.journal.data.Mood
import com.onelineaday.journal.ui.components.*
import com.onelineaday.journal.ui.theme.*
import com.onelineaday.journal.viewmodel.JournalUiState
import com.onelineaday.journal.viewmodel.JournalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: JournalViewModel,
    uiState: JournalUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var editingEntry by remember { mutableStateOf<JournalEntry?>(null) }
    val listState = rememberLazyListState()
    
    val entries = if (searchQuery.isNotEmpty()) {
        uiState.searchResults
    } else {
        uiState.entries
    }
    
    // Group entries by month
    val groupedEntries = entries.groupBy { entry ->
        entry.date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }
    
    // Show edit dialog if editing
    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            viewModel = viewModel,
            onDismiss = { editingEntry = null },
            onSave = { newContent, newMood, newPhotoUri ->
                viewModel.updateEntry(entry, newContent, newMood, newPhotoUri)
                editingEntry = null
            }
        )
    }
    
    // Show detail screen if entry is selected
    selectedEntry?.let { entry ->
        EntryDetailScreen(
            entry = entry,
            onBack = { selectedEntry = null },
            onDelete = {
                viewModel.deleteEntry(entry)
                selectedEntry = null
            }
        )
        return
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (!showSearch) {
                            Text(
                                text = stringResource(R.string.timeline),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        AnimatedVisibility(
                            visible = showSearch,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut()
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it
                                    viewModel.search(it)
                                },
                                placeholder = { Text(stringResource(R.string.timeline_search)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                showSearch = !showSearch
                                if (!showSearch) {
                                    searchQuery = ""
                                    viewModel.search("")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (showSearch) Icons.Rounded.Close else Icons.Rounded.Search,
                                contentDescription = if (showSearch) stringResource(R.string.timeline_close_search) else stringResource(R.string.timeline_open_search)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { paddingValues ->
        if (entries.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "🔍" else "📚",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = if (searchQuery.isNotEmpty()) 
                            "No entries found" 
                        else 
                            stringResource(R.string.timeline_your_timeline_awaits),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            stringResource(R.string.timeline_try_different)
                        else
                            stringResource(R.string.timeline_start_writing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                groupedEntries.forEach { (monthYear, monthEntries) ->
                    item {
                        MonthHeader(monthYear = monthYear, entryCount = monthEntries.size)
                    }
                    
                    items(
                        items = monthEntries,
                        key = { it.date.toString() }
                    ) { entry ->
                        val dateLabel = viewModel.getRelativeDate(entry.date)
                        
                        EntryCard(
                            entry = entry,
                            dateLabel = dateLabel,
                            onClick = { selectedEntry = entry },
                            onEdit = { editingEntry = entry },
                            onDelete = { viewModel.deleteEntry(entry) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun MonthHeader(
    monthYear: String,
    entryCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = monthYear,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = if (entryCount == 1) stringResource(R.string.timeline_entry_count, entryCount) else stringResource(R.string.timeline_entries_count, entryCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryDialog(
    entry: JournalEntry,
    viewModel: JournalViewModel,
    onDismiss: () -> Unit,
    onSave: (String, Mood, String?) -> Unit
) {
    val context = LocalContext.current
    var editedText by remember { mutableStateOf(entry.content) }
    var editedMood by remember { mutableStateOf(entry.mood) }
    var editedPhotoUri by remember { mutableStateOf(entry.photoUri) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.timeline_edit_entry),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date display
                Text(
                    text = entry.date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Mood picker
                Text(
                    text = stringResource(R.string.timeline_mood),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Mood.entries.forEach { mood ->
                        Surface(
                            onClick = { editedMood = mood },
                            shape = RoundedCornerShape(12.dp),
                            color = if (editedMood == mood) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = mood.emoji,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
                
                // Text input
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    label = { Text(stringResource(R.string.timeline_your_line)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )
                
                // Photo section
                PhotoAttachment(
                    photoUri = editedPhotoUri,
                    onPhotoSelected = { uri ->
                        val savedPath = viewModel.savePhotoToInternal(context, uri)
                        editedPhotoUri = savedPath
                    },
                    onPhotoRemoved = { editedPhotoUri = null }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (editedText.isNotBlank()) {
                        onSave(editedText, editedMood, editedPhotoUri) 
                    }
                },
                enabled = editedText.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
