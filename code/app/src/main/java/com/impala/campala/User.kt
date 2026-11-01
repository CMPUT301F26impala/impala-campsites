package com.impala.campala


import kotlinx.coroutines.tasks.await
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import android.util.Log // Recommended for keeping track of bugs and flags and such. Click "Logcat" in the bottom left of Android Studio to view logs. I have placed logs throughout this class for debugging purposes.
import com.google.firebase.Timestamp // Useful for figuring out when a user profile was created
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.tasks.await

/**
 * The user object stores what view mode the user is in (for the frontend to display the right UI)
 */
enum class UserViewMode { ENTRANT, CAMPSITE_OWNER, ADMIN }

//TODO: Documentation
/**
 * REMEMBER TO CALL initialize()!
 * Represents a user of the site, their permissions, their name (if they set one), and their currently selected role (Owner, Admin, Entrant).
 * Details of the Entrant Profile or Campsite Owner Profile are contained in their own classes and pointed to by User's ID
 *
 * @property uid The unique ID for this user.
 * @property isAdmin Does the user have admin permissions? True on first sign in, and potentially after.
 * @property isOwner Does the user have owner permissions? True on first sign in, and potentially after.
 * @property isEntrant Does the user have entrant permissions? Almost always, yes.
 * @property hasCreatedEntrantProfile A boolean for if this user has ever made an entrant profile? Some users only ever act as campsite owners and so this would be false.
 * @property hasActiveEntrantProfile Is this user's entrant profile still usable? False if it was never made, or if it got banned/deleted.
 * @property hasCreatedCampsiteOwnerProfile A boolean for if this user has ever made a campsite owner profile? Some users only ever act as entrants and so this would be false.
 * @property hasActiveCampsiteOwnerProfile Is this user's campsite owner profile still usable? False if it was never made, or if it got banned/deleted.
 * @property currentRole Which view the app should open in on start. Null if they haven't picked one yet.
 */

open class User() {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore // TODO: figure out if this and auth can be sourced from outside the user class. It feels... dumb, having them inside.

    private var uid : String = "" // TODO: See if we can pass this directly instead of having it be an argument for each blithering function
    private var isAdmin = true      // TODO: On first sign-in, these are all true, but once first role is picked, then some may become false depending on which role is selected. Otherwise, these booleans should be figured out during initialization, once the UID is nabbed.
    private var isOwner = true
    private var isEntrant = true
    private var userName : String? = null
    private var hasCreatedEntrantProfile = false
    private var hasActiveEntrantProfile = false
    private var hasCreatedCampsiteOwnerProfile = false
    private var hasActiveCampsiteOwnerProfile = false
    private var currentRole : UserViewMode? = null
    private val TAG = "User" // TODO: Looks like this is just for error reporting but I should clarify with Liam

    public suspend fun initialize() : User {

        var currentUser = auth.currentUser
        var intermediateUID : String? = null // Nullable so that it takes the beating that we don't want uid to take.

        // This makes sure that, if you start up and you've already authenticated, but you did on a different server (eg: the firebase emulator has been reset since last time)
        // It will log you out and re-log you back in
        if (currentUser != null) {
            try {
                currentUser.getIdToken(true).await()
            }
            catch (e: FirebaseNetworkException) {
                // A network error does not mean the user wasn't found, it just means no network
            }
            catch (e: FirebaseException) {
                Log.w(TAG, "Saved user no longer exists in the auth emulator, signing in again.", e)
                auth.signOut()
                currentUser = null
            }
        }

        if (currentUser == null) { // Note: currentUser is nullable by default

            var authResult : AuthResult

            try {
                authResult = auth.signInAnonymously().await()
            }
            catch (e: Exception) {
                    Log.e(TAG, "Authentication failed. " +
                            "Error will throw. " +
                            "Please display visual indicator!")
                throw(e)
            }

            intermediateUID = authResult.user?.uid

            if (intermediateUID == null) {
                Log.wtf(TAG, "Authentication failed (Server returned null uid... uncommon occurrence), " + // Haha. I like that you can use "wtf" for a school project.
                        "error will throw immediately. " +                                                       // For a "wtf" message, it's essentially just a critical/rare error.
                        "Please find out why this happened and fix it!")
                throw IllegalStateException("UserSession initialization failed: UID cannot be null. (auth passed null UID)")
            }

            val exists = checkServerForUser(intermediateUID) // Check the server for the user.
            if (exists) { //NOTE: exists is nullable, but if it's null, we have bigger problems to deal with. (It will almost never be null)
                updateLocalDetails(intermediateUID) // Nab their details from the server if they are an existing user.
            }
            else {
                updateUserProfile(intermediateUID) // Create this user if their UID hasn't been registered yet.
            }// NOTE: don't give user set values for role booleans just yet. We will use getters and setters to set stuff during login/signup.

        }
        else { // If they are already logged in, then who cares? Just grab their info from the server for local use.
            intermediateUID = currentUser.uid
            updateLocalDetails(intermediateUID)
        }

        uid = intermediateUID

        return this
    }


