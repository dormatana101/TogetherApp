package com.example.togetherproject.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.togetherproject.model.Model
import kotlinx.coroutines.launch

class AddPostViewModel : ViewModel() {
    val isUploading = MutableLiveData<Boolean>()
    val uploadSuccess = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun createPost(email: String, content: String, imageBitmap: Bitmap?) {
        isUploading.value = true

        viewModelScope.launch {
            Model.instance.publishPost(email, imageBitmap, content) { success, error ->
                isUploading.postValue(false)
                if (success) {
                    uploadSuccess.postValue(true)
                } else {
                    errorMessage.postValue(error ?: "Error while uploading post")
                }
            }
        }
    }
    fun editPost(postId: String, content: String, imageBitmap: Bitmap?) {
        isUploading.value = true

        viewModelScope.launch {
            Model.instance.modifyPost(postId, imageBitmap, content) { success, error ->
                isUploading.postValue(false)
                if (success) {
                    uploadSuccess.postValue(true)
                } else {
                    errorMessage.postValue(error ?: "Error while editing post")
                }
            }
        }
    }


}
