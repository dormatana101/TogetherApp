package com.example.togetherproject.model

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserRepository  constructor() {
    private val authRepo = AuthRepository.authRepository
    private val firestore = Firebase.firestore
    private val cloudinary = CloudinaryModel()

    companion object {
        val shared = UserRepository()
    }

    fun addUser(name: String, email: String, callback: (Boolean, String?) -> Unit) {
        val user = hashMapOf(
            "name" to name,
            "email" to email
        )

        firestore.collection("users")
            .add(user)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.localizedMessage)
            }
    }

    fun fetchUser(callback: (Map<String, Any>?) -> Unit) {
        val userEmail = authRepo.currentUser?.email
        if (userEmail != null) {
            firestore.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDocument = documents.documents[0]
                        callback(userDocument.data)
                    } else {
                        callback(null)
                    }
                }
                .addOnFailureListener {
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

    fun fetchUserByEmail(email: String, callback: (Map<String, Any>?) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDocument = documents.documents[0]
                    callback(userDocument.data)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun fetchProfileImage(callback: (String?) -> Unit) {
        fetchUser { user ->
            callback(user?.get("image") as? String)
        }
    }

    fun updateUser(name: String, password: String, image: Bitmap?, callback: (Boolean, String?) -> Unit) {
        val userEmail = authRepo.currentUser?.email
        if (userEmail != null) {
            firestore.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDocument = documents.documents[0]
                        val documentReference = userDocument.reference
                        var updateCount = 0
                        var totalUpdates = 0
                        var hasError = false

                        if (password.isNotEmpty()) totalUpdates++
                        if (name.isNotEmpty()) totalUpdates++
                        if (image != null) totalUpdates++

                        fun checkCompletion() {
                            if (updateCount == totalUpdates && !hasError) {
                                callback(true, null)
                            } else if (hasError) {
                                callback(false, "An error occurred while updating the profile.")
                            }
                        }

                        if (password.isNotEmpty()) {
                            authRepo.updatePassword(password) { success, _ ->
                                if (!success) hasError = true
                                updateCount++
                                checkCompletion()
                            }
                        }

                        if (name.isNotEmpty()) {
                            documentReference.update("name", name)
                                .addOnSuccessListener {
                                    updateCount++
                                    checkCompletion()
                                }
                                .addOnFailureListener {
                                    hasError = true
                                    updateCount++
                                    checkCompletion()
                                }
                        }

                        if (image != null) {
                            val previousImageUrl = userDocument.getString("image")
                            val uploadImage = { bitmap: Bitmap ->
                                cloudinary.uploadImage(bitmap, authRepo.getCurrentUserEmail(), { uri ->
                                    if (!uri.isNullOrBlank()) {
                                        documentReference.update("image", uri)
                                            .addOnSuccessListener {
                                                updateCount++
                                                checkCompletion()
                                            }
                                            .addOnFailureListener {
                                                hasError = true
                                                updateCount++
                                                checkCompletion()
                                            }
                                    } else {
                                        hasError = true
                                        updateCount++
                                        checkCompletion()
                                    }
                                }, {
                                    hasError = true
                                    updateCount++
                                    checkCompletion()
                                })
                            }

                            if (!previousImageUrl.isNullOrEmpty()) {
                                cloudinary.deleteImage(previousImageUrl) { deleteSuccess, _ ->
                                    if (deleteSuccess) {
                                        uploadImage(image)
                                    } else {
                                        hasError = true
                                        updateCount++
                                        checkCompletion()
                                    }
                                }
                            } else {
                                uploadImage(image)
                            }
                        }

                        if (totalUpdates == 0) {
                            callback(false, "No changes were made")
                        }
                    } else {
                        callback(false, "User not found")
                    }
                }
                .addOnFailureListener { e ->
                    callback(false, e.localizedMessage)
                }
        } else {
            callback(false, "User email is null")
        }
    }
}