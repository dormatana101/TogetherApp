package com.example.togetherproject.view

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
import com.example.togetherproject.model.local.UserRepository
import com.example.togetherproject.model.local.AppDatabase
import com.example.togetherproject.model.local.UserEntity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import androidx.lifecycle.ViewModelProvider
import com.example.togetherproject.MainActivity
import com.example.togetherproject.R
import com.example.togetherproject.viewmodel.EditProfileViewModel


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private var cameraLauncher: ActivityResultLauncher<Void?>? = null
private var galleryLauncher: ActivityResultLauncher<String>? = null

class editProfileFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel: EditProfileViewModel

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

        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val editIcon = view.findViewById<ImageView>(R.id.editIcon)
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        var addedImageToProfile = false
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val editProfilePassword = view.findViewById<EditText>(R.id.editProfilePassword)
        val editProfileName = view.findViewById<EditText>(R.id.editProfileName)
        val editProfileConfirmPass = view.findViewById<EditText>(R.id.editProfileConfirmPassword)

        // 🟢 אתחול ViewModel
        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]

        // 🟢 תצפית על מצב שמירה
        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            saveButton.isEnabled = !saving
            if (saving) {
                Toast.makeText(requireContext(), "Saving...", Toast.LENGTH_SHORT).show()
            }
        }

        // 🟢 תצפית על הצלחה
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Profile successfully updated", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.updateProfileData()
                (activity as? MainActivity)?.handleProfileClick()
            }
        }

        // 🟢 תצפית על שגיאה
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), "Update error: $it", Toast.LENGTH_SHORT).show()
            }
        }

        // 🖼️ פתיחת מצלמה או גלריה
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
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher?.launch(null)
                    1 -> galleryLauncher?.launch("image/*")
                }
            }
            builder.show()
        }

        // 💾 לחיצה על כפתור שמירה
        saveButton.setOnClickListener {
            val bitmap: Bitmap? = if (addedImageToProfile) {
                (profileImage.drawable as BitmapDrawable).bitmap
            } else null

            val name = editProfileName.text.toString()
            val password = editProfilePassword.text.toString()
            val confirm = editProfileConfirmPass.text.toString()

            if (password != confirm) {
                Toast.makeText(context, "Mismatch: passwords do not align", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6 && password.isNotEmpty()) {
                Toast.makeText(context, "Minimum password length is 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.updateProfile(name, password, bitmap)

                // שמירה גם ב-Room
                val mainActivity = activity as? MainActivity
                val email = mainActivity?.retrieveUserEmail()
                if (email != null) {
                    val db = AppDatabase.getDatabase(requireContext())
                    val userImage = "" // תוכל בעתיד לשלב URL אמיתי
                    val updatedUser = UserEntity(email = email, name = name, image = userImage)
                    Thread {
                        db.userDao().insertUser(updatedUser)
                    }.start()
                }
            }
        }

        val cancelButton = view.findViewById<Button>(R.id.CancelButtonEditProfile)
        cancelButton.setOnClickListener {
            (activity as? MainActivity)?.handleProfileClick()
        }

        // טעינת תמונת פרופיל קיימת
        UserRepository.shared.getProfileImageUrl { uri ->
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
