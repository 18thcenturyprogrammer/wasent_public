package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.databinding.DetailItemMakeInvoiceViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.SearchedCustomerStartViewholderBinding

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class SearchedCustomerStartApt (val viewModel: StartFragmentVM, val frg:StartFragment): androidx.recyclerview.widget.ListAdapter<Customer,SearchedCustomerStartVH>(SearchedCustomerDiffCallback()){
    private val TAG = SearchedCustomerStartApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchedCustomerStartVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")


        val viewholder = SearchedCustomerStartVH.from(parent)

        viewholder.binding.loSearchedCutomerStartVM.setOnClickListener {layout ->
            val index = layout.getTag() as Int

            // user clicked , send user to TaskBoardFragment with customer
            frg.goTaskBoardFragment(getItem(index))
        }

        return viewholder
    }

    override fun onBindViewHolder(holder: SearchedCustomerStartVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)
        holder.bind(item, position, viewModel, frg)
    }
}

class SearchedCustomerStartVH(val binding: SearchedCustomerStartViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = DetailItemMakeInvoiceVH::class.java.simpleName

    fun bind(customer: Customer, position: Int, viewModel: StartFragmentVM, frg:StartFragment ){
        Log.d(TAG,"+++++ DetailItemMakeInvoiceVH bind +++++")

        val tag = position

        binding.viewModel = viewModel
        binding.frg = frg

        binding.loSearchedCutomerStartVM.setTag(tag)
        binding.tvNameSearchedCustomerStartVH.text = customer.firstName +" "+customer.lastName
        binding.tvPhoneSearchedCustomerStartVH.text = customer.phoneNum

        // "shirt on Hanger" , "shirt in Box"
        // I want to show just last part
        binding.tvShirtSearchedCustomerStartVH.text = customer.shirt!!.split(" ").last()

        // "No starch" , "Light starch", "Medium starch"
        // I want to show just first part
        binding.tvStarchSearchedCustomerStartVH.text = customer.starch!!.split(" ").first()

        binding.tvNoteSearchedCustomerStartVH.text = customer.note

    }


    companion object {
        fun from(parent: ViewGroup): SearchedCustomerStartVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: SearchedCustomerStartViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.searched_customer_start_viewholder,parent,false)
            return SearchedCustomerStartVH(binding)
        }
    }
}

class SearchedCustomerDiffCallback : DiffUtil.ItemCallback<Customer>(){
    override fun areItemsTheSame(oldItem: Customer, newItem: Customer): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: Customer, newItem: Customer): Boolean {
        return false
    }
}