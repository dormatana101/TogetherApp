package com.example.togetherproject.model

import android.graphics.Bitmap
import android.util.Log
import com.example.togetherproject.model.AuthRepository
import com.example.togetherproject.model.CloudinaryModel
import com.example.togetherproject.model.UserRepository
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
        fetchPosts { retrievedPosts ->
            postList = retrievedPosts
        }
    }

    fun createPost(email: String, image: Bitmap?, content: String, callback: (Boolean, String?) -> Unit) {
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
                    Log.d("Firestore", "Image upload started")
                    uploadImage(it, documentReference.id, { uri ->
                        Log.d("Firestore", "Image upload completed")
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
                            Log.d("Firestore", "Image upload failed")
                            callback(false, "Image upload failed")
                        }
                    }, { error ->
                        callback(false, error)
                    })
                } ?: callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding post", e)
                callback(false, e.localizedMessage)
            }
    }

    private fun uploadImage(bitmap: Bitmap, name: String, onSuccess: (String?) -> Unit, onError: (String?) -> Unit) {
        cloudinary.uploadImage(bitmap, name, onSuccess, onError)
    }

    fun fetchPosts(callback: (MutableList<Post>) -> Unit) {
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
                    userRepo.fetchUserByEmail(email) { user ->
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
                            Log.d("Firestore", "Successfully fetched ${posts.size} posts.")
                            callback(posts)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching posts", e)
                callback(mutableListOf())
            }
    }

    fun fetchUserPosts(callback: (MutableList<Post>) -> Unit) {
        val email = authRepo.getCurrentUserEmail()

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
                    userRepo.fetchUserByEmail(email) { user ->
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
                            Log.d("Firestore", "Successfully fetched ${posts.size} posts.")
                            callback(posts)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching posts", e)
                callback(mutableListOf())
            }
    }

    fun fetchPostById(postId: String, callback: (Post?) -> Unit) {
        firestore.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val email = document.getString("email") ?: "email"
                    userRepo.fetchUserByEmail(email) { user ->
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
                Log.e("Firestore", "Error getting post by ID", e)
                callback(null)
            }
    }

    fun updatePost(postId: String, image: Bitmap?, content: String, callback: (Boolean, String?) -> Unit) {
        val post = hashMapOf(
            "content" to content,
            "imageUrl" to ""
        )

        firestore.collection("posts").document(postId)
            .update(post as Map<String, Any>)
            .addOnSuccessListener {
                image?.let {
                    Log.d("Firestore", "Image upload started")
                    uploadImage(it, postId, { uri ->
                        Log.d("Firestore", "Image upload completed")
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
                            Log.d("Firestore", "Image upload failed")
                            callback(false, "Image upload failed")
                        }
                    }, { error ->
                        callback(false, error)
                    })
                } ?: callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating post", e)
                callback(false, e.localizedMessage)
            }
    }

    fun removePost(postId: String, callback: (Boolean, String?) -> Unit) {
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