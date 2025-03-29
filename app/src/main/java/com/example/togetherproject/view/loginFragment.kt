package com.example.togetherproject.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.togetherproject.repository.AuthRepository
import com.example.togetherproject.model.local.AppDatabase
import androidx.lifecycle.ViewModelProvider
import com.example.togetherproject.LoginRegisterActivity
import com.example.togetherproject.MainActivity
import com.example.togetherproject.R
import com.example.togetherproject.viewmodel.AuthViewModel


class loginFragment : Fragment() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var viewModel: AuthViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val emailEditText = view.findViewById<EditText>(R.id.email_field_login)
        val passwordEditText = view.findViewById<EditText>(R.id.password_field)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val registerLink = view.findViewById<TextView>(R.id.new_member_link)



        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->

        }


        viewModel.authSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Login success", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.handleHomeClick()
            }
        }


        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }


        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val server = AuthRepository.authRepository
            server.signInUser(email, password) { success, error ->
                if (success) {

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.putExtra("user_name", "User")
                    intent.putExtra("user_email", email)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()

                } else {
                    Toast.makeText(requireContext(), error ?: "Sign in unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
        }



        registerLink.setOnClickListener {
            (activity as? LoginRegisterActivity)?.goToRegisterScreen()

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

                    val db = AppDatabase.getDatabase(requireContext())
                    Thread {
                        val user = db.userDao().getUserByEmail(email)
                        activity?.runOnUiThread {
                            if (user != null) {

                                (activity as? LoginRegisterActivity)?.goToHomeScreenWithUser(user.name, user.email)
                            } else {

                                (activity as? LoginRegisterActivity)?.goToHomeScreen()
                            }
                        }
                    }.start()
                } else {
                    Toast.makeText(requireContext(), error ?: "Sign in unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }
}
