package com.example.togetherproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.togetherproject.model.UserRepository

class MainActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private var profileName: String = ""
    private var userEmail: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FeedFragment())
                .commit()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (currentFragment !is FeedFragment) {
                                    handleHomeClick()
                                } else {
                                    finishAffinity()
                                }
            }
        })

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val addPostButton = findViewById<ImageView>(R.id.add_icon)
        addPostButton.setOnClickListener {
            handleAddPostClick(false, null)
        }

        val homePageButton = findViewById<ImageView>(R.id.home_icon)
        homePageButton.setOnClickListener {
            handleHomeClick()
        }

        val profilePageButton = findViewById<ImageView>(R.id.profile_icon)
        profilePageButton.setOnClickListener{
            handleProfileClick()
        }
        val myPostsButton = findViewById<ImageView>(R.id.my_posts_icon)
        myPostsButton.setOnClickListener{
            handleMyPostsClick()
        }

        var userServer = UserRepository.shared
        userServer.retrieveUserData { user ->
            if (user != null) {
                profileName = user["name"].toString()
                userEmail = user["email"].toString()
            }
        }
        val articlesButton = findViewById<ImageView>(R.id.map_icon)
        articlesButton.setOnClickListener{
            handleArticlesClick()
        }
    }

    fun handleMyPostsClick() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, MyPostsFragment())
            addToBackStack(null)
            commit()
        }
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
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, articlesFragment())
            addToBackStack(null)
            commit()
        }
    }

    fun handleAddPostClick(isEdit: Boolean, postId: String?) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, addPostFragment.newInstance(isEdit, postId))
            addToBackStack(null)
            commit()
        }
    }

    fun launchBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    fun handleHomeClick() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, FeedFragment())
            commit()
        }
    }

    fun handleProfileClick() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, myProfileFragment())
            addToBackStack(null)
            commit()
        }
    }

    fun handleEditProfileClick(){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, editProfileFragment())
            commit()
        }
    }

    fun switchFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    fun editPost(postId: String?) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, addPostFragment.newInstance(true, postId))
            addToBackStack(null)
            commit()
        }
    }
    fun redirectToLogin() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
