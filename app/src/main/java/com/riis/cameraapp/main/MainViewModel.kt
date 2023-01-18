package com.riis.cameraapp.main

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.riis.cameraapp.models.DJIResourceManager
import dji.common.error.DJIError
import dji.common.useraccount.UserAccountState
import dji.common.util.CommonCallbacks
import dji.sdk.products.Aircraft
import dji.sdk.useraccount.UserAccountManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MainViewModel : ViewModel() {
    val notifyStatusChanged = DJIResourceManager.instance.connectionStatus.asLiveData()
    val progressToVideo = MutableLiveData(false)
    val promptLogin = MutableLiveData(false)

    var product: Aircraft? = null
        get() {
            field = DJIResourceManager.instance.aircraft

            return field
        }
        private set

    fun openVideoFragment() {
        progressToVideo.value = true
    }

    fun registerDJI(activity: Activity) {
        viewModelScope.launch {
            DJIResourceManager.instance.registerApp(activity)
        }
    }

    fun loginDJIUserAccount(context: Context) {
        UserAccountManager.getInstance().logIntoDJIUserAccount(context,
            object : CommonCallbacks.CompletionCallbackWith<UserAccountState> {
                override fun onSuccess(userAccountState: UserAccountState) {
//                    showToast("login success! Account state is:" + userAccountState.name)
                }

                override fun onFailure(error: DJIError) {
//                    showToast(error.description)
                }
            })
    }
}
