package com.centuryprogrammer18thwasentsingleland.inventory

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceInventoryViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.UserInvoiceHistoryViewholderBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.InvoiceOrderDiffCallback
import com.centuryprogrammer18thwasentsingleland.utils.*

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class InvoiceInventoryApt (val viewModelCallback: InvoiceInventoryViewModelCallback, val frgCallback: InvoiceInventoryFrgCallback): ListAdapter<InvoiceOrder, InvoiceInventoryVH>(InvoiceOrderDiffCallback()),
    InvoiceInventoryHighLightCallback {
    private val TAG = InvoiceInventoryApt::class.java.simpleName

    private var highLighted = ""

    override fun onUpdatedHighLighted(highLightItem : String){
        highLighted = highLightItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceInventoryVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")


        val viewholder = InvoiceInventoryVH.from(parent)

        viewholder.binding.cvInvoiceInventoryVH.setOnClickListener { cardView ->
            val index = cardView.getTag() as Int

            val clickedInvoiceOrder = getItem(index)

            Log.d(TAG,"+++++ invoice inventory viewholder clicked ++++++")
            frgCallback.onInovoiceOrderClicked(clickedInvoiceOrder!!)

        }

        return viewholder
    }

    override fun onBindViewHolder(holder: InvoiceInventoryVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)
        item?.let {
            holder.bind(it, position, viewModelCallback, frgCallback, highLighted)
        }
    }
}

class InvoiceInventoryVH(val binding: InvoiceInventoryViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = InvoiceInventoryVH::class.java.simpleName

    fun bind(invoiceOrder: InvoiceOrder, position: Int, viewModelCallback: InvoiceInventoryViewModelCallback, frgCallback: InvoiceInventoryFrgCallback, highLightItem: String){
        Log.d(TAG,"+++++ InvoiceInventoryVH bind +++++")

        val tag = position

        binding.cvInvoiceInventoryVH.setTag(tag)

        if(invoiceOrder.rackLocation != null && invoiceOrder.rackLocation!!.isNotEmpty()){
            // invoiceOrder has not been racked

            // getColor deprecated ref) https://stackoverflow.com/a/32202256/3151712
            binding.cvInvoiceInventoryVH.setCardBackgroundColor(ContextCompat.getColor(binding.cvInvoiceInventoryVH.context, R.color.colorPrimaryDark))
        }

        when(highLightItem){
            "createdAt" ->{
                binding.tvCreatedAtInvoiceInventoryVH.setTextSize(18.0f)
                binding.tvCreatedAtInvoiceInventoryVH.setTypeface(Typeface.DEFAULT_BOLD)

                binding.tvFirstNameInvoiceInventoryVH.setTextSize(14.0f)
                binding.tvLastNameInvoiceInventoryVH.setTextSize(14.0f)

                binding.tvFirstNameInvoiceInventoryVH.setTypeface(Typeface.DEFAULT)
                binding.tvLastNameInvoiceInventoryVH.setTypeface(Typeface.DEFAULT)

                binding.tvRackLocationInvoiceInventoryVH.setTextSize(14.0f)
                binding.tvRackLocationInvoiceInventoryVH.setTypeface(Typeface.DEFAULT)
            }
            "customerName" ->{
                binding.tvFirstNameInvoiceInventoryVH.setTextSize(18.0f)
                binding.tvLastNameInvoiceInventoryVH.setTextSize(18.0f)

                binding.tvFirstNameInvoiceInventoryVH.setTypeface(Typeface.DEFAULT_BOLD)
                binding.tvLastNameInvoiceInventoryVH.setTypeface(Typeface.DEFAULT_BOLD)

                binding.tvCreatedAtInvoiceInventoryVH.setTextSize(14.0f)
                binding.tvCreatedAtInvoiceInventoryVH.setTypeface(Typeface.DEFAULT)

                binding.tvRackLocationInvoiceInventoryVH.setTextSize(14.0f)
                binding.tvRackLocationInvoiceInventoryVH.setTypeface(Typeface.DEFAULT)
            }
            "rackLocation" ->{
                binding.tvRackLocationInvoiceInventoryVH.setTextSize(18.0f)
                binding.tvRackLocationInvoiceInventoryVH.setTypeface(Typeface.DEFAULT_BOLD)

                binding.tvCreatedAtInvoiceInventoryVH.setTextSize(14.0f)
                binding.tvCreatedAtInvoiceInventoryVH.setTypeface(Typeface.DEFAULT)

                binding.tvFirstNameInvoiceInventoryVH.setTextSize(14.0f)
                binding.tvLastNameInvoiceInventoryVH.setTextSize(14.0f)

                binding.tvFirstNameInvoiceInventoryVH.setTypeface(Typeface.DEFAULT)
                binding.tvLastNameInvoiceInventoryVH.setTypeface(Typeface.DEFAULT)
            }

        }

        binding.tvIdInvoiceInventoryVH.text = invoiceOrder.id.toString()
        binding.tvCreatedAtInvoiceInventoryVH.text = invoiceOrder.createdAt
        binding.tvDueInvoiceInventoryVH.text = invoiceOrder.dueDateTime
        binding.tvRackLocationInvoiceInventoryVH.text = invoiceOrder.rackLocation
        binding.tvBalanceInvoiceInventoryVH.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["balance"]!!)

        val totalQty =
                invoiceOrder.qtyTable!!["dry"]!!+
                invoiceOrder.qtyTable!!["wet"]!!+
                invoiceOrder.qtyTable!!["alter"]!!+
                invoiceOrder.qtyTable!!["press"]!!+
                invoiceOrder.qtyTable!!["clean"]!!
        binding.tvTotalQtyInvoiceInventoryVH.text = totalQty.toString()

        binding.tvFirstNameInvoiceInventoryVH.text = invoiceOrder.customer!!.firstName
        binding.tvLastNameInvoiceInventoryVH.text = invoiceOrder.customer!!.lastName
        binding.tvPhoneNumInvoiceInventoryVH.text = invoiceOrder.customer!!.phoneNum
        binding.tvEmailInvoiceInventoryVH.text = invoiceOrder.customer!!.email
    }


    companion object {
        fun from(parent: ViewGroup): InvoiceInventoryVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: InvoiceInventoryViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.invoice_inventory_viewholder,parent,false)
            return InvoiceInventoryVH(binding)
        }
    }
}