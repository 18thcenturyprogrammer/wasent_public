package com.centuryprogrammer18thwasentsingleland.drop_pickup.pickup

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceWithPayment
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceWithPaymentPickupViewholderBinding
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar


// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class InvoiceWithPaymentPickupApt (val viewModel: InvoiceOrderPickupFrgVM, val frg: InvoiceOrderPickupFrg): ListAdapter<InvoiceWithPayment, InvoiceWithPaymentPickupVH>(
    InvoiceWithPaymentDiffCallback()
){
    private val TAG = InvoiceWithPaymentPickupApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceWithPaymentPickupVH {

        val viewholder = InvoiceWithPaymentPickupVH.from(parent)

        // this is called when check is changed
        viewholder.binding.rbgTypePaymentInvoiceOrderVH.setOnCheckedChangeListener { radioGroup, btnId ->
            Log.d(TAG,"\n+++++ radio button checked is changed which is invoiceWithPayment list +++++")


            // position is Tag
            val checkedTag = radioGroup!!.getTag() as Int
            Log.d(TAG,"+++++ position is ${checkedTag.toString()} +++++")
            Log.d(TAG,"+++++ at position invoiceWithPayment is ${getItem(checkedTag).invoiceOrderId.toString()} ${getItem(checkedTag).typePayment.toString()} ${getItem(checkedTag).amount.toString()}+++++\n\n")

            when(btnId){
                R.id.rbInvoiceOrderFullpaidVH -> {
                    Log.d(TAG,"+++++ full paid radio button checked changed +++++")

                    val temp = getItem(checkedTag)

                    // check if radio button status changed by user or program
                    if(viewholder.binding.rbInvoiceOrderFullpaidVH.isPressed){
                        // by user , not program

                        Log.d(TAG,"+++++ by user +++++")
                        Log.d(TAG,"+++++ fullpaid radio button isChecked : ${viewholder.binding.rbInvoiceOrderFullpaidVH.isChecked.toString()} +++++")

                        if(!viewholder.binding.rbInvoiceOrderFullpaidVH.isChecked){

                            Log.d(TAG,"+++++ InvoiceOrderPickupFrgVM onDeCheckedTypePayment will be called , because i think user want deselect+++++\n\n\n")
                            viewModel.onDeCheckedTypePayment(temp)
                        }else{
                            temp.typePayment= "fullpaid"

                            Log.d(TAG,"+++++ InvoiceOrderPickupFrg onCheckedTypePayment will be called , because i think user want select+++++")
                            Log.d(TAG,"+++++ invoiceWithPayment is ${temp.invoiceOrderId.toString()} ${temp.typePayment.toString()} ${temp.amount.toString()} will be passed+++++")

                            frg.onCheckedTypePayment(temp)

                            // i spent a lot of time to figure this out
                            // i had to set as default, which is null
                            temp.typePayment= null

                            Log.d(TAG,"+++++ after sending and setting, make default val invoiceWithPayment is ${temp.invoiceOrderId.toString()} ${temp.typePayment.toString()} ${temp.amount.toString()} +++++\n\n\n")

                        }
                    }
                }

                R.id.rbInvoiceOrderPrepaidVH -> {
                    Log.d(TAG,"+++++ prepaid paid radio button checked changed +++++")

                    val temp = getItem(checkedTag)

                    // check if radio button status changed by user or program
                    if(viewholder.binding.rbInvoiceOrderPrepaidVH.isPressed){
                        // by user , not program

                        Log.d(TAG,"+++++ by user +++++")
                        Log.d(TAG,"+++++ prepaid radio button isChecked : ${viewholder.binding.rbInvoiceOrderPrepaidVH.isChecked.toString()} +++++")

                        if(!viewholder.binding.rbInvoiceOrderPrepaidVH.isChecked){
                            Log.d(TAG,"+++++ InvoiceOrderPickupFrgVM onDeCheckedTypePayment will be called , because i think user want deselect+++++\n\n\n")
                            viewModel.onDeCheckedTypePayment(temp)
                        }else{
                            temp.typePayment= "prepaid"

                            Log.d(TAG,"+++++ InvoiceOrderPickupFrg onCheckedTypePayment will be called , because i think user want select+++++")
                            Log.d(TAG,"+++++ invoiceWithPayment is ${temp.invoiceOrderId.toString()} ${temp.typePayment.toString()} ${temp.amount.toString()} will be passed+++++")


                            frg.onCheckedTypePayment(temp)

                            // i spent a lot of time to figure this out
                            // i had to set as default, which is null
                            temp.typePayment= null

                            Log.d(TAG,"+++++ after sending and setting default val invoiceWithPayment is ${temp.invoiceOrderId.toString()} ${temp.typePayment.toString()} ${temp.amount.toString()} +++++\n\n\n")
                        }
                    }
                }

                R.id.rbInvoiceOrderPartialPaidVH -> {
                    Log.d(TAG,"+++++ partial paid radio button checked changed +++++")

                    val temp = getItem(checkedTag)

                    Log.d(TAG,"+++++ by user +++++")
                    Log.d(TAG,"+++++ partial paid radio button isChecked : ${viewholder.binding.rbInvoiceOrderPartialPaidVH.isChecked.toString()} +++++")


                    // check if radio button status changed by user or program
                    if(viewholder.binding.rbInvoiceOrderPartialPaidVH.isPressed){
                        // by user , not program

                        if(!viewholder.binding.rbInvoiceOrderPartialPaidVH.isChecked){
                            Log.d(TAG,"+++++ InvoiceOrderPickupFrgVM onDeCheckedTypePayment will be called , because i think user want deselect+++++\n\n\n")

                            viewModel.onDeCheckedTypePayment(temp)
                        }else{
                            temp.typePayment= "partialpaid"

                            Log.d(TAG,"+++++ InvoiceOrderPickupFrg onCheckedTypePayment will be called , because i think user want select+++++")
                            Log.d(TAG,"+++++ invoiceWithPayment is ${temp.invoiceOrderId.toString()} ${temp.typePayment.toString()} ${temp.amount.toString()} will be passed+++++")


                            frg.onCheckedTypePayment(temp)

                            // i spent a lot of time to figure this out
                            // i had to set as default, which is null
                            temp.typePayment= null

                            Log.d(TAG,"+++++ after sending and setting default val invoiceWithPayment is ${temp.invoiceOrderId.toString()} ${temp.typePayment.toString()} ${temp.amount.toString()} +++++\n\n\n")

                        }
                    }
                }
            }
        }

        return viewholder
    }

    override fun onBindViewHolder(holder: InvoiceWithPaymentPickupVH, position: Int) {

        val item = getItem(position)

        Log.d(TAG,"+++++ onBindViewHolder | position :${position.toString()} | isPaid :${item.isPaid.toString()} | amount :${item.amount.toString()} | typePayment :${item.typePayment.toString()}+++++")


        holder.bind(item, position, viewModel, frg)

    }
}

