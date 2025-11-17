package org.thunderdog.challegram

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "App started!")
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            Log.d("Firebase", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("Firebase", "Firebase init failed!", e)
        }
        
        // یه TextView ساده برای تست
        val textView = TextView(this).apply {
            text = "Hello! App is running.\nChecking FCM token..."
            textSize = 18f
            setPadding(50, 50, 50, 50)
        }
        setContentView(textView)
        
        // گرفتن توکن FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", "Token: $token")
                textView.text = "✅ App is working!\n\nFCM Token:\n$token"
            } else {
                Log.e("FCM_TOKEN", "Failed to get token", task.exception)
                textView.text = "❌ Error getting FCM token:\n${task.exception?.message}"
            }
        }
    }
}