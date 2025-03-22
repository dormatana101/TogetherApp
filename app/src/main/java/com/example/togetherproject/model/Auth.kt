package com.example.togetherproject.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun createAccount(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage)
                }
            }
    }

    fun signInUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, "Incorrect email or password")
                }
            }
    }

    fun logOutUser() {
        firebaseAuth.signOut()
    }

    fun changePassword(newPassword: String, callback: (Boolean, String?) -> Unit) {
        val user = firebaseAuth.currentUser
        user?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage)
                }
            }
    }

    fun checkUserLoggedIn(): Boolean {
        return currentUser != null
    }

    fun retrieveCurrentUserEmail(): String {
        return currentUser?.email.orEmpty()
    }

    companion object {
        val authRepository = AuthRepository()
    }
}
