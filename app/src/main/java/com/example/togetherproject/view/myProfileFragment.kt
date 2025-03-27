package com.example.togetherproject.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.togetherproject.MainActivity
import com.example.togetherproject.R
import com.example.togetherproject.repository.AuthRepository
import com.example.togetherproject.model.local.UserRepository
import com.example.togetherproject.model.local.AppDatabase
import com.example.togetherproject.viewmodel.ProfileViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation


class myProfileFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var profileNameText: TextView
    private lateinit var profileEmailText: TextView
    private lateinit var viewModel: ProfileViewModel


    @SuppressLint("CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)

        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        val editButton: FloatingActionButton = view.findViewById(R.id.editButton)
        val logoutButton: Button = view.findViewById(R.id.logoutButton)
        progressBar = view.findViewById(R.id.profileImageProgressBar)
        profileNameText = view.findViewById(R.id.profileName)
        profileEmailText = view.findViewById(R.id.profileEmail)

        // 🟦 אתחול ViewModel
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // 🟦 תצפית על שם
        viewModel.name.observe(viewLifecycleOwner) {
            profileNameText.text = it
        }

        // 🟦 תצפית על אימייל
        viewModel.email.observe(viewLifecycleOwner) {
            profileEmailText.text = it
        }

        // 🟦 תצפית על תמונה
        viewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Picasso.get().load(url)
                    .transform(CropCircleTransformation())
                    .into(profileImage)
            } else {
                // טען מ־Firebase אם אין URL מקומי
                UserRepository.shared.getProfileImageUrl { uri ->
                    if (uri != null) {
                        Picasso.get().load(uri)
                            .transform(CropCircleTransformation())
                            .into(profileImage)
                    }
                }
            }

            progressBar.visibility = View.GONE
            profileImage.visibility = View.VISIBLE
        }

        // 🟦 טעינת פרופיל מה־ViewModel
        val mainActivity = activity as? MainActivity
        val userEmail = mainActivity?.retrieveUserEmail()
        if (userEmail != null) {
            val db = AppDatabase.getDatabase(requireContext())
            viewModel.loadProfile(userEmail, db)
        }

        // 🔄 כפתור עריכה
        editButton.setOnClickListener {
            (activity as? MainActivity)?.handleEditProfileClick()
        }

        // 🔐 כפתור יציאה
        logoutButton.setOnClickListener {
            AuthRepository.authRepository.logOutUser()
            Toast.makeText(context, "You logged out, have a great day", Toast.LENGTH_LONG).show()
            (activity as? MainActivity)?.redirectToLogin()
        }

        return view
    }

}
