package com.centuryprogrammer18thwasentsingleland.manager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.data.DetailBaseItem
import com.centuryprogrammer18thwasentsingleland.databinding.DetailBaseitemViewholderBinding
import com.centuryprogrammer18thwasentsingleland.repository.DetailBaseItemViewholderLongClickCallback
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import java.lang.StringBuilder

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class DetailBaseItemApt(val callback: DetailBaseItemViewholderLongClickCallback) : ListAdapter<DetailBaseItem, DetailBaseItemViewHolder>(DetailBaseItemDiffCallback()) {
    private val TAG = DetailBaseItemApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailBaseItemViewHolder {
        return DetailBaseItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DetailBaseItemViewHolder, position: Int) {
        val item = getItem(position)

        // set long click listener
        holder.binding.cvDetailBaseItemViewholder.setOnLongClickListener {

            callback.onDetailBaseItemViewholderLongClick(it.tag as DetailBaseItem)

            return@setOnLongClickListener true
        }

        holder.bind(item)
    }
}

class DetailBaseItemViewHolder(val binding: DetailBaseitemViewholderBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(item: DetailBaseItem){

        // make into ex) beads 2pc suit dry clean press
        val name = StringBuilder()
        name.append(item.name).append(" ").append(item.baseItemProcess.replace("_"," "))

        // add DetailBaseItem as tag
        binding.cvDetailBaseItemViewholder.setTag(item)

        binding.tvNameDetailBaseItemViewholder.text = name.toString()

        // make into ex) 2pc suit dry clean press
        binding.tvItemProcessDetailBaseItemViewholder.text = item.baseItemProcess.replace("_"," ")
        binding.tvRateDetailBaseItemViewholder.text = item.rate.toString()
        binding.tvAmountDetailBaseItemViewholder.text = makeTwoPointsDecialStringWithDollar(item.amount)
    }

    companion object{
        fun from(parent: ViewGroup):DetailBaseItemViewHolder{
            val layoutInflater = LayoutInflater.from(parent.context)

            // if i don't use parent parameter , android make full filled grid,and i can't adjust margin
            val binding = DetailBaseitemViewholderBinding.inflate(layoutInflater, parent, false)

            return DetailBaseItemViewHolder(binding)
        }
    }
}

class DetailBaseItemDiffCallback : DiffUtil.ItemCallback<DetailBaseItem>(){
    override fun areItemsTheSame(oldItem: DetailBaseItem, newItem: DetailBaseItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: DetailBaseItem, newItem: DetailBaseItem): Boolean {
        return oldItem == newItem
    }

}