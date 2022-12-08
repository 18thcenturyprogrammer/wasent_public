package com.centuryprogrammer18thwasentsingleland.rack

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.DetailItemViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceOrderRackViewholderBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.InvoiceOrderDiffCallback


// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class InvoiceOrderRackApt(): ListAdapter<InvoiceOrder,InvoiceOrderRackVH >(InvoiceOrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceOrderRackVH {
        return InvoiceOrderRackVH.from(parent)
    }

    override fun onBindViewHolder(holder: InvoiceOrderRackVH, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class InvoiceOrderRackVH(val binding: InvoiceOrderRackViewholderBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(invoiceOrder: InvoiceOrder){
        with(binding){
            tvInvoiceIdDoingRackFrgVH.text = invoiceOrder.id.toString()
            tvRackLocationDoingRackFrgVH.text = invoiceOrder.rackLocation
            tvFirstNameDoingRackFrgVH.text = invoiceOrder.customer!!.firstName
            tvLastNameDoingRackFrgVH.text = invoiceOrder.customer!!.lastName
            tvPhoneDoingRackFrgVH.text = invoiceOrder.customer!!.phoneNum
            tvDueDoingRackFrgVH.text = invoiceOrder.dueDateTime
        }
    }

    companion object{
        fun from(parent: ViewGroup):InvoiceOrderRackVH{
            val layoutInflater = LayoutInflater.from(parent.context)

            val binding = InvoiceOrderRackViewholderBinding.inflate(layoutInflater, parent,false)

            return InvoiceOrderRackVH(binding)
        }
    }
}

