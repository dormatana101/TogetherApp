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
import androidx.navigation.fragment.findNavController
import com.example.togetherproject.model.AuthRepository

class loginFragment : Fragment() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // ⬇️ מעבר למסך הרשמה דרך NavController
        val newMemberTextView: TextView = view.findViewById(R.id.new_member_link)
        newMemberTextView.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailField = view.findViewById(R.id.email_field_login)
        passwordField = view.findViewById(R.id.password_field)
        loginButton = view.findViewById(R.id.login_button)
        initializeListeners()
    }

    private fun initializeListeners() {
        val server = AuthRepository.authRepository
        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            server.signInUser(email, password) { success, error ->
                if (success) {
                    // אם ההתחברות הצליחה – נעבור ל־MainActivity
                    (activity as? LoginRegisterActivity)?.goToHomeScreen()
                } else {
                    Toast.makeText(requireContext(), error ?: "Sign in unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
