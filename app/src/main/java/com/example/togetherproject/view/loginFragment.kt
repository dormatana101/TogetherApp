package com.example.togetherproject.view

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


        // 🟢 אתחול ViewModel
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // 🟢 תצפית על טעינה
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            // בעתיד תוכל להוסיף ProgressBar
        }

        // 🟢 תצפית על הצלחה
        viewModel.authSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Login success", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.handleHomeClick()
            }
        }

        // 🟢 תצפית על שגיאה
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // 🟢 התחברות
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        // מעבר לרישום
        registerLink.setOnClickListener {
            (activity as? MainActivity)?.handleRegisterClick()

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
                    // ⬇ טען את המשתמש מ־Room ושמור ל־MainActivity
                    val db = AppDatabase.getDatabase(requireContext())
                    Thread {
                        val user = db.userDao().getUserByEmail(email)
                        activity?.runOnUiThread {
                            if (user != null) {
                                // שמור ב־MainActivity
                                (activity as? LoginRegisterActivity)?.goToHomeScreenWithUser(user.name, user.email)
                            } else {
                                // fallback – כניסה רגילה
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
