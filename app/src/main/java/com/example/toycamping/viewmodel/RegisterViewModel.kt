package com.example.toycamping.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.toycamping.base.BaseViewModel
import com.example.toycamping.base.ViewState
import com.example.toycamping.data.repo.FirebaseRepository
import com.example.toycamping.ext.ioScope
import org.koin.java.KoinJavaComponent.inject

class RegisterViewModel(app: Application) : BaseViewModel(app) {

    private val firebaseRepository by inject<FirebaseRepository>(FirebaseRepository::class.java)

    val registerId = MutableLiveData<String>()
    val registerPass = MutableLiveData<String>()

    fun register() {
        checkRegister()?.let { user ->
            ioScope {
                viewStateChanged(RegisterViewState.ShowProgress)
                firebaseRepository.register(user.id, user.pass)
                    .addOnCompleteListener { registerTask ->
                        if (registerTask.isSuccessful) {
                            ioScope {
                                firebaseRepository.createUserBookmarkDB(user.id)
                                    .addOnCompleteListener { bookmarkDBTask ->
                                        if (bookmarkDBTask.isSuccessful) {
                                            ioScope {
                                                firebaseRepository.createUserSnapDB(user.id)
                                                    .addOnCompleteListener { snapDBTask ->
                                                        if (snapDBTask.isSuccessful) {
                                                            viewStateChanged(RegisterViewState.RegisterSuccess)
                                                            viewStateChanged(RegisterViewState.HideProgress)
                                                        } else {
                                                            viewStateChanged(RegisterViewState.RegisterFailure)
                                                            viewStateChanged(RegisterViewState.HideProgress)
                                                        }
                                                    }
                                            }
                                        } else {
                                            viewStateChanged(RegisterViewState.RegisterFailure)
                                            viewStateChanged(RegisterViewState.HideProgress)
                                        }
                                    }
                            }
                        } else {
                            viewStateChanged(RegisterViewState.RegisterFailure)
                            viewStateChanged(RegisterViewState.HideProgress)
                        }
                    }
            }
        }
    }

    private fun checkRegister(): User? {
        return when {
            registerId.value.isNullOrEmpty() -> {
                viewStateChanged(RegisterViewState.EmptyUserId)
                null
            }
            registerPass.value.isNullOrEmpty() -> {
                viewStateChanged(RegisterViewState.EmptyUserPass)
                null
            }
            else -> {
                User(registerId.value!!, registerPass.value!!)
            }
        }
    }

    private data class User(val id: String, val pass: String)

    sealed class RegisterViewState : ViewState {
        object EmptyUserId : RegisterViewState()
        object EmptyUserPass : RegisterViewState()
        object RegisterSuccess : RegisterViewState()
        object RegisterFailure : RegisterViewState()
        object ShowProgress : RegisterViewState()
        object HideProgress : RegisterViewState()
    }

}