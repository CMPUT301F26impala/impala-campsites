package com.impala.campala.model

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.impala.campala.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        try {
            val channel = NotificationChannel("main", "main", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        } catch (e: Exception) {
            Log.e("NotificationService", "Failed to create notification channel", e)
        }
    }

    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = User.getUID().ifEmpty { FirebaseAuth.getInstance().currentUser?.uid }
                if (!uid.isNullOrEmpty()) {
                    val profile = EntrantProfileRepository.getEntrantProfileById(uid)
                    if (profile != null) {
                        profile.notificationId = token
                        profile.save().await()
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationService", "Failed to save FCM token on new token", e)
            }
        }
    }

    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = User.getUID().ifEmpty { FirebaseAuth.getInstance().currentUser?.uid }
                if (!uid.isNullOrEmpty()) {
                    val profile = EntrantProfileRepository.getEntrantProfileById(uid)
                    if (profile != null) {
                        profile.notificationId = installationId
                        profile.save().await()
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationService", "Failed to save installation ID on registered", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = User.getUID().ifEmpty { FirebaseAuth.getInstance().currentUser?.uid }
                if (!uid.isNullOrEmpty()) {
                    val profile = EntrantProfileRepository.getEntrantProfileById(uid)
                    if (profile != null && !profile.allowNotification) {
                        return@launch
                    }
                }

                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@launch
                }

                val title = message.notification?.title ?: message.data["title"] ?: "Campala"
                val body = message.notification?.body ?: message.data["body"] ?: message.data["message"] ?: "New notification"

                val notif = NotificationCompat.Builder(applicationContext, "main")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.drawable.ic_dialog_info)
                    .build()

                NotificationManagerCompat.from(applicationContext)
                    .notify(message.messageId?.hashCode() ?: System.currentTimeMillis().toInt(), notif)
            } catch (e: Exception) {
                Log.e("NotificationService", "Error showing received notification", e)
            }
        }
    }
}
