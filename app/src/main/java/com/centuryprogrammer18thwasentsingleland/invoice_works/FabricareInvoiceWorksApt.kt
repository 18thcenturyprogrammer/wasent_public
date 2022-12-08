package com.centuryprogrammer18thwasentsingleland.invoice_works

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Fabricare
import com.centuryprogrammer18thwasentsingleland.databinding.FabricareInvoiceWorksViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.FabricareMakeInvoiceViewholderBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.BaseItemMakeInvoiceViewHolder
import com.centuryprogrammer18thwasentsingleland.drop_pickup.FabricareDiffCallback
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialString

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class FabricareInvoiceWorksApt : androidx.recyclerview.widget.ListAdapter<Fabricare,FabricareInvoiceWorksVH>(
    FabricareDiffCallback()
){
    private val TAG = FabricareInvoiceWorksApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FabricareInvoiceWorksVH {
        val viewholder = FabricareInvoiceWorksVH.from(parent)
        return viewholder
    }

    override fun onBindViewHolder(holder: FabricareInvoiceWorksVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder binding process started +++++")

        val item = getItem(position)

        holder.bind(item,position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}

class FabricareInvoiceWorksVH(val binding: FabricareInvoiceWorksViewholderBinding) : RecyclerView.ViewHolder(binding.root){
    private val TAG = BaseItemMakeInvoiceViewHolder::class.java.simpleName

    fun bind(fabricare : Fabricare, position: Int){
        Log.d(TAG,"+++++ bind binding process +++++")

        binding.cvFabricareMakeInvoiceVM.setBackgroundResource(R.color.colorPrimary)
        binding.tvNumPieceFabricareInvoiceWorksVH.text = fabricare.numPiece.toString()
        binding.tvNameFabricareInvoiceWorksVH.text = fabricare.name
        binding.tvProcessFabricareInvoiceWorksVH.text = getProcessAbbr(fabricare.process)

        var detailFabricares = ""
        for (detail in fabricare.fabricareDetails){

            var detailPriceStr = ""
            if (detail.price != 0.0){
                detailPriceStr = ":"+ makeTwoPointsDecialString(detail.price)
            }

            detailFabricares += "[${detail.name}${detailPriceStr}]"
        }
        binding.tvDetailsFabricareInvoiceWorksVH.text = detailFabricares
        binding.tvBasePriceFabricareInvoiceWorksVH.text = "$"+fabricare.basePrice.toString()
        binding.tvSumFabricareInvoiceWorksVH.text = makeTwoPointsDecialString(fabricare.subTotalPrice)

    }

    companion object{
        fun from(parent: ViewGroup):FabricareInvoiceWorksVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding : FabricareInvoiceWorksViewholderBinding = FabricareInvoiceWorksViewholderBinding.inflate(inflater,parent,false)


            return FabricareInvoiceWorksVH(binding)
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