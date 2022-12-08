package com.centuryprogrammer18thwasentsingleland.sales


import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.CategorizedInvoiceOrderViewholderBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.InvoiceOrderDiffCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar


// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class CategorizedInvoiceOrderApt (val sharedPrefs: SharedPreferences, val viewModel:CategorizedInvoicesFrgVM, val categoryName: String) : ListAdapter<InvoiceOrder, VoidedAdjustedInvoiceOrderVH>(InvoiceOrderDiffCallback()) {

    private val TAG = CategorizedInvoiceOrderApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoidedAdjustedInvoiceOrderVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")

        val viewholder = VoidedAdjustedInvoiceOrderVH.from(parent)

        return viewholder
    }

    override fun onBindViewHolder(holder: VoidedAdjustedInvoiceOrderVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)

        holder.binding.cvVoidedAdjustedInvoiceVH.setOnClickListener {
            Log.d(TAG,"+++++ voided invoice or adjusted invoice layout clicked +++++")

            viewModel.showVoidedAdjustedInvoice(it.getTag() as InvoiceOrder)
        }

        holder.binding.btnShowOrgInvoiceVoidedAdjustedInvoiceVH.setOnClickListener {
            Log.d(TAG,"+++++ original invoice button clicked +++++")

            val invoiceId = it.getTag() as Long

            Repository.getInvoiceOrderByIdFromAll(sharedPrefs, invoiceId.toString(), viewModel)
        }

        holder.bind(item, position, categoryName)

    }
}

class VoidedAdjustedInvoiceOrderVH(val binding: CategorizedInvoiceOrderViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = VoidedAdjustedInvoiceOrderVH::class.java.simpleName

    fun bind(invoiceOrder: InvoiceOrder, position: Int, categoryName: String){
        Log.d(TAG,"+++++ VoideAdjustedInvoiceOrderVH bind +++++")

        with(binding){
            cvVoidedAdjustedInvoiceVH.setTag(invoiceOrder)

            if(invoiceOrder.adjustedBy != null){
                // adjusted InvoiceOrder

                cvVoidedAdjustedInvoiceVH.setCardBackgroundColor(ContextCompat.getColor(binding.cvVoidedAdjustedInvoiceVH.context, R.color.colorWarning))

            }

            if(invoiceOrder.voidAt != null){
                // voided InvoiceOrder

                cvVoidedAdjustedInvoiceVH.setCardBackgroundColor(ContextCompat.getColor(binding.cvVoidedAdjustedInvoiceVH.context, R.color.colorDanger))

            }


            tvIdVoidedAdjustedInvoiceVH.text = invoiceOrder.id.toString()

            tvFirstNameVoidedAdjustedInvoiceVH.text = invoiceOrder.customer!!.firstName
            tvLastNameVoidedAdjustedInvoiceVH.text = invoiceOrder.customer!!.lastName
            tvPhoneVoidedAdjustedInvoiceVH.text = invoiceOrder.customer!!.phoneNum
            tvEmailVoidedAdjustedInvoiceVH.text = invoiceOrder.customer!!.email

            tvCreatedAtVoidedAdjustedInvoiceVH.text = invoiceOrder.createdAt
            tvCreatedByVoidedAdjustedInvoiceVH.text = invoiceOrder.createdBy

            tvDueDateTimeVoidedAdjustedInvoiceVH.text = invoiceOrder.dueDateTime

            tvTotalVoidedAdjustedInvoiceVH.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["total"]!!)

            when(categoryName){
                "all_invoices" -> {
                    tvVoidedAtVoidedAdjustedInvoiceVH.visibility = View.INVISIBLE
                    tvVoidedByVoidedAdjustedInvoiceVH.visibility = View.INVISIBLE
                    tvVoidedAtVoidedAdjustedInvoiceVH.text = ""
                    tvVoidedByVoidedAdjustedInvoiceVH.text = ""


                    tvOrgInvoiceIdVoidedAdjustedInvoiceVH.visibility = View.GONE

                    loShowOrgInvoiceVoidedAdjustedInvoiceVH.visibility = View.GONE
                }

                "voided" -> {
                    tvVoidedAtVoidedAdjustedInvoiceVH.visibility = View.VISIBLE
                    tvVoidedByVoidedAdjustedInvoiceVH.visibility = View.VISIBLE
                    tvVoidedAtVoidedAdjustedInvoiceVH.text = invoiceOrder.voidAt
                    tvVoidedByVoidedAdjustedInvoiceVH.text = invoiceOrder.voidBy


                    tvOrgInvoiceIdVoidedAdjustedInvoiceVH.visibility = View.GONE

                    loShowOrgInvoiceVoidedAdjustedInvoiceVH.visibility = View.GONE
                }

                "adjusted" -> {

                    tvOrgInvoiceIdVoidedAdjustedInvoiceVH.visibility = View.VISIBLE
                    tvOrgInvoiceIdVoidedAdjustedInvoiceVH.text = invoiceOrder.orgInvoiceOrderId.toString()

                    tvVoidedAtVoidedAdjustedInvoiceVH.visibility = View.GONE
                    tvVoidedByVoidedAdjustedInvoiceVH.visibility = View.GONE

                    loShowOrgInvoiceVoidedAdjustedInvoiceVH.visibility = View.VISIBLE

                    btnShowOrgInvoiceVoidedAdjustedInvoiceVH.setTag(invoiceOrder.orgInvoiceOrderId)
                }
            }
        }

    }


    companion object {
        fun from(parent: ViewGroup): VoidedAdjustedInvoiceOrderVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: CategorizedInvoiceOrderViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.categorized_invoice_order_viewholder,parent,false)
            return VoidedAdjustedInvoiceOrderVH(binding)
        }
    }
}
