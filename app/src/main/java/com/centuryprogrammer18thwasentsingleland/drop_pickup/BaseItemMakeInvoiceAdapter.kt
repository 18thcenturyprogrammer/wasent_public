package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.data.BaseItem
import com.centuryprogrammer18thwasentsingleland.databinding.BaseitemMakeInvoiceViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.BaseitemViewholderBinding
import com.centuryprogrammer18thwasentsingleland.manager.BaseItemDiffCallback
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialString

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class BaseItemMakeInvoiceAdapter(val actViewModel : MakeInvoiceActVM) : androidx.recyclerview.widget.ListAdapter<BaseItem,BaseItemMakeInvoiceViewHolder>(
    BaseItemDiffCallback()
){
    private val TAG = BaseItemMakeInvoiceAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseItemMakeInvoiceViewHolder {
        val viewholder = BaseItemMakeInvoiceViewHolder.from(parent, actViewModel)

        return viewholder
    }

    override fun onBindViewHolder(holder: BaseItemMakeInvoiceViewHolder, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder binding process started +++++")

        val item = getItem(position)
        holder.bind(item,position)

    }
}

class BaseItemMakeInvoiceViewHolder(val binding: BaseitemMakeInvoiceViewholderBinding) : RecyclerView.ViewHolder(binding.root){
    private val TAG = BaseItemMakeInvoiceViewHolder::class.java.simpleName

    fun bind(basicItem : BaseItem, position: Int){
        Log.d(TAG,"+++++ bind binding process +++++")


        val tag = position
        Log.d(TAG,"+++++ created position Tag is '${tag.toString()}' +++++")

        binding.tvNameBaseItemMakeInvoiceVH.text = basicItem.name
        binding.tvPriceBaseItemMakeInvoiceVH.setText("$"+makeTwoPointsDecialString(basicItem.price))
        binding.tvNumPieceBaseItemMakeInvoiceVH.text = basicItem.numPiece.toString()
        binding.tvProcessBaseItemMakeInvoiceVH.text = basicItem.process

    }

    companion object{
        fun from(parent: ViewGroup, actViewModel: MakeInvoiceActVM):BaseItemMakeInvoiceViewHolder{
            val inflater = LayoutInflater.from(parent.context)
            val binding : BaseitemMakeInvoiceViewholderBinding = BaseitemMakeInvoiceViewholderBinding.inflate(inflater,parent,false)

            binding.actViewModel = actViewModel
            return BaseItemMakeInvoiceViewHolder(binding)
        }
    }
}