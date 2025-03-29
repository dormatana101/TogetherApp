package com.example.togetherproject.view

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
import androidx.lifecycle.ViewModelProvider
import com.example.togetherproject.MainActivity
import com.example.togetherproject.R
import com.example.togetherproject.model.local.UserRepository
import com.example.togetherproject.viewmodel.AddPostViewModel

class addPostFragment : Fragment() {

    private var isEdit: Boolean = false
    private var postId: String? = null
    private lateinit var viewModel: AddPostViewModel

    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private lateinit var progressBar: ProgressBar

    private val args: addPostFragmentArgs by navArgs()
    private var addedImageToPost = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEdit = args.isEdit
        postId = args.postId
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
        val removeImageButton = view.findViewById<ImageView>(R.id.removeImageButton)
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)

        val userServer = UserRepository.shared
        val email = (activity as? MainActivity)?.retrieveUserEmail().toString()
        profileName.text = (activity as? MainActivity)?.retrieveUserName()

        viewModel = ViewModelProvider(this)[AddPostViewModel::class.java]

        viewModel.isUploading.observe(viewLifecycleOwner) { uploading ->
            progressBar.visibility = if (uploading) View.VISIBLE else View.GONE
        }

        viewModel.uploadSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Your post was shared successfully!", Toast.LENGTH_LONG).show()
                (activity as? MainActivity)?.handleHomeClick()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        // === Launchers ===

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                postImage.setImageBitmap(it)
                postImage.visibility = View.VISIBLE
                removeImageButton.visibility = View.VISIBLE
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
                    postImage.visibility = View.VISIBLE
                    removeImageButton.visibility = View.VISIBLE
                    addedImageToPost = true
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // === Remove Image Button ===

        removeImageButton.setOnClickListener {
            postImage.setImageDrawable(null)
            postImage.visibility = View.GONE
            removeImageButton.visibility = View.GONE
            addedImageToPost = false
        }

        // === Add Media Button ===

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

        // === Load Profile Image ===

        progressBar.visibility = View.VISIBLE
        profileImage.visibility = View.GONE
        userServer.getProfileImage { uri ->
            if (uri != null) {
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
            }
            progressBar.visibility = View.GONE
            profileImage.visibility = View.VISIBLE
        }

        // === Edit Mode ===

        if (isEdit && postId != null) {
            postButton.text = "Edit"
            Model.instance.getPostById(postId!!) { post ->
                post?.let {
                    postText.setText(it.content)
                    if (it.imageUrl.isNotEmpty()) {
                        Picasso.get().load(it.imageUrl).into(postImage)
                        postImage.visibility = View.VISIBLE
                        removeImageButton.visibility = View.VISIBLE
                        addedImageToPost = true
                    }
                }
            }

            postButton.setOnClickListener {
                handleEditPost(postText, postImage, addedImageToPost)
            }

        } else {
            // === New Post ===
            postButton.setOnClickListener {
                val content = postText.text.toString()
                if (content.isEmpty()) {
                    Toast.makeText(context, "Post empty is invalid", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val bitmap = if (addedImageToPost) {
                    (postImage.drawable as BitmapDrawable).bitmap
                } else null

                viewModel.createPost(email, content, bitmap)
                postText.text.clear()
            }
        }

        return view
    }

    private fun handleEditPost(postText: EditText, postImage: ImageView, addedImage: Boolean) {
        val content = postText.text.toString()
        if (content.isEmpty()) {
            Toast.makeText(context, "Post empty is invalid", Toast.LENGTH_LONG).show()
            return
        }

        val id = postId ?: return
        val bitmap = if (addedImage) {
            (postImage.drawable as BitmapDrawable).bitmap
        } else null

        viewModel.editPost(id, content, bitmap)
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
}
