package com.onelineaday.journal.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.onelineaday.journal.notifications.ReminderManager
import com.onelineaday.journal.ui.theme.*
import com.onelineaday.journal.viewmodel.JournalUiState
import com.onelineaday.journal.viewmodel.JournalViewModel
import com.onelineaday.journal.widget.JournalWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: JournalViewModel,
    uiState: JournalUiState,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    var isExporting by remember { mutableStateOf(false) }
    
    // Notification settings
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var notificationsEnabled by remember { 
        mutableStateOf(prefs.getBoolean("reminders_enabled", false)) 
    }
    var reminderHour by remember { mutableStateOf(prefs.getInt("reminder_hour", 20)) }
    var reminderMinute by remember { mutableStateOf(prefs.getInt("reminder_minute", 0)) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Permission launcher for notifications
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationsEnabled = true
            prefs.edit().putBoolean("reminders_enabled", true).apply()
            ReminderManager(context).scheduleDailyReminder(reminderHour, reminderMinute)
            Toast.makeText(context, "Daily reminders enabled! 🔔", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission needed for reminders", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun toggleNotifications(enabled: Boolean) {
        if (enabled) {
            // Check and request permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                
                if (!hasPermission) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return
                }
            }
            
            notificationsEnabled = true
            prefs.edit().putBoolean("reminders_enabled", true).apply()
            ReminderManager(context).scheduleDailyReminder(reminderHour, reminderMinute)
            Toast.makeText(context, "Daily reminders enabled! 🔔", Toast.LENGTH_SHORT).show()
        } else {
            notificationsEnabled = false
            prefs.edit().putBoolean("reminders_enabled", false).apply()
            ReminderManager(context).cancelReminder()
            Toast.makeText(context, "Reminders disabled", Toast.LENGTH_SHORT).show()
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsToggleItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    isChecked = isDarkMode,
                    onCheckedChange = onDarkModeChange
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsToggleItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Daily Reminder",
                    subtitle = if (notificationsEnabled) 
                        "Reminder at ${String.format("%02d:%02d", reminderHour, reminderMinute)}" 
                    else 
                        "Get a gentle nudge to write",
                    isChecked = notificationsEnabled,
                    onCheckedChange = { toggleNotifications(it) }
                )
                
                if (notificationsEnabled) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    SettingsItem(
                        icon = Icons.Rounded.Schedule,
                        title = "Reminder Time",
                        subtitle = String.format("%02d:%02d", reminderHour, reminderMinute),
                        onClick = { showTimePicker = true }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Data Section
            SettingsSection(title = "Data") {
                SettingsItem(
                    icon = Icons.Rounded.PictureAsPdf,
                    title = "Export to PDF",
                    subtitle = "Download your journal as PDF",
                    onClick = {
                        scope.launch {
                            isExporting = true
                            try {
                                val file = exportToPdf(context, uiState)
                                sharePdf(context, file)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context, 
                                    "Export failed: ${e.message}", 
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            isExporting = false
                        }
                    },
                    isLoading = isExporting
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                SettingsItem(
                    icon = Icons.Rounded.Widgets,
                    title = "Refresh Widget",
                    subtitle = "Update home screen widget",
                    onClick = { 
                        JournalWidget.updateAllWidgets(context)
                        Toast.makeText(context, "Widget refreshed!", Toast.LENGTH_SHORT).show()
                    }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                SettingsItem(
                    icon = Icons.Rounded.Backup,
                    title = "Backup Data",
                    subtitle = "Coming soon",
                    onClick = { 
                        Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show()
                    },
                    enabled = false
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Rounded.Info,
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                SettingsItem(
                    icon = Icons.Rounded.Code,
                    title = "Made with ❤️",
                    subtitle = "One Line A Day",
                    onClick = { }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📝",
                        style = MaterialTheme.typography.displayMedium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "One Line A Day",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Capture life, one line at a time",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "${uiState.totalEntries} memories captured",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderHour,
            initialMinute = reminderMinute
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Set Reminder Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderHour = timePickerState.hour
                        reminderMinute = timePickerState.minute
                        prefs.edit()
                            .putInt("reminder_hour", reminderHour)
                            .putInt("reminder_minute", reminderMinute)
                            .apply()
                        
                        // Reschedule with new time
                        ReminderManager(context).scheduleDailyReminder(reminderHour, reminderMinute)
                        Toast.makeText(
                            context, 
                            "Reminder set for ${String.format("%02d:%02d", reminderHour, reminderMinute)}", 
                            Toast.LENGTH_SHORT
                        ).show()
                        showTimePicker = false
                    }
                ) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled && !isLoading) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

private suspend fun exportToPdf(context: Context, uiState: JournalUiState): File {
    return withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "OneLineADay_Journal.pdf")
        
        // Create proper PDF using iText7
        val writer = com.itextpdf.kernel.pdf.PdfWriter(file)
        val pdf = com.itextpdf.kernel.pdf.PdfDocument(writer)
        val document = com.itextpdf.layout.Document(pdf)
        
        // Colors
        val primaryColor = com.itextpdf.kernel.colors.DeviceRgb(147, 51, 234) // Purple
        val secondaryColor = com.itextpdf.kernel.colors.DeviceRgb(236, 72, 153) // Pink
        val backgroundColor = com.itextpdf.kernel.colors.DeviceRgb(250, 245, 255) // Light purple bg
        val textColor = com.itextpdf.kernel.colors.DeviceRgb(30, 30, 30)
        val mutedColor = com.itextpdf.kernel.colors.DeviceRgb(120, 120, 120)
        
        // Fonts
        val titleFont = com.itextpdf.kernel.font.PdfFontFactory.createFont(
            com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD
        )
        val normalFont = com.itextpdf.kernel.font.PdfFontFactory.createFont(
            com.itextpdf.io.font.constants.StandardFonts.HELVETICA
        )
        
        // ===== HEADER SECTION =====
        val headerTable = com.itextpdf.layout.element.Table(1)
            .useAllAvailableWidth()
            .setBackgroundColor(primaryColor)
            .setPadding(20f)
        
        val headerCell = com.itextpdf.layout.element.Cell()
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .add(com.itextpdf.layout.element.Paragraph("📝 ONE LINE A DAY")
                .setFont(titleFont)
                .setFontSize(28f)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
            .add(com.itextpdf.layout.element.Paragraph("Your Personal Journal")
                .setFont(normalFont)
                .setFontSize(14f)
                .setFontColor(com.itextpdf.kernel.colors.DeviceRgb(220, 220, 255))
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginTop(5f))
        headerTable.addCell(headerCell)
        document.add(headerTable)
        
        // ===== STATS SECTION =====
        val statsTable = com.itextpdf.layout.element.Table(3)
            .useAllAvailableWidth()
            .setMarginTop(20f)
            .setMarginBottom(20f)
        
        // Stat boxes
        listOf(
            Triple("🔥", "${uiState.currentStreak}", "Day Streak"),
            Triple("📚", "${uiState.totalEntries}", "Total Entries"),
            Triple("🏆", "${uiState.longestStreak}", "Best Streak")
        ).forEach { (emoji, value, label) ->
            val statCell = com.itextpdf.layout.element.Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBackgroundColor(backgroundColor)
                .setPadding(15f)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .add(com.itextpdf.layout.element.Paragraph(emoji)
                    .setFontSize(24f)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
                .add(com.itextpdf.layout.element.Paragraph(value)
                    .setFont(titleFont)
                    .setFontSize(20f)
                    .setFontColor(primaryColor)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
                .add(com.itextpdf.layout.element.Paragraph(label)
                    .setFont(normalFont)
                    .setFontSize(10f)
                    .setFontColor(mutedColor)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
            statsTable.addCell(statCell)
        }
        document.add(statsTable)
        
        // ===== ENTRIES SECTION =====
        document.add(com.itextpdf.layout.element.Paragraph("📖 Your Memories")
            .setFont(titleFont)
            .setFontSize(18f)
            .setFontColor(primaryColor)
            .setMarginTop(20f)
            .setMarginBottom(15f))
        
        val dateTimeFormatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        
        uiState.entries.sortedByDescending { it.date }.forEach { entry ->
            // Entry container table
            val entryTable = com.itextpdf.layout.element.Table(1)
                .useAllAvailableWidth()
                .setMarginBottom(15f)
                .setBorder(com.itextpdf.layout.borders.SolidBorder(
                    com.itextpdf.kernel.colors.DeviceRgb(230, 230, 230), 1f))
            
            val entryCell = com.itextpdf.layout.element.Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(15f)
            
            // Date and mood header
            val dateStr = entry.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
            val timeStr = dateTimeFormatter.format(java.util.Date(entry.updatedAt))
            
            entryCell.add(com.itextpdf.layout.element.Paragraph("${entry.mood.emoji}  $dateStr")
                .setFont(titleFont)
                .setFontSize(13f)
                .setFontColor(primaryColor))
            
            // Time with clock emoji
            entryCell.add(com.itextpdf.layout.element.Paragraph("🕐 $timeStr  •  ${entry.mood.label}")
                .setFont(normalFont)
                .setFontSize(10f)
                .setFontColor(mutedColor)
                .setMarginBottom(10f))
            
            // Content
            entryCell.add(com.itextpdf.layout.element.Paragraph(entry.content)
                .setFont(normalFont)
                .setFontSize(12f)
                .setFontColor(textColor))
            
            // Add photo if exists
            entry.photoUri?.let { photoPath ->
                try {
                    val photoFile = File(photoPath)
                    if (photoFile.exists()) {
                        val imageData = com.itextpdf.io.image.ImageDataFactory.create(photoPath)
                        val image = com.itextpdf.layout.element.Image(imageData)
                            .setMaxWidth(250f)
                            .setMaxHeight(200f)
                            .setMarginTop(10f)
                            .setBorderRadius(com.itextpdf.layout.properties.BorderRadius(8f))
                        
                        // Add photo label
                        entryCell.add(com.itextpdf.layout.element.Paragraph("📷 Photo Memory")
                            .setFont(normalFont)
                            .setFontSize(9f)
                            .setFontColor(secondaryColor)
                            .setMarginTop(10f))
                        
                        entryCell.add(image)
                    }
                } catch (e: Exception) {
                    // Skip image if can't load
                    e.printStackTrace()
                }
            }
            
            entryTable.addCell(entryCell)
            document.add(entryTable)
        }
        
        // ===== FOOTER =====
        document.add(com.itextpdf.layout.element.Paragraph("✨ Generated by One Line A Day")
            .setFont(normalFont)
            .setFontSize(10f)
            .setFontColor(secondaryColor)
            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
            .setMarginTop(30f))
        
        document.add(com.itextpdf.layout.element.Paragraph("Capture life, one line at a time 💜")
            .setFont(normalFont)
            .setFontSize(9f)
            .setFontColor(mutedColor)
            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
            .setMarginTop(5f))
        
        document.close()
        file
    }
}

private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "My One Line A Day Journal")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    
    context.startActivity(Intent.createChooser(intent, "Share Journal PDF"))
}
