package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.data.ColorItem
import com.centuryprogrammer18thwasentsingleland.databinding.ColorItemMakeInvoiceViewholderBinding


// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class ColorItemApt(val actViewModel : MakeInvoiceActVM) : androidx.recyclerview.widget.ListAdapter<ColorItem,ColorItemViewHolder>(
    ColorItemDiffCallback()
){
    private val TAG = ColorItemApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorItemViewHolder {
        val viewholder = ColorItemViewHolder.from(parent, actViewModel)

        return viewholder
    }

    override fun onBindViewHolder(holder: ColorItemViewHolder, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder binding process started +++++")

        val item = getItem(position)
        holder.bind(item,position)
    }
}

class ColorItemViewHolder(val binding: ColorItemMakeInvoiceViewholderBinding) : RecyclerView.ViewHolder(binding.root){
    private val TAG = BaseItemMakeInvoiceViewHolder::class.java.simpleName

    fun bind( colorItem:ColorItem , position: Int){
        val tag = position

        binding.colorItem =colorItem

        // display color as background color
        binding.cvColorItemMakeInvoiceVM.setCardBackgroundColor(colorItem.colorNumber)

        // display color name
        binding.tvNameColorItemMakeInvoiceVM.text = colorItem.name


    }

    companion object{
        fun from(parent: ViewGroup, actViewModel: MakeInvoiceActVM):ColorItemViewHolder{
            val inflater = LayoutInflater.from(parent.context)
            val binding : ColorItemMakeInvoiceViewholderBinding = ColorItemMakeInvoiceViewholderBinding.inflate(inflater,parent,false)

            binding.viewModel = actViewModel
            return ColorItemViewHolder(binding)
        }
    }
}

// DiffUtil ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#3
class ColorItemDiffCallback: DiffUtil.ItemCallback<ColorItem>(){
    override fun areItemsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean {
        return false
    }

}