package com.centuryprogrammer18thwasentsingleland.invoice_works


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InvoiceWorksDialogUpgradedFrgVM : ViewModel() {
    private val TAG = InvoiceWorksDialogUpgradedFrgVM::class.java.simpleName

    private val _messageOnFragment = MutableLiveData<String>()
    val messageOnFragment : LiveData<String>
        get() = _messageOnFragment

    private val _closeDialog = MutableLiveData<Boolean>()
    val closeDialog : LiveData<Boolean>
        get() = _closeDialog

}