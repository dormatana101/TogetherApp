package com.example.togetherproject.base

import android.app.Application
import android.content.Context
import android.util.Log
import com.cloudinary.android.MediaManager
import com.example.togetherproject.BuildConfig

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Globals.context = applicationContext

        Log.d("MyApplication", "onCreate() called!")

        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUD_NAME,
            "api_key" to BuildConfig.API_KEY,
            "api_secret" to BuildConfig.API_SECRET
        )
        MediaManager.init(this, config)
        Log.d("MyApplication", "Cloudinary initialized with config.")
    }

    object Globals {
        var context: Context? = null
    }
}
