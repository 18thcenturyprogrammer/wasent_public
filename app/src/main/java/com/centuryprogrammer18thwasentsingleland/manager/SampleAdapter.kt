//package com.centuryprogrammer18thwasentsingleland.manager
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.RecyclerView
//import com.centuryprogrammer18thwasentsingleland.data.BasicItem
//import com.centuryprogrammer18thwasentsingleland.databinding.BasicItemViewholderBinding
//
//
//// recycler view adpater
//// using DiffUtil and ListAdapter
//// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
//// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt
//
//class BasicItemAdapter : androidx.recyclerview.widget.ListAdapter<BasicItem,BasicItemAdapter.ViewHolder>(BasicItemDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder.from(parent)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = getItem(position)
//        holder.bind(item)
//    }
//
//
//    class ViewHolder(val binding: BasicItemViewholderBinding) : RecyclerView.ViewHolder(binding.root){
//        fun bind(basicItem : BasicItem){
//            binding.tvNameBasicItemViewholder.text = basicItem.name
//            binding.tvCategoryBasicItemViewholder.text = "category"
//            binding.tvPriceBasicItemViewholder.text = basicItem.price.toString()
//            binding.tvNumPieceBasicItemViewholder.text = basicItem.numPiece.toString()
//            binding.tvRateBasicItemViewholder.text = basicItem.priceChangeRate.toString()
//            binding.tvAmoutBasicItemViewholder.text = basicItem.priceChangeAmount.toString()
//
//
//        }
//
//        companion object{
//            fun from(parent: ViewGroup):ViewHolder{
//                val inflater = LayoutInflater.from(parent.context)
//                val binding : BasicItemViewholderBinding = BasicItemViewholderBinding.inflate(inflater,parent,false)
//                return ViewHolder(binding)
//            }
//        }
//    }
//
//}
//
//
//// DiffUtil ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#3
//class BasicItemDiffCallback: DiffUtil.ItemCallback<BasicItem>(){
//    override fun areItemsTheSame(oldItem: BasicItem, newItem: BasicItem): Boolean {
//        return oldItem.name == newItem.name
//    }
//
//    override fun areContentsTheSame(oldItem: BasicItem, newItem: BasicItem): Boolean {
//        return oldItem == newItem
//    }
//
//}
//
//
//
