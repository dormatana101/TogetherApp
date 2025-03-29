package com.example.togetherproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.togetherproject.model.local.UserRepository
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment



class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar
    private var profileName: String = ""
    private var userEmail: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intent.getStringExtra("user_name")?.let { profileName = it }
        intent.getStringExtra("user_email")?.let { userEmail = it }


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        val authServer = com.example.togetherproject.repository.AuthRepository.authRepository
        if (!authServer.checkUserLoggedIn()) {
            navController.navigate(R.id.loginFragment)
        } else {
            navController.navigate(R.id.feedFragment)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!navController.popBackStack()) {
                    finishAffinity()
                }
            }
        })

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.home_icon).setOnClickListener {
            handleHomeClick()
        }

        findViewById<ImageView>(R.id.profile_icon).setOnClickListener {
            handleProfileClick()
        }

        findViewById<ImageView>(R.id.my_posts_icon).setOnClickListener {
            handleMyPostsClick()
        }

        findViewById<ImageView>(R.id.map_icon).setOnClickListener {
            handleArticlesClick()
        }

        findViewById<ImageView>(R.id.articles_icon).setOnClickListener {
            handleMapClick()
        }

        val userServer = com.example.togetherproject.model.local.UserRepository.shared
        userServer.retrieveUserData { user ->
            if (user != null) {
                profileName = user["name"].toString()
                userEmail = user["email"].toString()
            }
        }
    }



    fun handleMyPostsClick() {
        navController.navigate(R.id.myPostsFragment)
    }

    fun handleMapClick() {
        navController.navigate(R.id.mapFragment)
    }


    fun retrieveUserName(): String {
        return profileName
    }

    fun retrieveUserEmail(): String {
        return userEmail
    }

    fun updateProfileData() {
        var userServer = UserRepository.shared
        userServer.retrieveUserData { user ->
            if (user != null) {
                profileName = user["name"].toString()
                userEmail = user["email"].toString()
            }
        }
    }

    fun handleArticlesClick() {
        navController.navigate(R.id.articlesFragment)
    }

    fun handleAddPostClick(isEdit: Boolean, postId: String?) {
        val bundle = Bundle().apply {
            putBoolean("isEdit", isEdit)
            putString("postId", postId)
        }
        navController.navigate(R.id.addPostFragment, bundle)
    }



    fun handleHomeClick() {
        navController.popBackStack(R.id.feedFragment, false)
        navController.navigate(R.id.feedFragment)
    }


    fun handleProfileClick() {
        navController.navigate(R.id.myProfileFragment)
    }


    fun handleEditProfileClick() {
        navController.navigate(R.id.editProfileFragment)
    }


    fun editPost(postId: String?) {
        val bundle = Bundle().apply {
            putBoolean("isEdit", true)
            putString("postId", postId)
        }
        navController.navigate(R.id.addPostFragment, bundle)
    }

    fun redirectToLogin() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
    fun handleLoginClick() {
        navController.navigate(R.id.loginFragment)
    }

    fun handleRegisterClick() {
        navController.navigate(R.id.registerFragment)
    }




}
