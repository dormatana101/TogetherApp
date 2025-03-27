package com.example.togetherproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.togetherproject.model.local.UserRepository
import com.example.togetherproject.model.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    val name = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String?>()

    fun loadProfile(localEmail: String?, db: AppDatabase) {
        if (localEmail == null) return

        viewModelScope.launch(Dispatchers.IO) {
            val localUser = db.userDao().getUserByEmail(localEmail)
            if (localUser != null) {
                name.postValue(localUser.name)
                email.postValue(localUser.email)
                imageUrl.postValue(localUser.image)
            } else {
                // fallback ל־Firebase
                UserRepository.shared.retrieveUserData { user ->
                    if (user != null) {
                        name.postValue(user["name"] as? String ?: "")
                        email.postValue(user["email"] as? String ?: "")
                        imageUrl.postValue(null) // תמונה תיטען מה־Firebase
                    }
                }
            }
        }
    }
}
