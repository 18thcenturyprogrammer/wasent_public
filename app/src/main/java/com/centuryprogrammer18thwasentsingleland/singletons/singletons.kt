package com.centuryprogrammer18thwasentsingleland.singletons

import com.centuryprogrammer18thwasentsingleland.data.Fabricare
import com.centuryprogrammer18thwasentsingleland.data.HoldedInvoiceOrder

// first i tried to make user can click one fabricare and if user click one more time , deselect the fabricare.
// however, i thought not allowing deselect is better, so i have unused functionality
object SeletedFabricare{
    var index : Int = -1
    var fabricare : Fabricare? = null

    fun nothingSelected():SeletedFabricare{
        index = -1
        fabricare = null
        return this
    }

    fun selected(selectedIndex: Int, selectedFabricare : Fabricare):SeletedFabricare{
        index = selectedIndex
        fabricare = selectedFabricare
        return this
    }

    fun isThereSelectedOne():Boolean{
        return index != -1
    }
}

object HoldedInvoiceOrders{
    private var holdedInvoicedOrders  = mutableListOf<HoldedInvoiceOrder>()

    fun isThereHoldInovoices():Boolean{
        return holdedInvoicedOrders.count() > 0
    }

    fun addInvoice(holdedInvoiceOrder : HoldedInvoiceOrder){
        holdedInvoicedOrders.add(holdedInvoiceOrder)
    }

    fun updateInvoices(invoices : MutableList<HoldedInvoiceOrder>){
        holdedInvoicedOrders = invoices
    }

    fun getHoldedInvoiceOrders():MutableList<HoldedInvoiceOrder>{
        return holdedInvoicedOrders
    }

    fun getHoldedInvoiceByIndex(Index: Int):HoldedInvoiceOrder{
        return holdedInvoicedOrders[Index]
    }

    fun removeHoldedInvoiceByIndex(Index: Int){
        holdedInvoicedOrders.removeAt(Index)
    }
}