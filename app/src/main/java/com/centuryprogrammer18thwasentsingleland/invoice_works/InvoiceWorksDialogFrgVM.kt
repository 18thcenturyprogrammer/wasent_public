package com.centuryprogrammer18thwasentsingleland.invoice_works


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InvoiceWorksDialogFrgVM : ViewModel() {
    private val TAG = InvoiceWorksDialogFrgVM::class.java.simpleName

    private val _messageOnFragment = MutableLiveData<String>()
    val messageOnFragment : LiveData<String>
        get() = _messageOnFragment

    private val _closeDialog = MutableLiveData<Boolean>()
    val closeDialog : LiveData<Boolean>
        get() = _closeDialog


}