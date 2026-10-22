package com.impala.campala

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore

class CampalaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            FirebaseFirestore.getInstance().useEmulator("127.0.0.1", 8080)
        }
    }
}