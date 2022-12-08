package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.HoldedInvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.DetailItemMakeInvoiceViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.HoldedInvoiceOrderViewholderBinding

class HoldedInvoiceOrderApt (val frg:HoldedInvoicesFrg): androidx.recyclerview.widget.ListAdapter<HoldedInvoiceOrder,HoldedInvoiceOrderVH>(HoldedInvoiceOrderDiffCallback()){
    private val TAG = HoldedInvoiceOrderApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoldedInvoiceOrderVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")

        val viewHolder = HoldedInvoiceOrderVH.from(parent)

        viewHolder.binding.cvHoldedInvoicOrderVH.setOnClickListener {
            frg.onCardViewClicked(it.tag as Int)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: HoldedInvoiceOrderVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)
        holder.bind(item, position, frg)
    }
}

class HoldedInvoiceOrderVH(val binding: HoldedInvoiceOrderViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = DetailItemMakeInvoiceVH::class.java.simpleName

    fun bind(holdedInvoiceOrder: HoldedInvoiceOrder, position: Int, frg:HoldedInvoicesFrg ){
        Log.d(TAG,"+++++ DetailItemMakeInvoiceVH bind +++++")

        val tag = position
        binding.cvHoldedInvoicOrderVH.setTag(tag)

        binding.cvHoldedInvoicOrderVH.setCardBackgroundColor(ContextCompat.getColor(binding.cvHoldedInvoicOrderVH.context, R.color.colorPrimary))
        binding.tvFirstNameHoldedInvoicOrderVH.text = holdedInvoiceOrder.customer!!.firstName
        binding.tvLastNameHoldedInvoicOrderVH.text = holdedInvoiceOrder.customer!!.lastName
        binding.tvPhoneHoldedInvoicOrderVH.text = holdedInvoiceOrder.customer!!.phoneNum
        binding.tvEmailHoldedInvoicOrderVH.text = holdedInvoiceOrder.customer!!.email

        binding.tvHoldedAtHoldedInvoicOrderVH.text = App.resourses!!.getString(R.string.holede_at)+holdedInvoiceOrder.holdedAt

        var itemStr = ""
        holdedInvoiceOrder.invoiceOrder?.fabricares?.let {fabricares ->
            for(item in fabricares){
                itemStr += " [ " +item.name + " ] "
            }
        }
        binding.tvItemHoldedInvoicOrderVH.text = itemStr

        var qty = 0
        with(holdedInvoiceOrder.invoiceOrder!!){
            qty = qtyTable!!["dry"]!! + qtyTable!!["wet"]!! + qtyTable!!["alter"]!! + qtyTable!!["press"]!!+ qtyTable!!["clean"]!!
        }
        binding.tvQtyHoldedInvoicOrderVH.text = App.resourses!!.getString(R.string.qty)+ ": "+qty.toString()

    }

    companion object {
        fun from(parent: ViewGroup): HoldedInvoiceOrderVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: HoldedInvoiceOrderViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.holded_invoice_order_viewholder,parent,false)
            return HoldedInvoiceOrderVH(binding)
        }
    }
}

class HoldedInvoiceOrderDiffCallback : DiffUtil.ItemCallback<HoldedInvoiceOrder>(){
    override fun areItemsTheSame(oldItem: HoldedInvoiceOrder, newItem: HoldedInvoiceOrder): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: HoldedInvoiceOrder, newItem: HoldedInvoiceOrder): Boolean {
        return oldItem == newItem
    }
}
