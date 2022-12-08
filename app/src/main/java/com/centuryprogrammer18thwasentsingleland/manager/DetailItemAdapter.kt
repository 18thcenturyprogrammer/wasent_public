package com.centuryprogrammer18thwasentsingleland.manager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.data.DetailItem
import com.centuryprogrammer18thwasentsingleland.databinding.DetailItemFrgBinding
import com.centuryprogrammer18thwasentsingleland.databinding.DetailItemViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.ItemViewholderBinding
import com.centuryprogrammer18thwasentsingleland.repository.DetailItemViewholderLongClickCallback

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class DetailItemAdapter(val callback: DetailItemViewholderLongClickCallback): ListAdapter<DetailItem, DetailItemViewHolder>(DetailItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailItemViewHolder {
        return DetailItemViewHolder.from(parent,callback)
    }

    override fun onBindViewHolder(holder: DetailItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


}

class DetailItemViewHolder(val binding: DetailItemViewholderBinding, val callback: DetailItemViewholderLongClickCallback) : RecyclerView.ViewHolder(binding.root){
    fun bind(detailItem: DetailItem){
        binding.cvDetailItemViewholder.setOnLongClickListener {view ->
            callback.onDetailItemViewholderLongClick(view)
            return@setOnLongClickListener true
        }

        binding.tvNameDetailItemViewholder.text = detailItem.name
        binding.tvCategoryDetailItemViewholder.text = detailItem.category
    }

    companion object{
        fun from(parent: ViewGroup, callback: DetailItemViewholderLongClickCallback):DetailItemViewHolder{
            val layoutInflater = LayoutInflater.from(parent.context)

            val binding = DetailItemViewholderBinding.inflate(layoutInflater, parent, false)

            return DetailItemViewHolder(binding, callback)
        }
    }
}

class DetailItemDiffCallback : DiffUtil.ItemCallback<DetailItem>(){
    override fun areItemsTheSame(oldDetailItem: DetailItem, newDetailItem: DetailItem): Boolean {
        return oldDetailItem.name == newDetailItem.name
    }

    override fun areContentsTheSame(oldDetailItem: DetailItem, newDetailItem: DetailItem): Boolean {
        return oldDetailItem == newDetailItem
    }

}