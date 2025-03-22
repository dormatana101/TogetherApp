package com.example.togetherproject.model

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class Post(
    val id: String,
    val name: String,
    var profileImage: String,
    var content: String,
    var imageUrl: String,
    var timestamp: java.util.Date,
)


class Model private constructor() {
    private val firestore = Firebase.firestore
    @Volatile
    var postList: MutableList<Post> = mutableListOf()
    private val userRepo = UserRepository.shared
    private val cloudinary = CloudinaryModel()
    private val authRepo = AuthRepository.authRepository

    companion object {
        val instance = Model()
    }

    init {
        retrievePosts { retrievedPosts ->
            postList = retrievedPosts
        }
    }

    fun publishPost(email: String, image: Bitmap?, content: String, callback: (Boolean, String?) -> Unit) {
        val post = hashMapOf(
            "email" to email,
            "content" to content,
            "timestamp" to System.currentTimeMillis(),
            "imageUrl" to ""
        )

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener { documentReference ->
                image?.let {
                    Log.d("Firestore", "Image transmission initiated")
                    transmitImage(it, documentReference.id, { uri ->
                        Log.d("Firestore", "Image transmission successful")
                        if (!uri.isNullOrBlank()) {
                            firestore.collection("posts").document(documentReference.id)
                                .update("imageUrl", uri)
                                .addOnSuccessListener {
                                    callback(true, null)
                                }
                                .addOnFailureListener { e ->
                                    callback(false, e.localizedMessage)
                                }
                        } else {
                            Log.d("Firestore", "Image transmission failed")
                            callback(false, "Image transmission failed")
                        }
                    }, { error ->
                        callback(false, error)
                    })
                } ?: callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error while publishing post", e)
                callback(false, e.localizedMessage)
            }
    }

    private fun transmitImage(bitmap: Bitmap, name: String, onSuccess: (String?) -> Unit, onError: (String?) -> Unit) {
        cloudinary.sendImageToCloud(bitmap, name, onSuccess, onError)
    }

    fun retrievePosts(callback: (MutableList<Post>) -> Unit) {
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val posts: MutableList<Post> = mutableListOf()
                var pendingCallbacks = documents.size()

                if (pendingCallbacks == 0) {
                    callback(posts)
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    val email = document.getString("email") ?: "email"
                    userRepo.retrieveUserDataByEmail(email) { user ->
                        val post = Post(
                            id = document.id,
                            name = user?.get("name") as? String ?: "name",
                            profileImage = user?.get("image") as? String ?: "image",
                            content = document.getString("content") ?: "content",
                            imageUrl = document.getString("imageUrl") ?: "",
                            timestamp = java.util.Date(document.getLong("timestamp") ?: 0)
                        )
                        posts.add(post)
                        pendingCallbacks--

                        if (pendingCallbacks == 0) {
                            posts.sortByDescending { it.timestamp }
                            Log.d("Firestore", "Successfully retrieved ${posts.size} posts.")
                            callback(posts)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error retrieving posts", e)
                callback(mutableListOf())
            }
    }

    fun retrieveUserPosts(callback: (MutableList<Post>) -> Unit) {
        val email = authRepo.retrieveCurrentUserEmail()

        firestore.collection("posts")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                val posts: MutableList<Post> = mutableListOf()
                var pendingCallbacks = documents.size()

                if (pendingCallbacks == 0) {
                    callback(posts)
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    userRepo.retrieveUserDataByEmail(email) { user ->
                        val post = Post(
                            id = document.id,
                            name = user?.get("name") as? String ?: "name",
                            profileImage = user?.get("image") as? String ?: "image",
                            content = document.getString("content") ?: "content",
                            imageUrl = document.getString("imageUrl") ?: "",
                            timestamp = java.util.Date(document.getLong("timestamp") ?: 0)
                        )
                        posts.add(post)
                        pendingCallbacks--

                        if (pendingCallbacks == 0) {
                            posts.sortByDescending { it.timestamp }
                            Log.d("Firestore", "Successfully retrieved ${posts.size} posts.")
                            callback(posts)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error retrieving posts", e)
                callback(mutableListOf())
            }
    }

    fun getPostById(postId: String, callback: (Post?) -> Unit) {
        firestore.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val email = document.getString("email") ?: "email"
                    userRepo.retrieveUserDataByEmail(email) { user ->
                        val post = Post(
                            id = document.id,
                            name = user?.get("name") as? String ?: "name",
                            profileImage = user?.get("image") as? String ?: "image",
                            content = document.getString("content") ?: "content",
                            imageUrl = document.getString("imageUrl") ?: "",
                            timestamp = java.util.Date(document.getLong("timestamp") ?: 0)
                        )
                        callback(post)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error retrieving post by ID", e)
                callback(null)
            }
    }

    fun modifyPost(postId: String, image: Bitmap?, content: String, callback: (Boolean, String?) -> Unit) {
        val post = hashMapOf(
            "content" to content,
            "imageUrl" to ""
        )

        firestore.collection("posts").document(postId)
            .update(post as Map<String, Any>)
            .addOnSuccessListener {
                image?.let {
                    Log.d("Firestore", "Image transmission initiated")
                    transmitImage(it, postId, { uri ->
                        Log.d("Firestore", "Image transmission successful")
                        if (!uri.isNullOrBlank()) {
                            firestore.collection("posts").document(postId)
                                .update("imageUrl", uri)
                                .addOnSuccessListener {
                                    callback(true, null)
                                }
                                .addOnFailureListener { e ->
                                    callback(false, e.localizedMessage)
                                }
                        } else {
                            Log.d("Firestore", "Image transmission failed")
                            callback(false, "Image transmission failed")
                        }
                    }, { error ->
                        callback(false, error)
                    })
                } ?: callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error modifying post", e)
                callback(false, e.localizedMessage)
            }
    }

    fun deletePost(postId: String, callback: (Boolean, String?) -> Unit) {
        firestore.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting post", e)
                callback(false, e.localizedMessage)
            }
    }
}
