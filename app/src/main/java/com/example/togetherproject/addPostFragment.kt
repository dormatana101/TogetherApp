package com.example.togetherproject

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.togetherproject.model.Model
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class addPostFragment : Fragment() {

    private var isEdit: Boolean = false
    private var postId: String? = null

    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isEdit = it.getBoolean("isEdit", false)
            postId = it.getString("postId")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        val profileName = view.findViewById<TextView>(R.id.profileName)
        progressBar = view.findViewById(R.id.profileImageAddPostProgressBar)
        val postText = view.findViewById<EditText>(R.id.addPostEditText)
        val postButton = view.findViewById<TextView>(R.id.postButton)
        val addMediaButton = view.findViewById<TextView>(R.id.addMediaButton)
        val postImage = view.findViewById<ImageView>(R.id.postImage)
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)

        val userServer = com.example.togetherproject.model.UserRepository.shared
        val email = (activity as? MainActivity)?.retrieveUserEmail().toString()
        profileName.text = (activity as? MainActivity)?.retrieveUserName()

        var addedImageToPost = false

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                postImage.setImageBitmap(it)
                addedImageToPost = true
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val resized = resizeBitmap(bitmap, 200, 200)
                    postImage.setImageBitmap(resized)
                    addedImageToPost = true
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        addMediaButton.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            val customTitle = layoutInflater.inflate(R.layout.dialog_title, null)
            AlertDialog.Builder(requireContext())
                .setCustomTitle(customTitle)
                .setItems(options) { _, which ->
                    if (which == 0) cameraLauncher?.launch(null)
                    else galleryLauncher?.launch("image/*")
                }.show()
        }

        progressBar.visibility = View.VISIBLE
        profileImage.visibility = View.GONE
        userServer.getProfileImage { uri ->
            if (uri != null) {
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
            }
            progressBar.visibility = View.GONE
            profileImage.visibility = View.VISIBLE
        }

        if (isEdit && postId != null) {
            postButton.text = "Edit"
            Model.instance.getPostById(postId!!) { post ->
                post?.let {
                    postText.setText(it.content)
                    if (it.imageUrl.isNotEmpty()) {
                        Picasso.get().load(it.imageUrl).into(postImage)
                        addedImageToPost = true
                    }
                }
            }
            postButton.setOnClickListener {
                handleEditPost(postText, postImage, addedImageToPost)
            }
        } else {
            postButton.setOnClickListener {
                handleNewPost(email, postText, postImage, addedImageToPost)
            }
        }

        return view
    }

    private fun handleNewPost(email: String, postText: EditText, postImage: ImageView, addedImage: Boolean) {
        val content = postText.text.toString()
        if (content.isEmpty()) {
            Toast.makeText(context, "Post empty is invalid", Toast.LENGTH_LONG).show()
            return
        }

        if (addedImage) {
            val bitmap = (postImage.drawable as BitmapDrawable).bitmap
            Model.instance.publishPost(email, bitmap, content) { success, error ->
                if (success) {
                    Toast.makeText(context, "Your post was shared successfully!", Toast.LENGTH_LONG).show()
                    (activity as? MainActivity)?.handleHomeClick()
                } else {
                    Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Model.instance.publishPost(email, null, content) { success, error ->
                if (success) {
                    Toast.makeText(context, "Your post was shared successfully!", Toast.LENGTH_LONG).show()
                    (activity as? MainActivity)?.handleHomeClick()
                } else {
                    Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()
                }
            }
        }
        postText.text.clear()
    }

    private fun handleEditPost(postText: EditText, postImage: ImageView, addedImage: Boolean) {
        val content = postText.text.toString()
        if (content.isEmpty()) {
            Toast.makeText(context, "Post empty is invalid", Toast.LENGTH_LONG).show()
            return
        }

        val id = postId ?: return
        if (addedImage) {
            val bitmap = (postImage.drawable as BitmapDrawable).bitmap
            Model.instance.modifyPost(id, bitmap, content) { success, error ->
                if (success) {
                    Toast.makeText(context, "Your post was updated successfully!", Toast.LENGTH_LONG).show()
                    (activity as? MainActivity)?.handleArticlesClick()
                } else {
                    Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Model.instance.modifyPost(id, null, content) { success, error ->
                if (success) {
                    Toast.makeText(context, "Your post was updated successfully!", Toast.LENGTH_LONG).show()
                    (activity as? MainActivity)?.handleArticlesClick()
                } else {
                    Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()
                }
            }
        }
        postText.text.clear()
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxWidth
            newHeight = (maxWidth / aspectRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * aspectRatio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    companion object {
        @JvmStatic
        fun newInstance(isEdit: Boolean, postId: String?) =
            addPostFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isEdit", isEdit)
                    putString("postId", postId)
                }
            }
    }
}
