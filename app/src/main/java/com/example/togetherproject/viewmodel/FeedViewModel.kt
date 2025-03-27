package com.example.togetherproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.togetherproject.model.Model
import com.example.togetherproject.model.Post
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {
    val postsLiveData = MutableLiveData<List<Post>>()
    val isLoading = MutableLiveData<Boolean>()

    fun loadPosts() {
        isLoading.value = true

        // נשתמש בקורוטינה כדי לטעון את הנתונים בצורה נקייה
        viewModelScope.launch {
            Model.instance.retrievePosts { posts ->
                postsLiveData.postValue(posts)
                isLoading.postValue(false)
            }
        }
    }
}
