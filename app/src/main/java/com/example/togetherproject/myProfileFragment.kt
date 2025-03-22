package com.example.togetherproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.togetherproject.model.AuthRepository
import com.example.togetherproject.model.UserRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class myProfileFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var profileNameText: TextView
    private lateinit var profileEmailText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)
        val editButton: FloatingActionButton = view.findViewById(R.id.editButton)
        editButton.setOnClickListener {
            (activity as? MainActivity)?.handleEditProfileClick()
        }

        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        progressBar = view.findViewById(R.id.profileImageProgressBar)
        profileNameText = view.findViewById(R.id.profileName)
        profileEmailText = view.findViewById(R.id.profileEmail)

        UserRepository.shared.getProfileImageUrl { uri ->
            if (uri != null) {
                Picasso.get().load(uri)
                    .transform(CropCircleTransformation())
                    .into(profileImage)
            }
            progressBar.visibility = View.GONE
            profileImage.visibility = View.VISIBLE
        }

        val logOutbtn=view.findViewById<Button>(R.id.logoutButton)
        val authServer=AuthRepository.authRepository
        logOutbtn.setOnClickListener {
            authServer.logOutUser()
            Toast.makeText(context,"You logged out, have a great day", Toast.LENGTH_LONG).show()
            (activity as? MainActivity)?.redirectToLogin()
        }
        val profileName = view.findViewById<TextView>(R.id.profileName)
        val profileEmail=view.findViewById<TextView>(R.id.profileEmail)
        UserRepository.shared.retrieveUserData { user ->
            if (user != null) {
                profileName.text = user["name"].toString()
                profileEmail.text = user["email"].toString()
            }
        }


        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            myProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("param1", param1)
                    putString("param2", param2)
                }
            }
    }
}
