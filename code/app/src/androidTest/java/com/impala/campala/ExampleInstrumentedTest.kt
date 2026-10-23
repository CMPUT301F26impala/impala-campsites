//package com.impala.campala
//
//import androidx.test.platform.app.InstrumentationRegistry
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.google.android.gms.tasks.Tasks
//import com.google.firebase.firestore.FirebaseFirestore
//import java.util.concurrent.TimeUnit
//
//import org.junit.Test
//import org.junit.runner.RunWith
//
//import org.junit.Assert.*
//
///**
// * Instrumented test, which will execute on an Android device.
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//@RunWith(AndroidJUnit4::class)
//class ExampleInstrumentedTest {
//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.impala.campala", appContext.packageName)
//    }
//
//    @Test
//    fun firestoreEmulatorRoundTrip() {
//        val firestore = FirebaseFirestore.getInstance()
//
//        val document = firestore
//            .collection("emulatorTest")
//            .document("roundTrip")
//
//        Tasks.await(
//            document.set(mapOf("message" to "i hate technology")),
//            10,
//            TimeUnit.SECONDS
//        )
//        val snapshot = Tasks.await(document.get(), 10, TimeUnit.SECONDS)
//
//        assertEquals("i hate technology", snapshot.getString("message"))
//    }
//}