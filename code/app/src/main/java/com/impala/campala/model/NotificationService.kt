package com.impala.campala.model

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.impala.campala.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.uuid.Uuid

class NotificationService : FirebaseMessagingService() {
    override fun onCreate() {
        super.onCreate()
        try {
            val channel =
                NotificationChannel("main", "main", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        } catch (e: Exception) {
            Log.e("NotificationService", "Failed to create notification channel", e)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profile = EntrantProfileRepository.getEntrantProfileById(User.getUID())!!
                profile.notificationId = token
                profile.save().await()
            } catch (e: Exception) {
                Log.e("NotificationService", "Failed to save FCM token on new token", e)
            }
        }
    }

    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profile = EntrantProfileRepository.getEntrantProfileById(User.getUID())!!
                profile.notificationId = installationId
                profile.save().await()
            } catch (e: Exception) {
                Log.e("NotificationService", "Failed to save installation ID on registered", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                show(
                    applicationContext,
                    message.notification?.title ?: "",
                    message.notification?.body ?: "",
                    message.messageId.hashCode()
                )
            } catch (e: Exception) {
                Log.e("NotificationService", "Error showing received notification", e)
            }
        }
    }

    companion object {
        fun show(
            context: Context,
            title: String,
            body: String,
            id: Int = Uuid.random().hashCode()
        ) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
            val notif = NotificationCompat.Builder(context, "main")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_dialog_info)
                .build()
            NotificationManagerCompat.from(context).notify(id, notif)
        }
    }
}
