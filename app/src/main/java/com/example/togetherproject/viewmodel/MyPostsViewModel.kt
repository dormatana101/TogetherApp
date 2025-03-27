package com.example.togetherproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.togetherproject.model.Model
import com.example.togetherproject.model.Post
import kotlinx.coroutines.launch

class MyPostsViewModel : ViewModel() {
    val myPostsLiveData = MutableLiveData<List<Post>>()
    val isLoading = MutableLiveData<Boolean>()
    val deleteSuccess = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun loadMyPosts() {
        isLoading.value = true
        viewModelScope.launch {
            Model.instance.retrieveUserPosts { posts ->
                myPostsLiveData.postValue(posts)
                isLoading.postValue(false)
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            Model.instance.deletePost(postId) { success, error ->
                if (success) {
                    deleteSuccess.postValue(true)
                } else {
                    errorMessage.postValue(error ?: "Delete failed")
                    deleteSuccess.postValue(false)
                }
            }
        }
    }
}
