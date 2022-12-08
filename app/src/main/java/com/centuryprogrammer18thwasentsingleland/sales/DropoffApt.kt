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
import com.centuryprogrammer18thwasentsingleland.databinding.DropoffViewholderBinding
import java.lang.StringBuilder

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class DropoffApt () : ListAdapter<Dropoff, DropoffVH>(DropoffDiffCallback()) {

    private val TAG = PaymentsApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DropoffVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")

        val viewholder = DropoffVH.from(parent)

        return viewholder
    }

    override fun onBindViewHolder(holder: DropoffVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)

        holder.bind(item, position)
    }
}

class DropoffVH(val binding: DropoffViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = DropoffVH::class.java.simpleName

    fun bind(dropoff: Dropoff, position: Int){
        Log.d(TAG,"+++++ DropoffVH bind +++++")

        with(binding){
            tvFirstNameDropoffVH.text = dropoff.customer!!.firstName
            tvLastNameDropoffVH.text = dropoff.customer!!.lastName
            tvPhoneDropoffVH.text = dropoff.customer!!.phoneNum
            tvEmailDropoffVH.text = dropoff.customer!!.email

            val itemsStrBuilder = StringBuilder()

            if(dropoff.qtyTable!!["dry"]!! >0 ){
                itemsStrBuilder.append("Dry[ "+ dropoff.qtyTable!!["dry"]!!.toString()+" ]")
            }

            if(dropoff.qtyTable!!["wet"]!! >0 ){
                itemsStrBuilder.append("Wet[ "+ dropoff.qtyTable!!["wet"]!!.toString()+" ]")
            }

            if(dropoff.qtyTable!!["alter"]!! >0 ){
                itemsStrBuilder.append("Alter[ "+ dropoff.qtyTable!!["alter"]!!.toString()+" ]")
            }

            if(dropoff.qtyTable!!["household"]!! >0 ){
                itemsStrBuilder.append("Household[ "+ dropoff.qtyTable!!["household"]!!.toString()+" ]")
            }

            if(dropoff.qtyTable!!["clean_only"]!! >0 ){
                itemsStrBuilder.append("Clean only[ "+ dropoff.qtyTable!!["clean_only"]!!.toString()+" ]")
            }

            if(dropoff.qtyTable!!["press_only"]!! >0 ){
                itemsStrBuilder.append("Press only[ "+ dropoff.qtyTable!!["press_only"]!!.toString()+" ]")
            }

            if(dropoff.qtyTable!!["ext"]!! >0 ){
                itemsStrBuilder.append("Ext[ "+ dropoff.qtyTable!!["ext"]!!.toString()+" ]")
            }

            if(dropoff.qtyTable!!["redo"]!! >0 ){
                itemsStrBuilder.append("Redo[ "+ dropoff.qtyTable!!["redo"]!!.toString()+" ]")
            }

            tvItemsDropoffVH.text = itemsStrBuilder.toString()

            tvCreatedAtDropoffVH.text = dropoff.createdAt
            tvCreatedByDropoffVH.text = dropoff.createdBy

            tvDuedateDropoffVH.text = dropoff.dueDateTime

            // textview scroll scrollable ref) https://stackoverflow.com/a/56355714/3151712
            tvNoteDropoffVH.movementMethod = ScrollingMovementMethod()
            tvNoteDropoffVH.text = dropoff.note

        }

    }


    companion object {
        fun from(parent: ViewGroup): DropoffVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: DropoffViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.dropoff_viewholder,parent,false)
            return DropoffVH(binding)
        }
    }
}

class DropoffDiffCallback : DiffUtil.ItemCallback<Dropoff>(){
    override fun areItemsTheSame(oldItem: Dropoff, newItem: Dropoff): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Dropoff, newItem: Dropoff): Boolean {
        return oldItem == newItem
    }
}
