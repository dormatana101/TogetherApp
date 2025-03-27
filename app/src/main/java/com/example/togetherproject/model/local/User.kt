package com.example.togetherproject.model.local

import android.graphics.Bitmap
import com.example.togetherproject.repository.AuthRepository
import com.example.togetherproject.model.CloudinaryModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserRepository constructor() {
    private val authRepo = AuthRepository.authRepository
    private val firestore = Firebase.firestore
    private val cloudinary = CloudinaryModel()

    companion object {
        val shared = UserRepository()
    }

    fun createUserProfile(name: String, email: String, callback: (Boolean, String?) -> Unit) {
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

    fun retrieveUserData(callback: (Map<String, Any>?) -> Unit) {
        val currentEmail = authRepo.currentUser?.email
        if (currentEmail != null) {
            firestore.collection("users")
                .whereEqualTo("email", currentEmail)
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

    fun retrieveUserDataByEmail(email: String, callback: (Map<String, Any>?) -> Unit) {
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

    fun getProfileImageUrl(callback: (String?) -> Unit) {
        retrieveUserData { user ->
            callback(user?.get("image") as? String)
        }
    }
    fun getProfileImage(onComplete: (String?) -> Unit) {
        retrieveUserData { user ->
            if (user != null) {
                val image = user["image"] as? String
                onComplete(image)
            } else {
                onComplete(null)
            }
        }
    }
    fun modifyUserProfile(name: String, password: String, image: Bitmap?, callback: (Boolean, String?) -> Unit) {
        val currentEmail = authRepo.currentUser?.email
        if (currentEmail != null) {
            firestore.collection("users")
                .whereEqualTo("email", currentEmail)
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

                        fun verifyCompletion() {
                            if (updateCount == totalUpdates && !hasError) {
                                callback(true, null)
                            } else if (hasError) {
                                callback(false, "Profile update encountered an error.")
                            }
                        }

                        if (password.isNotEmpty()) {
                            authRepo.changePassword(password) { success, _ ->
                                if (!success) hasError = true
                                updateCount++
                                verifyCompletion()
                            }
                        }

                        if (name.isNotEmpty()) {
                            documentReference.update("name", name)
                                .addOnSuccessListener {
                                    updateCount++
                                    verifyCompletion()
                                }
                                .addOnFailureListener {
                                    hasError = true
                                    updateCount++
                                    verifyCompletion()
                                }
                        }

                        if (image != null) {
                            val previousImageUrl = userDocument.getString("image")
                            val uploadImage = { bitmap: Bitmap ->
                                cloudinary.sendImageToCloud(bitmap, authRepo.retrieveCurrentUserEmail(), { uri ->
                                    if (!uri.isNullOrBlank()) {
                                        documentReference.update("image", uri)
                                            .addOnSuccessListener {
                                                updateCount++
                                                verifyCompletion()
                                            }
                                            .addOnFailureListener {
                                                hasError = true
                                                updateCount++
                                                verifyCompletion()
                                            }
                                    } else {
                                        hasError = true
                                        updateCount++
                                        verifyCompletion()
                                    }
                                }, {
                                    hasError = true
                                    updateCount++
                                    verifyCompletion()
                                })
                            }

                            if (!previousImageUrl.isNullOrEmpty()) {
                                cloudinary.removeImageFromCloud(previousImageUrl) { deleteSuccess, _ ->
                                    if (deleteSuccess) {
                                        uploadImage(image)
                                    } else {
                                        hasError = true
                                        updateCount++
                                        verifyCompletion()
                                    }
                                }
                            } else {
                                uploadImage(image)
                            }
                        }

                        if (totalUpdates == 0) {
                            callback(false, "No modifications were detected")
                        }
                    } else {
                        callback(false, "User profile not found")
                    }
                }
                .addOnFailureListener { e ->
                    callback(false, e.localizedMessage)
                }
        } else {
            callback(false, "Current user email is missing")
        }
    }
}
