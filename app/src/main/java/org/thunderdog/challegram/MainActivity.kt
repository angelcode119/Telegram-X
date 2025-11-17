package org.thunderdog.challegram

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import org.thunderdog.challegram.ui.theme.TelegramXTheme
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // لاگ شروع اپ
        Log.d("MainActivity", "onCreate() called - Initializing Firebase...")

        // تست اولیه فایربیس
        try {
            FirebaseApp.initializeApp(this)
            Log.d("Firebase", "FirebaseApp initialized successfully.")
        } catch (e: Exception) {
            Log.e("Firebase", "Firebase initialization failed!", e)
        }

        setContent {
            TelegramXTheme {
                FirebaseTokenScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseTokenScreen() {
    val context = LocalContext.current
    var fcmToken by remember { mutableStateOf<String?>("Loading token...") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // گرفتن توکن FCM با لاگ کامل
    LaunchedEffect(Unit) {
        Log.d("FCM", "Requesting FCM token...")
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            fcmToken = token
            errorMessage = null
            Log.d("FCM_TOKEN", "Token retrieved successfully:")
            Log.d("FCM_TOKEN", token) // توکن کامل
        } catch (e: Exception) {
            val error = "Error: ${e.message}"
            fcmToken = error
            errorMessage = e.message
            Log.e("FCM_TOKEN", "Failed to get FCM token", e)
            Log.e("FCM_TOKEN", "Exception class: ${e.javaClass.name}")
            Log.e("FCM_TOKEN", "Stack trace: ${e.stackTraceToString()}")
        } finally {
            isLoading = false
            Log.d("FCM", "Token loading finished. isLoading = false")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FCM Token", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // کادر توکن
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your FCM Token:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Fetching token from Firebase...", fontSize = 13.sp)
                        }
                    } else {
                        Text(
                            text = fcmToken ?: "Unknown",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = if (errorMessage != null) Color.Red else MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            softWrap = true
                        )
                    }

                    // دکمه کپی
                    Button(
                        onClick = {
                            if (!isLoading && errorMessage == null && fcmToken != null) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("FCM Token", fcmToken)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Token copied to clipboard!", Toast.LENGTH_SHORT).show()
                                Log.d("Clipboard", "FCM Token copied: ${fcmToken?.take(50)}...")
                            }
                        },
                        enabled = !isLoading && errorMessage == null && fcmToken != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Copy Token")
                    }
                }
            }

            // پیام راهنما
            Text(
                text = "This token is used to send push notifications.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )

            // دیباگ اضافی (فقط در حالت دیباگ)
            if (errorMessage != null) {
                Text(
                    text = "Debug: $errorMessage",
                    fontSize = 10.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// آیکون‌ها
@Composable
fun Icons = androidx.compose.material.icons.Icons

@Preview(showBackground = true)
@Composable
fun TokenScreenPreview() {
    TelegramXTheme {
        FirebaseTokenScreen()
    }
}