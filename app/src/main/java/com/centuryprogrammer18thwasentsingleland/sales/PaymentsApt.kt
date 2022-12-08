package com.centuryprogrammer18thwasentsingleland.sales

import android.content.SharedPreferences
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Payment
import com.centuryprogrammer18thwasentsingleland.databinding.PaymentViewholderBinding
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class PaymentsApt (val sharedPrefs: SharedPreferences, val frg:CategorizedPaymentsFrg, val categoryName: String) : ListAdapter<Payment, PaymentVH>(PaymentDiffCallback()) {

    private val TAG = PaymentsApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")

        val viewholder = PaymentVH.from(parent, categoryName)

        return viewholder
    }

    override fun onBindViewHolder(holder: PaymentVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)
        item?.let {

            holder.binding.btnPickupPaymentVH.setOnClickListener {
                val pickupId = it.getTag()

                pickupId?.let {
                    frg.onPickupBtnClicked(pickupId as Long)
                }
            }

            holder.binding.btnInvoicePaymentVH.setOnClickListener {
                val invoiceId = it.getTag()

                invoiceId?.let {
                    Repository.getInvoiceOrderByIdFromAll(sharedPrefs,it.toString(), frg)
                }
            }
            holder.bind(it, position)
        }
    }
}

class PaymentVH(val binding: PaymentViewholderBinding, val categoryName: String): RecyclerView.ViewHolder(binding.root){
    private val TAG = PaymentVH::class.java.simpleName

    fun bind(payment: Payment, position: Int){
        Log.d(TAG,"+++++ InvoiceInventoryVH bind +++++")

        val tag = position

        with(binding){
            btnPickupPaymentVH.setTag(tag)
            btnInvoicePaymentVH.setTag(tag)

            payment.invoiceOrderBalance?.let {
                tvInvoiceOrderBalancePaymentVH.text = makeTwoPointsDecialStringWithDollar(it)
            }

            when(categoryName){
                "prepaid_paid" -> {
                    tvAmountPaymentVH.text = makeTwoPointsDecialStringWithDollar(payment.prepaidAmount!!)
                    tvCreditAmountPaymentVH.text = ""
                }

                "prepaid_pickup" -> {
                    tvAmountPaymentVH.text = makeTwoPointsDecialStringWithDollar(payment.pastPrepaidAmount!!)
                    tvCreditAmountPaymentVH.text = ""
                }

                "credit_pickup" -> {
                    tvAmountPaymentVH.text = makeTwoPointsDecialStringWithDollar(payment.partialpaidAmount!!)
                    tvCreditAmountPaymentVH.text = makeTwoPointsDecialStringWithDollar(payment.creditAmount!!)
                }

                "credit_payback" -> {
                    tvAmountPaymentVH.text = makeTwoPointsDecialStringWithDollar(payment.creditpaidAmount!!)
                    tvCreditAmountPaymentVH.text = ""
                }

                "all_payments" -> {
                    var paidAmount = 0.0
                    var creditAmount = 0.0

                    payment.prepaidAmount?.let{amount ->
                        if(amount > 0.0){
                            binding.tvPaymentTypePaymentVH.text = App.resourses!!.getString(R.string.prepaid_dash_paid)
                            paidAmount = amount
                        }
                    }

                    payment.pastPrepaidAmount?.let{amount ->
                        if(amount > 0.0){
                            binding.tvPaymentTypePaymentVH.text = App.resourses!!.getString(R.string.prepaid_dash_pickup)
                            paidAmount = amount
                        }
                    }

                    payment.partialpaidAmount?.let{amount ->
                        if(amount > 0.0){
                            binding.tvPaymentTypePaymentVH.text = App.resourses!!.getString(R.string.credit_pickup)
                            paidAmount = amount

                            creditAmount = payment.creditAmount!!
                        }
                    }

                    payment.creditpaidAmount?.let{amount ->
                         if(amount > 0.0){
                             binding.tvPaymentTypePaymentVH.text = App.resourses!!.getString(R.string.credit_payback)
                             paidAmount = amount
                         }
                    }

                    payment.fullPaidAmount?.let{amount ->
                        if(amount > 0.0){
                            binding.tvPaymentTypePaymentVH.text = App.resourses!!.getString(R.string.fullpaid)
                            paidAmount = amount
                        }
                    }

                    tvAmountPaymentVH.text = makeTwoPointsDecialStringWithDollar(paidAmount)
                    tvCreditAmountPaymentVH.text = makeTwoPointsDecialStringWithDollar(creditAmount)
                }
            }


            tvPaymentMaidAtPaymentVH.text = payment.paymentMaidAt
            tvPaymentMaidByPaymentVH.text = payment.paymentMaidBy

            btnPickupPaymentVH.setTag(payment.pickupId)
            btnInvoicePaymentVH.setTag(payment.invoiceOrderId)


        }


    }


    companion object {
        fun from(parent: ViewGroup, categoryName:String): PaymentVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: PaymentViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.payment_viewholder,parent,false)
            return PaymentVH(binding, categoryName)
        }
    }
}

class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>(){
    override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
        return oldItem == newItem
    }
}
