package com.centuryprogrammer18thwasentsingleland.stripe

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class FirebaseLoginForStripeConnectFrgVM : ViewModel() {
    private val TAG = FirebaseLoginForStripeConnectFrgVM::class.java.simpleName
}