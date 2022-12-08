package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceOrderTaskBoardViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.UserInvoiceHistoryFrgBinding
import com.centuryprogrammer18thwasentsingleland.databinding.UserInvoiceHistoryViewholderBinding
import com.centuryprogrammer18thwasentsingleland.utils.UserInvoiceHistoryFrgCallback
import com.centuryprogrammer18thwasentsingleland.utils.UserInvoiceHistoryViewModelCallback

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class UserInvoiceHistoryApt (val viewModelCallback: UserInvoiceHistoryViewModelCallback, val frgCallback:UserInvoiceHistoryFrgCallback): ListAdapter<InvoiceOrder, UserInvoiceHistoryVH>(InvoiceOrderDiffCallback()){
    private val TAG = UserInvoiceHistoryApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserInvoiceHistoryVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")


        val viewholder = UserInvoiceHistoryVH.from(parent)

        viewholder.binding.loUserInvoiceHistoryVH.setOnClickListener { layout ->
            val index = layout.getTag() as Int

            val clickedInvoiceOrder = getItem(index)

            Log.d(TAG,"+++++ user invoice history viewholder clicked ++++++")
            frgCallback.onInovoiceOrderClicked(clickedInvoiceOrder!!)

        }

        return viewholder
    }

    override fun onBindViewHolder(holder: UserInvoiceHistoryVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)
        item?.let {
            holder.bind(it, position, viewModelCallback, frgCallback)
        }
    }
}

class UserInvoiceHistoryVH(val binding: UserInvoiceHistoryViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = UserInvoiceHistoryVH::class.java.simpleName

    fun bind(invoiceOrder: InvoiceOrder, position: Int, viewModelCallback: UserInvoiceHistoryViewModelCallback, frgCallback:UserInvoiceHistoryFrgCallback ){
        Log.d(TAG,"+++++ DetailItemMakeInvoiceVH bind +++++")

        val tag = position

        binding.loUserInvoiceHistoryVH.setTag(tag)

        if(invoiceOrder.pickedAtTimestamp != null){
            // invoiceOrder has been picked up

            // getColor deprecated ref) https://stackoverflow.com/a/32202256/3151712
            binding.cvUserInvoiceHistoryVH.setCardBackgroundColor(ContextCompat.getColor(binding.cvUserInvoiceHistoryVH.context, R.color.colorPrimaryDark))
        }

        binding.tvInvoiceIdUserInvoiceHistoryVH.text = invoiceOrder.id.toString()
        binding.tvCreatedAtUserInvoiceHistoryVH.text = invoiceOrder.createdAt
        binding.tvDueDateTimeUserInvoiceHistoryVH.text = invoiceOrder.dueDateTime
        binding.tvPickedUpAtTimeUserInvoiceHistoryVH.text = invoiceOrder.pickedUpAt?.toString()
        binding.tvRackLocationUserInvoiceHistoryVH.text = invoiceOrder.rackLocation
        binding.tvBalanceUserInvoiceHistoryVH.text = "$"+invoiceOrder.priceStatement!!["balance"].toString()

        val totalQty =
            invoiceOrder.qtyTable!!["dry"]!!+
                    invoiceOrder.qtyTable!!["wet"]!!+
                    invoiceOrder.qtyTable!!["alter"]!!+
                    invoiceOrder.qtyTable!!["press"]!!+
                    invoiceOrder.qtyTable!!["clean"]!!
        binding.tvTotalQtyUserInvoiceHistoryVH.text = totalQty.toString()
    }


    companion object {
        fun from(parent: ViewGroup): UserInvoiceHistoryVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: UserInvoiceHistoryViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.user_invoice_history_viewholder,parent,false)
            return UserInvoiceHistoryVH(binding)
        }
    }
}