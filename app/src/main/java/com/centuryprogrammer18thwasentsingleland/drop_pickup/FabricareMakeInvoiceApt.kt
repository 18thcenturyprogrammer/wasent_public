package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Fabricare
import com.centuryprogrammer18thwasentsingleland.databinding.BaseitemMakeInvoiceViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.FabricareMakeInvoiceViewholderBinding
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialString

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class FabricareMakeInvoiceApt(val actViewModel : MakeInvoiceActVM) : androidx.recyclerview.widget.ListAdapter<Fabricare,FabricareMakeInvoiceViewholder>(
    FabricareDiffCallback()
){
    private val TAG = FabricareMakeInvoiceApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FabricareMakeInvoiceViewholder {
        val viewholder = FabricareMakeInvoiceViewholder.from(parent, actViewModel)
        return viewholder
    }

    override fun onBindViewHolder(holder: FabricareMakeInvoiceViewholder, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder binding process started +++++")
        Log.d(TAG,"+++++ new tag value is '${getItemId(position)}' +++++")

        val item = getItem(position)
        val newTag = getItemId(position).toInt()
        val isSelected =
            position == actViewModel.selectedFabricare.value!!.index

        holder.bind(item,position,newTag,isSelected)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}

class FabricareMakeInvoiceViewholder(val binding: FabricareMakeInvoiceViewholderBinding) : RecyclerView.ViewHolder(binding.root){
    private val TAG = BaseItemMakeInvoiceViewHolder::class.java.simpleName

    fun bind(fabricare : Fabricare, position: Int, tagVal: Int, isSelected:Boolean){
        Log.d(TAG,"+++++ bind binding process +++++")

        if (isSelected){
            binding.cvFabricareMakeInvoiceVM.setBackgroundResource(R.color.colorPrimary)
        }else{
            binding.cvFabricareMakeInvoiceVM.background = Color.TRANSPARENT.toDrawable()
        }

        binding.loFabricareMakeInvoiceVM.tag = tagVal
        binding.tvNameFabricareMakeInvoiceVH.text = fabricare.name
        binding.tvNumPieceFabricareMakeInvoiceVH.text = fabricare.numPiece.toString()
        binding.tvProcessFabricareMakeInvoiceVH.text = getProcessAbbr(fabricare.process)

        var detailFabricares = ""
        for (detail in fabricare.fabricareDetails){

            var detailPriceStr = ""
            if (detail.price != 0.0){
                detailPriceStr = ":"+makeTwoPointsDecialString(detail.price)
            }

            detailFabricares += "[${detail.name}${detailPriceStr}]"
        }
        binding.tvDetailsFabricareMakeInvoiceVH.text = detailFabricares
        binding.tvBasePriceFabricareMakeInvoiceVH.text = "$"+fabricare.basePrice.toString()
        binding.tvSumFabricareMakeInvoiceVH.text = makeTwoPointsDecialString(fabricare.subTotalPrice)

    }

    companion object{
        fun from(parent: ViewGroup, actViewModel: MakeInvoiceActVM):FabricareMakeInvoiceViewholder{
            val inflater = LayoutInflater.from(parent.context)
            val binding : FabricareMakeInvoiceViewholderBinding = FabricareMakeInvoiceViewholderBinding.inflate(inflater,parent,false)

            binding.actViewModel = actViewModel
            return FabricareMakeInvoiceViewholder(binding)
        }
    }


    // make abbreviation
    fun getProcessAbbr(processStr:String):String{
        var process = ""
        if (processStr.endsWith("_dry_clean_press")){
            process = "D.C.P"
        }
        if (processStr.endsWith("_dry_clean")){
            process = "D.C.P"
        }
        if (processStr.endsWith("_dry_press")){
            process = "D.P"
        }
        if (processStr.endsWith("_wet_clean_press")){
            process = "W.C.P"
        }
        if (processStr.endsWith("_wet_clean")){
            process = "W.C"
        }
        if (processStr.endsWith("_wet_press")){
            process = "W.P"
        }
        if (processStr.endsWith("_alter")){
            process = "A"
        }
        return process
    }
}

// DiffUtil ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#3
class FabricareDiffCallback: DiffUtil.ItemCallback<Fabricare>(){
    override fun areItemsTheSame(oldItem: Fabricare, newItem: Fabricare): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: Fabricare, newItem: Fabricare): Boolean {
        return oldItem == newItem
    }

}