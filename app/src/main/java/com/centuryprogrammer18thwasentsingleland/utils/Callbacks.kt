package com.centuryprogrammer18thwasentsingleland.utils

import android.widget.EditText
import com.centuryprogrammer18thwasentsingleland.data.BaseItem
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.PartialBaseItem
import com.centuryprogrammer18thwasentsingleland.data.Team

interface BaseItemVMAdapterInterface {
    fun onOkBtnClicked(key : String, baseItem : BaseItem)
}

interface BaseItemFrgAdapterInterface {
    fun onShowSoftKeyboard(show: Boolean, editText: EditText)
}

interface PartialBaseItemAdapterInterface{
    fun onOkBtnClicked(key:String, partialBaseItem: PartialBaseItem)
}

interface PartialBaseItemFrgAptInterface{
    fun onShowSoftKeyboard(show: Boolean, editText: EditText)
}

interface TeamAptCallback{
    fun onTeamSaveCallback(position:Int, team: Team)
    fun onTeamDeleteCallback(position:Int,team:Team)
    fun onMsgCallback(msg:String)
}

interface DatePickerCallback{
    fun onCancelBtnClicked(boolean: Boolean)
}

interface UserInvoiceHistoryFrgCallback{
    fun onInovoiceOrderClicked(invoiceOrder: InvoiceOrder)
}

interface UserInvoiceHistoryViewModelCallback{

}

interface InvoiceInventoryFrgCallback{
    fun onInovoiceOrderClicked(invoiceOrder: InvoiceOrder)
}

interface InvoiceInventoryViewModelCallback{

}

interface InvoiceConveyorFrgCallback{
    fun onInovoiceOrderClicked(invoiceOrder: InvoiceOrder)
}

interface InvoiceConveyorViewModelCallback{}

interface InvoiceInventoryHighLightCallback{
    fun onUpdatedHighLighted(hightLightItem: String)
}

interface FabricareDetailDialogCallback{
    fun onItemCancelCallback()
    fun onItemOkCallback(position:Int, newPrice: Double)
}


