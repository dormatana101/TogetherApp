package com.example.togetherproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, loginFragment())
                commit()
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error in onCreate: ${e.message}")
        }
    }

    fun handleNewMemberAction(view: View) {
        Log.d("TAG", "Debug: switching to registration view")
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, RegisterFragment())
            commit()
        }
    }
    fun handleBackAction(view: View) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, loginFragment())
            commit()
        }
    }

    fun goToHomeScreen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
