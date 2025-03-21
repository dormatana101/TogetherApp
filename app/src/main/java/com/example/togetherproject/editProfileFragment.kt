package com.example.togetherproject

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.togetherproject.model.UserRepository
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private var cameraLauncher: ActivityResultLauncher<Void?>? = null
private var galleryLauncher: ActivityResultLauncher<String>? = null

class editProfileFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        var editIcon = view.findViewById<ImageView>(R.id.editIcon)
        var profileImage = view.findViewById<ImageView>(R.id.profileImage)
        var addedImageToProfile: Boolean = false
        var saveButton = view.findViewById<Button>(R.id.saveButton)
        var userServer = UserRepository.shared
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap)
                val uri = MediaStore.Images.Media.insertImage(
                    context?.contentResolver,
                    bitmap,
                    null,
                    null
                ).toUri()
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
                addedImageToProfile = true
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                profileImage.setImageURI(uri)
                addedImageToProfile = true
            }
        }

        editIcon.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            val customTitle = layoutInflater.inflate(R.layout.dialog_title, null)
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
        val editProfilePassword = view.findViewById<EditText>(R.id.editProfilePassword)
        val editProfileName = view.findViewById<EditText>(R.id.editProfileName)
        val editProfileConfirmPass = view.findViewById<EditText>(R.id.editProfileConfirmPassword)
        saveButton.setOnClickListener {

            val bitmap: Bitmap?
            if (addedImageToProfile) {
                bitmap = (profileImage.drawable as BitmapDrawable).bitmap
            } else {
                bitmap = null
            }
            if (editProfilePassword.text.toString() != editProfileConfirmPass.text.toString()) {
                Toast.makeText(context, "Mismatch: passwords do not align", Toast.LENGTH_SHORT).show()
            } else if (editProfilePassword.text.toString().length < 6 && editProfilePassword.text.toString().isNotEmpty()) {
                Toast.makeText(context, "Minimum password length is 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                profileImage.isDrawingCacheEnabled = true
                profileImage.buildDrawingCache()
                userServer.modifyUserProfile(
                    editProfileName.text.toString(),
                    editProfilePassword.text.toString(),
                    bitmap
                ) { success, error ->
                    if (success) {
                        Toast.makeText(context, "Profile successfully updated", Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.updateProfileData()
                        (activity as? MainActivity)?.handleProfileClick()
                    } else {
                        Toast.makeText(context, "Update error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        val CancelButtonEditProfile = view.findViewById<Button>(R.id.CancelButtonEditProfile)
        CancelButtonEditProfile.setOnClickListener {
            (activity as? MainActivity)?.handleProfileClick()
        }

        userServer.getProfileImageUrl { uri ->
            if (uri != null) {
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            editProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
