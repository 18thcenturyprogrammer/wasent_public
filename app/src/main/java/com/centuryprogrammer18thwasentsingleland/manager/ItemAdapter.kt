package com.centuryprogrammer18thwasentsingleland.manager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.data.Item
import com.centuryprogrammer18thwasentsingleland.databinding.ItemViewholderBinding
import com.centuryprogrammer18thwasentsingleland.repository.ItemViewholderLongClickCallback

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt


class ItemAdapter(val callback: ItemViewholderLongClickCallback): ListAdapter<Item, ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.from(parent,callback)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


}

class ItemViewHolder(val binding:ItemViewholderBinding, val callback: ItemViewholderLongClickCallback) : RecyclerView.ViewHolder(binding.root){
    fun bind(item: Item){
        binding.loItemViewholder.setOnLongClickListener {view ->
            callback.onItemViewholderLongClick(view)
            return@setOnLongClickListener true
        }
        binding.tvItemNameViewholder.text = item.name
        binding.tvItemNumPieceViewholder.text = item.numPiece.toString()
    }

    companion object{
        fun from(parent: ViewGroup, callback: ItemViewholderLongClickCallback):ItemViewHolder{
            val layoutInflater = LayoutInflater.from(parent.context)

            // if i don't use parent parameter , android make full filled grid,and i can't adjust margin
            val binding = ItemViewholderBinding.inflate(layoutInflater, parent, false)

            return ItemViewHolder(binding, callback)
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<Item>(){
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }

}