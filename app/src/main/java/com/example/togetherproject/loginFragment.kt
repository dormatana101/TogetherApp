package com.example.togetherproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.togetherproject.model.AuthRepository


class loginFragment : Fragment() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(param1)
            param2 = it.getString(param2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(com.example.togetherproject.R.layout.fragment_login, container, false)
        val newMemberTextView: TextView = view.findViewById(com.example.togetherproject.R.id.new_member_link)
        newMemberTextView.setOnClickListener {
            (activity as? LoginRegisterActivity)?.handleNewMemberAction(it)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailField = view.findViewById(com.example.togetherproject.R.id.email_field_login)
        passwordField = view.findViewById(com.example.togetherproject.R.id.password_field)
        loginButton = view.findViewById(com.example.togetherproject.R.id.login_button)
        initializeListeners()
    }

    private fun initializeListeners() {
        var server = AuthRepository.authRepository
        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            server.signInUser(email, password) { success, error ->
                if (success) {
                    //Toast.makeText(requireContext(), "Sign in succeeded", Toast.LENGTH_SHORT).show()
                    (activity as? LoginRegisterActivity)?.goToHomeScreen()
                } else {
                    Toast.makeText(requireContext(), error ?: "Sign in unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
