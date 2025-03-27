package com.example.togetherproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.togetherproject.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    val isLoading = MutableLiveData<Boolean>()
    val authSuccess = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun login(email: String, password: String) {
        isLoading.value = true

        viewModelScope.launch {
            AuthRepository.authRepository.signInUser(email, password) { success, error ->
                isLoading.postValue(false)
                if (success) {
                    authSuccess.postValue(true)
                } else {
                    errorMessage.postValue(error ?: "Login failed")
                }
            }
        }
    }

    fun register(email: String, password: String) {
        isLoading.value = true

        viewModelScope.launch {
            AuthRepository.authRepository.createAccount(email, password) { success, error ->

            isLoading.postValue(false)
                if (success) {
                    authSuccess.postValue(true)
                } else {
                    errorMessage.postValue(error ?: "Registration failed")
                }
            }
        }
    }
}
