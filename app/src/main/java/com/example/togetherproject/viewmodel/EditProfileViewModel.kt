package com.example.togetherproject.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.togetherproject.model.local.UserRepository
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {
    val isSaving = MutableLiveData<Boolean>()
    val saveSuccess = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun updateProfile(name: String, password: String?, image: Bitmap?) {
        isSaving.value = true

        viewModelScope.launch {
            UserRepository.shared.modifyUserProfile(
                name,
                password ?: "",
                image
            ) { success, error ->
                isSaving.postValue(false)

                if (success) {
                    saveSuccess.postValue(true)
                } else {
                    errorMessage.postValue(error ?: "An error occurred")
                }
            }
        }
    }
}
