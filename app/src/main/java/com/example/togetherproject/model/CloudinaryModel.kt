package com.example.togetherproject.model

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.togetherproject.base.MyApplication
import java.io.File
import java.io.FileOutputStream
import android.content.Context

class CloudinaryModel {

    fun sendImageToCloud(
        bitmap: Bitmap,
        name: String,
        onSuccess: (String?) -> Unit,
        onError: (String?) -> Unit
    ) {
        val context = MyApplication.Globals.context
        if (context == null) {
            onError("Initialization error: context is null – MyApplication not set up?")
            return
        }

        val file = bitmap.toFile(context, name)

        MediaManager.get()
            .upload(file.path)
            .option("folder", "images")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    onSuccess(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError(error?.description ?: "Upload error: unknown issue")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                }
            })
            .dispatch()
    }

    fun removeImageFromCloud(previousImageUrl: String, onComplete: (Boolean, String?) -> Unit) {
        val publicId = getCloudPublicId(previousImageUrl)

        Thread {
            try {
                val result = MediaManager.get()
                    .cloudinary
                    .uploader()
                    .destroy(publicId, emptyMap<String, Any>())

                Handler(Looper.getMainLooper()).post {
                    if (result["result"] == "ok") {
                        onComplete(true, null)
                    } else {
                        onComplete(false, result["result"] as? String)
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    onComplete(false, e.message)
                }
            }
        }.start()
    }

    private fun getCloudPublicId(url: String): String {
        val afterUpload = url.substringAfter("upload/")
        val afterVersion = afterUpload.substringAfter("/")
        return afterVersion.substringBeforeLast(".")
    }

    private fun Bitmap.toFile(context: Context, name: String): File {
        val file = File(context.cacheDir, "image_$name.jpg")
        FileOutputStream(file).use { stream ->
            Log.d("Cloudinary", "Storing bitmap to file: ${file.absolutePath}")
            this.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
        return file
    }
}
