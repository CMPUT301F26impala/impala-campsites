package com.impala.campala

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore

// i got ai to make this cuz setting up this db thing was hell
// pretty bad name, but it looks like some kind of base layer
// for the app so maybe that works
class CampalaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            FirebaseFirestore.getInstance().useEmulator("127.0.0.1", 8080)
        }
    }
}