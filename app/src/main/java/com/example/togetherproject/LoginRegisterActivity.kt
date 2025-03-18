package com.example.togetherproject
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.togetherproject.model.AuthRepository


class LoginRegisterActivity : AppCompatActivity() {
    var authServer=AuthRepository.authRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (authServer.isUserLoggedIn()) {
                navigateToHome()
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


    fun onNewMemberClicked(view: View) {
        Log.d("TAG", "This is a debug message")
        // Replace the login fragment with the register fragment

             supportFragmentManager.beginTransaction().apply {
                 replace(R.id.fragment_container, RegisterFragment())
                 commit()
             }


    }
    fun onBackButtonClicked(view: View) {
        // Replace the register fragment with the login fragment
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, loginFragment())
            commit()
        }
    }

    fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

}
