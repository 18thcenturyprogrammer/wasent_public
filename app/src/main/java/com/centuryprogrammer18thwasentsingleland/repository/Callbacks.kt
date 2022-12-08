package com.centuryprogrammer18thwasentsingleland.repository

import android.view.View
import com.centuryprogrammer18thwasentsingleland.data.CleanerInitSetting
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.DetailBaseItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

interface AddCleanerInitSettingCallbacks {
    fun onAddCleanerInitSettingCall(isSuccess: Boolean, cleanerInitSetting:CleanerInitSetting)
}

interface AddCustomerCallbacks {
    fun onAddCustomerCall(isSuccess: Boolean, customer:Customer)
}

interface UpdateAllItemsCallback {
    fun onUpdateAllItemsCall(isSuccess: Boolean)
}

interface UpdateDetailItemCallback{
    fun onUpdateDetailItemCall(isSuccess: Boolean)
}

interface DeleteItemCallback{
    fun onDeleteItemCallback(isSuccess: Boolean)
}

interface DeleteDetailItemCallback{
    fun onDeleteDetailItemCallback(isSuccess: Boolean)
}

interface DeleteDetailBaseItemCallback{
    fun onDeleteDetailBaseItemCallback(isSuccess: Boolean)
}

interface SingleValueEventItemCallback{
    fun onSingleEventOnItemCall(snapshot: DataSnapshot?, error : DatabaseError?)
}

interface childEventOnBasicItemCallBack{

}

interface childEventOnItemCallback{
    fun onChildEventOnItemCall(snapshot: DataSnapshot?, error : DatabaseError?, whatHappend:String)
}

interface ItemViewholderLongClickCallback{
    fun onItemViewholderLongClick(view: View)
}

interface DetailItemViewholderLongClickCallback{
    fun onDetailItemViewholderLongClick(view: View)
}

interface DetailBaseItemViewholderLongClickCallback{
    fun onDetailBaseItemViewholderLongClick(detailBaseItem:DetailBaseItem)
}

interface RemoveItemCallback{
    fun onDeleteItemCallback(isSucceed:Boolean)
}

interface childEventOnBaseItemCallback{
    fun onChildEventOnBaseItemCall(snapshot: DataSnapshot?, error : DatabaseError?, whatHappend:String)
}

interface ValueEventOnItemCallback{
    fun onValueEventOnItemCallback(snapshot: DataSnapshot?, error : DatabaseError?)
}

interface ValueEventOnBaseItemCallback{
    fun onValueEventOnBaseItemCallback(snapshot: DataSnapshot?, error : DatabaseError?)
}

interface ValueEventOnPartialBaseItemCallback{
    fun onValueEventOnPartialBaseItemCallback(snapshot: DataSnapshot?, error : DatabaseError?)
}

interface ValueEventOnPartialBaseItemByPushKeyCallback{
    fun onValueEventOnPartialBaseItemByPushKeyCallback(snapshot: DataSnapshot?, error : DatabaseError?, typeClean: String)
}


interface ValueEventOnDetailItemCallback{
    fun onValueEventOnDetailItemCallback(snapshot: DataSnapshot?, error : DatabaseError?)
}




interface ValueEventOnItemsCallback{
    fun onValueEventOnItemsCallback(snapshot: DataSnapshot?, error : DatabaseError?)
}

interface UpdateBaseItemsCallback{
    fun onUpdateBaseItemsCallback(isSuccess: Boolean)
}

interface UpdateBaseItemPartialBaseItemCallback{
    fun onUpdateBaseItemPartialBaseItemCallback(isSuccess: Boolean)
}

interface UpdatePartialBaseItemsCallback{
    fun onUpdatePartialBaseItemsCallback(isSuccess: Boolean)
}

interface AddDetailBaseItemCallback{
    fun onAddDetailBaseItemCallback(isSuccess: Boolean)
}

interface AddInvoiceOrderCallback{
    fun onAddInvoiceOrderCallback(isSuccess: Boolean, newInvoiceId: Long? = null)
}

interface RemoveInvoiceOrderCallback{
    fun onRemoveInvoiceOrderCallback(isSuccess: Boolean)
}

interface RackInvoiceOrderCallback{
    fun onRackInvoiceOrderCallback(isSuccess: Boolean)
}
interface AddDropoffCallback{
    fun onAddDropoffCallback(isSuccess: Boolean)
}


interface SearchPhoneCallback{
    fun onSearchPhoneCallback(snapshot: DataSnapshot?, error : DatabaseError?)
}

interface SearchInvoiceCallback{
    fun onSearchInvoiceCallback(snapshot: DataSnapshot?, error : DatabaseError?)
}

interface SearchLastNameCallback{
    fun onSearchLastNameCallback(snapshot: DataSnapshot?, error : DatabaseError?)
}

interface SearchFirstNameCallback{
    fun onSearchFirstNameCallback(snapshot: DataSnapshot?, error : DatabaseError?)
}

interface InvoiceOrdersForCustomerCallback{
    fun onInvoiceOrdersForCustomerCallback(
        snapshot: DataSnapshot?,
        error : DatabaseError?
    )
}

interface LastPickupHistoryForCustomerCallback{
    fun onLastPickupHistoryForCustomerCallback(
        snapshot: DataSnapshot?,
        error : DatabaseError?
    )
}

interface InvoiceOrdersByIdCallback{
    fun onInvoiceOrdersByIdCallback(
        snapshot: DataSnapshot?,
        error : DatabaseError?
    )
}

interface InvoiceOrdersCallback{
    fun onInvoiceOrdersCallback(
        snapshot: DataSnapshot?,
        error : DatabaseError?
    )
}

interface PickupProcess{
    fun onPickupProcess(isSuccess: Boolean)
}

interface PickupByIdCallback{
    fun onPickupByIdCallback(
        snapshot: DataSnapshot?,
        error : DatabaseError?
    )
}

interface InvoiceOrderBriefGetBetweenCallback{
    fun onInvoiceOrderBriefsCallback(
        snapshot: DataSnapshot?,
        error : DatabaseError?
    )
}

interface PaymentGetBetweenCallback{
    fun onPaymentsCallback(
        snapshot: DataSnapshot?,
        error : DatabaseError?
    )
}

interface PickupGetBetweenCallback{
    fun onPickupsCallback(
        snapshot: DataSnapshot?,
        error : DatabaseError?
    )
}



interface DropoffGetBetweenCallback{
    fun onDropoffsCallback(
        snapshot: DataSnapshot?,
        error : DatabaseError?
    )
}