class InvoiceWithPaymentPickupVH(val binding: InvoiceWithPaymentPickupViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = InvoiceWithPaymentPickupVH::class.java.simpleName

    fun bind(invoiceWithPayment: InvoiceWithPayment, position: Int, viewModel: InvoiceOrderPickupFrgVM, frg: InvoiceOrderPickupFrg){
        val tag = position

        binding.viewModel = viewModel
        binding.frg = frg

        binding.loInvoiceOrderPickupVH.setTag(tag)

        binding.tvIdInvoiceOrderPickupVH.text = invoiceWithPayment.invoiceOrderId.toString()
        binding.tvCreatedAtInvoiceOrderPickupVH.text = invoiceWithPayment.invoiceOrder!!.createdAt
        binding.tvDueDateTimeInvoiceOrderPickupVH.text = invoiceWithPayment.invoiceOrder!!.dueDateTime
        binding.tvRackLocationInvoiceOrderPickupVH.text = invoiceWithPayment.invoiceOrder!!.rackLocation
        binding.tvBalanceInvoiceOrderPickupVH.text = makeTwoPointsDecialStringWithDollar(invoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!!)

        val totalQty =
            invoiceWithPayment.invoiceOrder!!.qtyTable!!["dry"]!!+
                    invoiceWithPayment.invoiceOrder!!.qtyTable!!["wet"]!!+
                    invoiceWithPayment.invoiceOrder!!.qtyTable!!["alter"]!!+
                    invoiceWithPayment.invoiceOrder!!.qtyTable!!["press"]!!+
                    invoiceWithPayment.invoiceOrder!!.qtyTable!!["clean"]!!
        binding.tvTotalQtyInvoiceOrderPickupVH.text = totalQty.toString()
        binding.rbgTypePaymentInvoiceOrderVH.setTag(tag)


        when(invoiceWithPayment.typePayment){
            "fullpaid" -> {
                binding.rbgTypePaymentInvoiceOrderVH.check(R.id.rbInvoiceOrderFullpaidVH)
            }
            "prepaid" -> {
                binding.rbgTypePaymentInvoiceOrderVH.check(R.id.rbInvoiceOrderPrepaidVH)
            }
            "partialpaid" -> {
                binding.rbgTypePaymentInvoiceOrderVH.check(R.id.rbInvoiceOrderPartialPaidVH)
            }
            null -> {
                binding.rbgTypePaymentInvoiceOrderVH.clearCheck()
            }
        }

//if i want to not allow second prepaid, remove comment below
//        if(invoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!! > 0.0){
//            // customer has prepaid before
//            // i allow only one prepaid per invoice order
//
//            binding.rbInvoiceOrderPrepaidVH.isEnabled = false
//        }

        if(invoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"] == 0.0){
            // customer all prepaid or balance is zero
            // hide prepaid and partialpaid radio button

            binding.rbInvoiceOrderPrepaidVH.isEnabled = false
            binding.rbInvoiceOrderPartialPaidVH.isEnabled = false
        }
    }

    companion object {
        fun from(parent: ViewGroup): InvoiceWithPaymentPickupVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: InvoiceWithPaymentPickupViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.invoice_with_payment_pickup_viewholder,parent,false)
            return InvoiceWithPaymentPickupVH(binding)
        }
    }
}

class InvoiceWithPaymentDiffCallback : DiffUtil.ItemCallback<InvoiceWithPayment>(){
    override fun areItemsTheSame(oldItem: InvoiceWithPayment, newItem: InvoiceWithPayment): Boolean {
        return oldItem.invoiceOrderId == newItem.invoiceOrderId
    }

    override fun areContentsTheSame(oldItem: InvoiceWithPayment, newItem: InvoiceWithPayment): Boolean {
        return oldItem == newItem
    }
}

// custom radio button which can be toggled
class ToggleAbleRadioButton: AppCompatRadioButton{
    constructor(context: Context) : this(context, null){}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){}

    private val TAG = ToggleAbleRadioButton::class.java.simpleName

    override fun toggle() {
        if (isChecked) {
            if (parent != null && parent is RadioGroup) {
                (parent as RadioGroup).clearCheck()
                Log.d(TAG,"+++++ radio button group clear checked +++++")
            }
        } else {
            super.toggle()
        }

    }
}