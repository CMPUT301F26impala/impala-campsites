package com.impala.campala.model

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.tasks.await

class NotificationService {
    fun askPermission(uid: String){
        FirebaseMessaging.getInstance().register().addOnSuccessListener{
            token->

        }
    }
}