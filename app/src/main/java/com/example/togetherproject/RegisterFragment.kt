package com.example.togetherproject

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.togetherproject.model.AuthRepository
import com.example.togetherproject.model.UserRepository


private const val Parameter_1 = "param1"
private const val Parameter_2 = "param2"


class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var emailField: TextView
    private lateinit var passwordField: TextView
    private lateinit var confirmPasswordField: TextView
    private lateinit var registerButton: Button
    private lateinit var name: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(Parameter_1)
            param2 = it.getString(Parameter_2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val backButton: Button = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            // Call the activity's method to replace the fragment
            (activity as? LoginRegisterActivity)?.onBackButtonClicked(it)
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

        setupListeners()

    }

    private fun setupListeners() {
        var authServer=AuthRepository.authRepository
        var userServer=UserRepository.shared
        registerButton.setOnClickListener {

            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()
            val username = name.text.toString()
            if (password != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authServer.registerUser(email, password) { success, error ->
                if (success) {
                    userServer.addUser(username,email){success,error->
                        if (success) {
                            Log.d("TAG", "User added to database")
                        } else {
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                    (activity as? LoginRegisterActivity)?.navigateToHome()
                    Log.d("TAG", "User registered successfully ")
                } else {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(Parameter_1, param1)
                    putString(Parameter_2, param2)
                }
            }
    }
}