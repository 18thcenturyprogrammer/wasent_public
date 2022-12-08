package com.centuryprogrammer18thwasentsingleland.sales

import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Dropoff
import com.centuryprogrammer18thwasentsingleland.data.Pickup
import com.centuryprogrammer18thwasentsingleland.databinding.DropoffViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.PickupViewholderBinding
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import java.lang.StringBuilder

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class PickupApt () : ListAdapter<Pickup, PickupVH>(PickupDiffCallback()) {

    private val TAG = PaymentsApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickupVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")

        val viewholder = PickupVH.from(parent)

        return viewholder
    }

    override fun onBindViewHolder(holder: PickupVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)

        holder.bind(item, position)
    }
}

class PickupVH(val binding: PickupViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = DropoffVH::class.java.simpleName

    fun bind(pickup: Pickup, position: Int){
        Log.d(TAG,"+++++ DropoffVH bind +++++")

        with(binding){
            tvIdPickupVH.text = pickup.id.toString()

            var invoiceIds = ""

            pickup.invoiceOrderIds?.let{
                for(item in it){
                    invoiceIds += "[ "+item+" ]"
                }
            }

            tvInvoiceIdsPickupVH.text = invoiceIds
            tvBalancePickupVH.text = makeTwoPointsDecialStringWithDollar(pickup.balance!!)
            tvRealPaidAmountPickupVH.text = makeTwoPointsDecialStringWithDollar(pickup.realPaidAmount!!)
            tvPayMethodPickupVH.text = pickup.payMethod
            tvCheckNumPickupVH.text = pickup.checkNum?:""
            tvPickUpAtPickupVH.text = pickup.pickUpAt
            tvPickedUpByPickupVH.text = pickup.pickedUpBy
        }

    }


    companion object {
        fun from(parent: ViewGroup): PickupVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: PickupViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.pickup_viewholder,parent,false)
            return PickupVH(binding)
        }
    }
}

class PickupDiffCallback : DiffUtil.ItemCallback<Pickup>(){
    override fun areItemsTheSame(oldItem: Pickup, newItem: Pickup): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Pickup, newItem: Pickup): Boolean {
        return oldItem == newItem
    }
}
