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
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class myProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var progressBar: ProgressBar
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
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)
        val editButton: TextView = view.findViewById(R.id.editButton)
        editButton.setOnClickListener {
            (activity as? MainActivity)?.handleEditProfileClick()
        }

        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        progressBar = view.findViewById(R.id.profileImageProgressBar)
        val userServer = UserRepository.shared
        progressBar.visibility = View.VISIBLE
        profileImage.visibility = View.GONE
        userServer.getProfileImageUrl { uri ->
            if (uri != null) {
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
                progressBar.visibility = View.GONE
                profileImage.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
                profileImage.visibility = View.VISIBLE
            }
        }

        val logOutbtn = view.findViewById<Button>(R.id.logoutButton)
        val authServer = AuthRepository.authRepository
        logOutbtn.setOnClickListener {
            authServer.logOutUser()
            //Toast.makeText(context, "Logout successful", Toast.LENGTH_LONG).show()
            (activity as? MainActivity)?.redirectToLogin()
        }
        val profileNameText = view.findViewById<TextView>(R.id.profileName)
        val profileEmailText = view.findViewById<TextView>(R.id.profileEmail)
        profileNameText.text = (activity as? MainActivity)?.retrieveUserName()
        profileEmailText.text = (activity as? MainActivity)?.retrieveUserEmail()

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            myProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
