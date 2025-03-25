package com.example.togetherproject

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.togetherproject.model.Model
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

private const val PARAM1 = "param1"
private const val PARAM2 = "param2"
class addPostFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var isEdit: Boolean = false
    private var postId: String? = null


    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(PARAM1)
            param2 = it.getString(PARAM2)
            isEdit= it.getBoolean(ARG_IS_EDIT, false)
            postId= it.getString(ARG_POST_ID)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view= inflater.inflate(com.example.togetherproject.R.layout.fragment_add_post, container, false)
        var profileName=view.findViewById<TextView>(com.example.togetherproject.R.id.profileName)
        progressBar = view.findViewById(com.example.togetherproject.R.id.profileImageAddPostProgressBar)
        var postText=view.findViewById<EditText>(com.example.togetherproject.R.id.addPostEditText)
        var postButton=view.findViewById<TextView>(com.example.togetherproject.R.id.postButton)
        var userServer= com.example.togetherproject.model.UserRepository.Companion.shared
        profileName.text = (activity as? com.example.togetherproject.MainActivity)?.retrieveUserName()
        var email = (activity as? com.example.togetherproject.MainActivity)?.retrieveUserEmail().toString()
        var addMediaButton=view.findViewById<TextView>(com.example.togetherproject.R.id.addMediaButton)
        var postImage=view.findViewById<ImageView>(com.example.togetherproject.R.id.postImage)
        var profileImage=view.findViewById<ImageView>(com.example.togetherproject.R.id.profileImage)
        var addedImageToPost: Boolean = false
        var postServer= Model.instance

        postButton.setOnClickListener {


            var postTextString=postText.text.toString()
            if(postTextString.isNotEmpty()){
                if (addedImageToPost){
                    postImage.isDrawingCacheEnabled = true
                    postImage.buildDrawingCache()
                    val bitmap = (postImage.drawable as BitmapDrawable).bitmap
                    Model.instance.publishPost(email,bitmap ,postTextString){ success, error ->
                        if (success) {
                            postText.text.clear()
                            Toast.makeText(
                                context,
                                "Your post was shared successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                            (activity as? MainActivity)?.handleHomeClick()

                        } else {
                            // Handle the error
                            Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()

                        }
                    }
                    postText.text.clear()
                }
                else{
                    Model.Companion.instance.publishPost(email,null ,postTextString){ success, error ->
                        if (success) {
                            postText.text.clear()
                            Toast.makeText(
                                context,
                                "Your post was shared successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                            (activity as? com.example.togetherproject.MainActivity)?.handleHomeClick()

                        } else {
                            // Handle the error
                            Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()

                        }
                    }
                    postText.text.clear()
                }

            }
            else{
                Toast.makeText(context, "Post empty is invalid", Toast.LENGTH_LONG).show()
            }
        }
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if(bitmap!=null) {
                postImage.setImageBitmap(bitmap)
                addedImageToPost = true
            }
        }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    val maxWidth = 200
                    val maxHeight = 200

                    val resizedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)

                    postImage.setImageBitmap(resizedBitmap)
                    addedImageToPost = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }




            addMediaButton.setOnClickListener(){
            val options = arrayOf("Take Photo", "Choose from Gallery")
            val customTitle = layoutInflater.inflate(com.example.togetherproject.R.layout.dialog_title, null)
            val builder = AlertDialog.Builder(requireContext())
            builder.setCustomTitle(customTitle)
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> cameraLauncher?.launch(null)
                    1 -> galleryLauncher?.launch("image/*")
                }
            }
            builder.show()
        }
        progressBar.visibility = View.VISIBLE
        profileImage.visibility= View.GONE
        userServer.getProfileImage { uri ->
            if (uri != null) {
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
                progressBar.visibility = View.GONE
                profileImage.visibility= View.VISIBLE
            }
            else{
                progressBar.visibility = View.GONE
                profileImage.visibility= View.VISIBLE
            }
        }
        if(isEdit){
            postButton.text = "Edit"
            postServer.getPostById(postId!!){post->
                post?.let {
                    postText.setText(it.content)
                    if(it.imageUrl.isNotEmpty()){
                        Picasso.get().load(it.imageUrl).into(postImage)
                        addedImageToPost = true
                    }
                }

            }
            postButton.setOnClickListener(){
                var postTextString=postText.text.toString()
                if(postTextString.isNotEmpty()){
                    if (addedImageToPost){
                        postImage.isDrawingCacheEnabled = true
                        postImage.buildDrawingCache()
                        val bitmap = (postImage.drawable as BitmapDrawable).bitmap
                        Model.Companion.instance.modifyPost(postId?:"" ,bitmap ,postTextString){ success, error ->
                            if (success) {
                                postText.text.clear()
                                Toast.makeText(
                                    context,
                                    "Your post was updated successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                (activity as? com.example.togetherproject.MainActivity)?.handleArticlesClick()

                            } else {
                                // Handle the error
                                Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()

                            }
                        }
                        postText.text.clear()
                    }
                    else{
                        Model.Companion.instance.modifyPost(postId?:"",null ,postTextString){ success, error ->
                            if (success) {
                                postText.text.clear()
                                Toast.makeText(
                                    context,
                                    "Your post was updated successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                (activity as? com.example.togetherproject.MainActivity)?.handleArticlesClick()

                            } else {
                                // Handle the error
                                Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()

                            }
                        }
                        postText.text.clear()
                    }

                }
                else{
                    Toast.makeText(context, "Post empty is invalid", Toast.LENGTH_LONG).show()
                }

            }


        }

        return view;
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

        private const val ARG_IS_EDIT = "isEdit"
        private const val ARG_POST_ID = "postId"
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(isEdit: Boolean, postId: String?) =
            addPostFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_EDIT, isEdit)
                    putString(ARG_POST_ID, postId)
                }
            }
    }
}