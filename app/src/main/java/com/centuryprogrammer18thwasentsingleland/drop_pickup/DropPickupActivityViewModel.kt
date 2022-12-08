package com.centuryprogrammer18thwasentsingleland.drop_pickup

import androidx.lifecycle.ViewModel

// shared ViewModel
// ref) updated https://developer.android.com/topic/libraries/architecture/viewmodel#sharing
// ref) basic https://medium.com/mindorks/how-to-communicate-between-fragments-and-activity-using-viewmodel-ca733233a51c
class DropPickupActivityViewModel : ViewModel(){

    override fun onCleared() {
        super.onCleared()
    }
}