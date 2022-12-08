package com.centuryprogrammer18thwasentsingleland.conveyor

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceConveyorViewholderBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.InvoiceOrderDiffCallback
import com.centuryprogrammer18thwasentsingleland.utils.*

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class InvoiceConveyorApt (val viewModelCallback: InvoiceConveyorViewModelCallback, val frgCallback: InvoiceConveyorFrgCallback): ListAdapter<InvoiceOrder, InvoiceConveyorVH>(
    InvoiceOrderDiffCallback()
),
    InvoiceInventoryHighLightCallback {
    private val TAG = InvoiceConveyorApt::class.java.simpleName

    private var highLighted = ""

    override fun onUpdatedHighLighted(highLightItem : String){
        highLighted = highLightItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceConveyorVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")


        val viewholder = InvoiceConveyorVH.from(parent)

        viewholder.binding.cvInvoiceConveyorVH.setOnClickListener { layout ->
            val index = layout.getTag() as Int

            val clickedInvoiceOrder = getItem(index)

            Log.d(TAG,"+++++ invoice inventory viewholder clicked ++++++")
            frgCallback.onInovoiceOrderClicked(clickedInvoiceOrder!!)

        }

        return viewholder
    }

    override fun onBindViewHolder(holder: InvoiceConveyorVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)
        item?.let {
            holder.bind(it, position, viewModelCallback, frgCallback, highLighted)
        }
    }
}

class InvoiceConveyorVH(val binding: InvoiceConveyorViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = InvoiceConveyorVH::class.java.simpleName

    fun bind(invoiceOrder: InvoiceOrder, position: Int, viewModelCallback: InvoiceConveyorViewModelCallback, frgCallback: InvoiceConveyorFrgCallback, highLightItem: String){
        Log.d(TAG,"+++++ InvoiceInventoryVH bind +++++")

        val tag = position

        binding.cvInvoiceConveyorVH.setTag(tag)

        when(highLightItem){
            "createdAt" ->{
                binding.tvCreatedAtInvoiceConveyorVH.setTextSize(18.0f)
                binding.tvCreatedAtInvoiceConveyorVH.setTypeface(Typeface.DEFAULT_BOLD)

                binding.tvFirstNameInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvLastNameInvoiceConveyorVH.setTextSize(14.0f)

                binding.tvFirstNameInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)
                binding.tvLastNameInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)

                binding.tvDueInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvDueInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)

                binding.tvPickedUpAtInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvPickedUpAtInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)
            }
            "customerName" ->{
                binding.tvFirstNameInvoiceConveyorVH.setTextSize(18.0f)
                binding.tvLastNameInvoiceConveyorVH.setTextSize(18.0f)

                binding.tvFirstNameInvoiceConveyorVH.setTypeface(Typeface.DEFAULT_BOLD)
                binding.tvLastNameInvoiceConveyorVH.setTypeface(Typeface.DEFAULT_BOLD)

                binding.tvCreatedAtInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvCreatedAtInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)

                binding.tvDueInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvDueInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)

                binding.tvPickedUpAtInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvPickedUpAtInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)
            }
            "dueDate" ->{
                binding.tvDueInvoiceConveyorVH.setTextSize(18.0f)
                binding.tvDueInvoiceConveyorVH.setTypeface(Typeface.DEFAULT_BOLD)

                binding.tvCreatedAtInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvCreatedAtInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)

                binding.tvFirstNameInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvLastNameInvoiceConveyorVH.setTextSize(14.0f)

                binding.tvFirstNameInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)
                binding.tvLastNameInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)

                binding.tvPickedUpAtInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvPickedUpAtInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)
            }
            "pickedUpAt"->{
                binding.tvPickedUpAtInvoiceConveyorVH.setTextSize(18.0f)
                binding.tvPickedUpAtInvoiceConveyorVH.setTypeface(Typeface.DEFAULT_BOLD)

                binding.tvCreatedAtInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvCreatedAtInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)

                binding.tvFirstNameInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvLastNameInvoiceConveyorVH.setTextSize(14.0f)

                binding.tvFirstNameInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)
                binding.tvLastNameInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)

                binding.tvDueInvoiceConveyorVH.setTextSize(14.0f)
                binding.tvDueInvoiceConveyorVH.setTypeface(Typeface.DEFAULT)
            }

        }

        binding.tvIdInvoiceConveyorVH.text = invoiceOrder.id.toString()
        binding.tvCreatedAtInvoiceConveyorVH.text = invoiceOrder.createdAt
        binding.tvDueInvoiceConveyorVH.text = invoiceOrder.dueDateTime
        binding.tvPickedUpAtInvoiceConveyorVH.text = invoiceOrder.pickedUpAt

        binding.tvFirstNameInvoiceConveyorVH.text = invoiceOrder.customer!!.firstName
        binding.tvLastNameInvoiceConveyorVH.text = invoiceOrder.customer!!.lastName
        binding.tvPhoneNumInvoiceConveyorVH.text = invoiceOrder.customer!!.phoneNum
        binding.tvEmailInvoiceConveyorVH.text = invoiceOrder.customer!!.email
    }


    companion object {
        fun from(parent: ViewGroup): InvoiceConveyorVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: InvoiceConveyorViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.invoice_conveyor_viewholder,parent,false)
            return InvoiceConveyorVH(binding)
        }
    }
}