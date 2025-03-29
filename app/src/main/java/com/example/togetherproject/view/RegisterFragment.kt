package com.example.togetherproject.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.togetherproject.repository.AuthRepository
import com.example.togetherproject.model.local.UserRepository
import com.example.togetherproject.model.local.AppDatabase
import com.example.togetherproject.model.local.UserEntity
import androidx.lifecycle.ViewModelProvider
import com.example.togetherproject.LoginRegisterActivity
import com.example.togetherproject.MainActivity
import com.example.togetherproject.R
import com.example.togetherproject.viewmodel.AuthViewModel
import androidx.navigation.fragment.findNavController



class RegisterFragment : Fragment() {

    private lateinit var emailField: TextView
    private lateinit var passwordField: TextView
    private lateinit var confirmPasswordField: TextView
    private lateinit var registerButton: Button
    private lateinit var name: TextView
    private lateinit var viewModel: AuthViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val emailEditText = view.findViewById<EditText>(R.id.email_field)
        val passwordEditText = view.findViewById<EditText>(R.id.password_field)
        val confirmEditText = view.findViewById<EditText>(R.id.confirm_password_field)
        val registerButton = view.findViewById<Button>(R.id.Register_button)
        val backButton = view.findViewById<Button>(R.id.back_button)

        backButton.setOnClickListener {
            (activity as? LoginRegisterActivity)?.handleBackAction(it)
        }
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->

        }

        viewModel.authSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Registration success", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_register_to_login)

            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirm = confirmEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(email, password)
        }


        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailField = view.findViewById(R.id.email_field)
        passwordField = view.findViewById(R.id.password_field)
        confirmPasswordField = view.findViewById(R.id.confirm_password_field)
        registerButton = view.findViewById(R.id.Register_button)
        name = view.findViewById(R.id.username_field)

        initializeRegisterListeners()
    }

    private fun initializeRegisterListeners() {
        val authServer = AuthRepository.authRepository
        val userServer = UserRepository.shared

        registerButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()
            val username = name.text.toString()

            if (password != confirmPassword) {
                Toast.makeText(context, "The entered passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authServer.createAccount(email, password) { success, error ->
                if (success) {
                    userServer.createUserProfile(username, email) { success, error ->
                        if (!isAdded) return@createUserProfile
                        if (success) {
                            val db = AppDatabase.getDatabase(requireContext())
                            Thread {
                                db.userDao().insertUser(UserEntity(email = email, name = username, image = ""))
                            }.start()
                            Log.d("TAG", "User saved in Room")
                        } else {
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                        }
                    }

                    (activity as? LoginRegisterActivity)?.goToHomeScreenWithUser(username, email)

                    Log.d("TAG", "Registration completed successfully")
                } else {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
