package com.impala.campala

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore

// i got ai to make this cuz setting up this db thing was hell
// (Github Copilot, "Setup the firebase cli to launch when a debug build is made", 2026-10-22)
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