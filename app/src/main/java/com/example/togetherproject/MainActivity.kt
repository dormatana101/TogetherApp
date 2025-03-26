package com.example.togetherproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.togetherproject.model.UserRepository


class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private var profileName: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intent.getStringExtra("user_name")?.let { profileName = it }
        intent.getStringExtra("user_email")?.let { userEmail = it }


        // טעינת הפיד כברירת מחדל
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FeedFragment())
                .commit()
        }

        // טיפול בלחיצה על כפתור חזור
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (currentFragment !is FeedFragment) {
                    handleHomeClick()
                } else {
                    finishAffinity()
                }
            }
        })

        // מאזיני לחיצה על כפתורי הניווט
        findViewById<ImageView>(R.id.home_icon).setOnClickListener { handleHomeClick() }
        findViewById<ImageView>(R.id.map_icon).setOnClickListener { handleArticlesClick() }
        findViewById<ImageView>(R.id.my_posts_icon).setOnClickListener { handleMyPostsClick() }
        findViewById<ImageView>(R.id.profile_icon).setOnClickListener { handleProfileClick() }

        // טעינת נתוני משתמש
        UserRepository.shared.retrieveUserData { user ->
            if (user != null) {
                profileName = user["name"].toString()
                userEmail = user["email"].toString()
            }
        }
    }

    fun retrieveUserName(): String = profileName
    fun retrieveUserEmail(): String = userEmail

    fun updateProfileData() {
        UserRepository.shared.retrieveUserData { user ->
            if (user != null) {
                profileName = user["name"].toString()
                userEmail = user["email"].toString()
            }
        }
    }

    fun handleHomeClick() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FeedFragment())
            .commit()
    }

    fun handleArticlesClick() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, articlesFragment())
            .addToBackStack(null)
            .commit()
    }

    fun handleMyPostsClick() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MyPostsFragment())
            .addToBackStack(null)
            .commit()
    }

    fun handleProfileClick() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, myProfileFragment())
            .addToBackStack(null)
            .commit()
    }

    fun handleEditProfileClick() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, editProfileFragment())
            .addToBackStack(null)
            .commit()
    }

    fun handleAddPostClick(isEdit: Boolean, postId: String?) {
        val fragment = addPostFragment.newInstance(isEdit, postId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun editPost(postId: String?) {
        val fragment = addPostFragment.newInstance(true, postId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun redirectToLogin() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
