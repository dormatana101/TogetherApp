package com.example.togetherproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.example.togetherproject.model.AuthRepository


class LoginRegisterActivity : AppCompatActivity() {
    var authServer = AuthRepository.authRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (authServer.checkUserLoggedIn()) {
                goToHomeScreen()
                return
            }

            enableEdgeToEdge()
            setContentView(R.layout.activity_login_register)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error in onCreate: ${e.message}")
        }
    }

    fun handleNewMemberAction(view: View) {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigate(R.id.action_login_to_register)
    }

    fun handleBackAction(view: View) {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigate(R.id.action_register_to_login)
    }

    fun goToHomeScreen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
    fun goToHomeScreenWithUser(name: String, email: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("user_name", name)
        intent.putExtra("user_email", email)
        startActivity(intent)
        finish()
    }

}
