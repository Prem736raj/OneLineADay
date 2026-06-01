package com.onelineaday.journal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PolicySection(
                title = "1. Overview",
                content = "Welcome to One Line A Day. We respect your privacy and are committed to protecting it. This Privacy Policy details how we handle information in the application.\n\nLocal-First Guarantee: One Line A Day is a local-first application. All of your journal entries, selected moods, and photo attachments are stored directly and exclusively on your device. We do not operate servers, and we do not collect, transmit, share, or sell your personal data."
            )
            
            PolicySection(
                title = "2. Information Collection and Storage",
                content = "All data processed by the application is kept private and stored locally on your device:\n\n• Journal Entries & Moods: Written entries and mood selections are stored in a local, encrypted-by-default SQLite database managed by the Android Room database framework.\n• Photos: When you attach photos to your journal entries, the app copies the image files into its private internal application directory (filesDir). These files are inaccessible to other apps and are not uploaded to any server.\n• Configuration Data: Application settings, including your dark mode preference and reminder schedules, are saved locally on the device using Android SharedPreferences and DataStore."
            )
            
            PolicySection(
                title = "3. Device Permissions",
                content = "To support its core journaling features, the app requests the following system permissions:\n\n• Notifications (POST_NOTIFICATIONS): Used only to deliver daily reminder alerts prompting you to write your entry. This permission is requested at runtime on supported Android versions, and you can disable reminders or permissions at any time.\n• Receive Boot Completed (RECEIVE_BOOT_COMPLETED): Used to automatically restore and reschedule your configured daily reminder notifications whenever your device is restarted.\n\nThe app does not request or require permissions to access your contacts, location, accounts, microphone, camera, or general external file storage directories."
            )
            
            PolicySection(
                title = "4. Third-Party Libraries",
                content = "The app uses a small number of open-source third-party dependencies to improve functionality:\n\n• Coil: Used locally to load and display attached images on the screens.\n• iText PDF: Used locally on your device to compile and render your journal database into a shareable PDF format.\n\nThese libraries operate fully locally within the app and do not collect, store, or transmit your personal data externally."
            )
            
            PolicySection(
                title = "5. Security of Your Data",
                content = "Because all data remains strictly on your own physical device, the security of your journal relies on your device's overall security settings. We recommend securing your device with a PIN, pattern, password, or biometric lock to prevent unauthorized physical access to your device and journal entries."
            )
            
            PolicySection(
                title = "6. Children's Privacy",
                content = "Our application is completely safe for all users, including children. We do not collect any personal data whatsoever, making the application fully compliant with child privacy laws globally, including COPPA and GDPR."
            )
            
            PolicySection(
                title = "7. Contact Us",
                content = "If you have any questions, suggestions, or concerns regarding this privacy policy, feel free to contact us via email:\n\nDeveloper Support: developeraisteps@gmail.com"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
