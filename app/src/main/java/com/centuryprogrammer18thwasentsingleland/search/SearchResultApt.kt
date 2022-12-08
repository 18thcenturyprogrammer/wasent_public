package com.centuryprogrammer18thwasentsingleland.search

import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.Payment
import com.centuryprogrammer18thwasentsingleland.databinding.CategorizedInvoiceOrderViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.SearchResultViewholderBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.InvoiceOrderDiffCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.sales.CategorizedInvoicesFrgVM
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class SearchResultApt (val frg:SearchingSearchFrg) : ListAdapter<SearchResult, SearchResultVH>(
    SearchResultDiffCallback()
) {

    private val TAG = SearchResultApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")

        val viewholder = SearchResultVH.from(parent)

        return viewholder
    }

    override fun onBindViewHolder(holder: SearchResultVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)

        holder.binding.tvContentSearchResultVH.text = item.content

        holder.binding.frg = frg
        holder.binding.searchResult = item

        holder.bind(item, position)

    }
}

class SearchResultVH(val binding: SearchResultViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = SearchResultVH::class.java.simpleName

    fun bind(searchResult: SearchResult, position: Int){
        Log.d(TAG,"+++++ SearchResultVH bind +++++")

    }


    companion object {
        fun from(parent: ViewGroup): SearchResultVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: SearchResultViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.search_result_viewholder,parent,false)
            return SearchResultVH(binding)
        }
    }
}

class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResult>(){
    override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
        return oldItem == newItem
    }
}