    private suspend fun checkServerForUser(uid: String) : Boolean {

        val foundUser = db.collection("users").document(uid).get().await()

        return foundUser.exists()
    }

    suspend fun updateLocalDetails(uid: String) {
        val document = db.collection("users").document(uid).get().await()

        require(document.exists()) {
            "Error: User details unable to be updated by the server.".also{ message ->
                Log.e(TAG, message)}
        }

        setIsAdmin(document.getBoolean("isAdmin") == true)
        setIsOwner(document.getBoolean("isOwner") == true)
        setIsEntrant(document.getBoolean("isEntrant") == true)
        setuserName(document.getString("userName"))
        setHasCreatedEntrantProfile(document.getBoolean("hasCreatedEntrantProfile") == true)
        setHasActiveEntrantProfile(document.getBoolean("hasActiveEntrantProfile") == true)
        setHasCreatedCampsiteOwnerProfile(document.getBoolean("hasCreatedCampsiteOwnerProfile") == true)
        setHasActiveCampsiteOwnerProfile(document.getBoolean("hasActiveCampsiteOwnerProfile") == true)
        setCurrentRole(UserViewMode.entries.find { it.name == document.getString("currentRole") })
    }


    public fun updateUserProfile(uid: String) { // In a real project this function should be private for security purposes. But this is aight for now
        val userData = mutableMapOf(
            "isAdmin" to isAdmin,
            "isOwner" to isOwner,
            "isEntrant" to isEntrant,
            "userName" to userName,
            "hasCreatedEntrantProfile" to hasCreatedEntrantProfile,
            "hasActiveEntrantProfile" to hasActiveEntrantProfile,
            "hasCreatedCampsiteOwnerProfile" to hasCreatedCampsiteOwnerProfile,
            "hasActiveCampsiteOwnerProfile" to hasActiveCampsiteOwnerProfile,
            "currentRole" to currentRole?.name
        )

        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully created brand new user profile in Firestore.")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to write profile data to Firestore: ${exception.message}")
            }
    }

    //TODO: First sign-in function that gives permissions depending on what role was selected.

    // ~~~ Getters and setters ~~~

    fun getIsAdmin(): Boolean = isAdmin
    fun setIsAdmin(value: Boolean) {
        isAdmin = value
    }

    fun getIsOwner(): Boolean = isOwner
    fun setIsOwner(value: Boolean) {
        isOwner = value
    }

    fun getIsEntrant(): Boolean = isEntrant
    fun setIsEntrant(value: Boolean) {
        isEntrant = value
    }

    fun getuserName(): String? = userName
    fun setuserName(value: String?) {
        userName = value
    }

    fun getHasCreatedEntrantProfile(): Boolean = hasCreatedEntrantProfile
    fun setHasCreatedEntrantProfile(value: Boolean) {
        hasCreatedEntrantProfile = value
    }

    fun getHasActiveEntrantProfile(): Boolean = hasActiveEntrantProfile
    fun setHasActiveEntrantProfile(value: Boolean) {
        hasActiveEntrantProfile = value
    }

    fun getHasCreatedCampsiteOwnerProfile(): Boolean = hasCreatedCampsiteOwnerProfile
    fun setHasCreatedCampsiteOwnerProfile(value: Boolean) {
        hasCreatedCampsiteOwnerProfile = value
    }

    fun getHasActiveCampsiteOwnerProfile(): Boolean = hasActiveCampsiteOwnerProfile
    fun setHasActiveCampsiteOwnerProfile(value: Boolean) {
        hasActiveCampsiteOwnerProfile = value
    }

    fun getCurrentRole(): UserViewMode? = currentRole
    fun setCurrentRole(value: UserViewMode?) {
        currentRole = value
    }

    fun getUID(): String = uid
    private fun setUID(value: String?) {
        println("Nope! Not how that works. BUT... if you wanna do this for testing purposes, then sure, change uid to a var, but be careful.")
    }
}