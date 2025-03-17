package com.example.togetherproject.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository private constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

     val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun registerUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage)
                }
            }
    }

    fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, "Password or email is incorrect")
                }
            }
    }

    fun signOutUser() {
        firebaseAuth.signOut()
    }

    fun updatePassword(newPassword: String, callback: (Boolean, String?) -> Unit) {
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

    fun isUserLoggedIn(): Boolean {
        return currentUser != null
    }

    fun getCurrentUserEmail(): String {
        return currentUser?.email.orEmpty()
    }

    companion object {
        val authRepository =AuthRepository()
    }
}