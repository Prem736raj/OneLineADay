package com.onelineaday.dailydiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.onelineaday.dailydiary.data.JournalEntry
import com.onelineaday.dailydiary.viewmodel.JournalUiState
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    uiState: JournalUiState,
    modifier: Modifier = Modifier
) {
    // Filter to only entries that have a photo
    val entriesWithPhotos = uiState.entries.filter { it.photoUri != null }
    
    // We can allow clicking a photo to see the entry detail
    var selectedEntry by remember { mutableStateOf<JournalEntry?>(null) }
    
    selectedEntry?.let { entry ->
        EntryDetailScreen(
            entry = entry,
            onBack = { selectedEntry = null },
            onEdit = { /* Not supporting edit from gallery for simplicity */ },
            onDelete = { /* Not supporting delete from gallery for simplicity */ }
        )
        return
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gallery",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (entriesWithPhotos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📸", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Photos Yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Add photos to your entries to see them here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 3 column grid
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entriesWithPhotos) { entry ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f) // Make it a square
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedEntry = entry }
                    ) {
                        AsyncImage(
                            model = entry.photoUri,
                            contentDescription = "Photo from ${entry.date}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Small date overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                .padding(4.dp)
                        ) {
                            Text(
                                text = entry.date.format(DateTimeFormatter.ofPattern("MMM d")),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
