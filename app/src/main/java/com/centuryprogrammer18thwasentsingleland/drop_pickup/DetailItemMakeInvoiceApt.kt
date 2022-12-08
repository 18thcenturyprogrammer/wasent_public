package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.MergedDetailItem
import com.centuryprogrammer18thwasentsingleland.databinding.DetailItemMakeInvoiceViewholderBinding
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialString

class DetailItemMakeInvoiceApt (val actViewModel : MakeInvoiceActVM, val viewModel: DetailMakeInvoiceFrgVM, val frg:DetailMakeInvoiceFrg): androidx.recyclerview.widget.ListAdapter<MergedDetailItem,DetailItemMakeInvoiceVH>(DetailItemDiffCallback()){
    private val TAG = DetailItemMakeInvoiceApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailItemMakeInvoiceVH {
//        Log.d(TAG,"+++++ onCreateViewHolder +++++")

        return DetailItemMakeInvoiceVH.from(parent)
    }

    override fun onBindViewHolder(holder: DetailItemMakeInvoiceVH, position: Int) {
//        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)
        holder.bind(item, position, actViewModel, viewModel, frg)
    }
}

class DetailItemMakeInvoiceVH(val binding: DetailItemMakeInvoiceViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = DetailItemMakeInvoiceVH::class.java.simpleName

    fun bind(mergedDetailItem: MergedDetailItem, position: Int , actViewModel: MakeInvoiceActVM, viewModel: DetailMakeInvoiceFrgVM, frg:DetailMakeInvoiceFrg ){
        Log.d(TAG,"+++++ DetailItemMakeInvoiceVH bind +++++")
        Log.d(TAG,"+++++ ${position.toString()}, name|${mergedDetailItem.name}, category|${mergedDetailItem.category}, baseItemProcess|${mergedDetailItem.baseItemProcess} , rate|${mergedDetailItem.rate}, amount|${mergedDetailItem.amount}+++++")

        binding.mergedDetailItem = mergedDetailItem
        binding.actViewModel = actViewModel
        binding.viewModel = viewModel
        binding.frg = frg

        binding.tvNameDetailItemMakeInvoiceVH.text = mergedDetailItem.name

        mergedDetailItem.selectedFabricare?.fabricare?.let {
            if(mergedDetailItem.rate != null && mergedDetailItem.amount != null){
                val basePrice = it.basePrice!!

                val subCharge =
                    if (mergedDetailItem.rate == 0f ){
                        mergedDetailItem.amount!!
                    }else{
                        basePrice*mergedDetailItem.rate!!
                    }

                binding.tvSubChargeDetailItemMakeInvoiceVH.text = "$"+makeTwoPointsDecialString(subCharge)
            }else{
                binding.tvSubChargeDetailItemMakeInvoiceVH.text = ""
            }
        }
    }


    companion object {
        fun from(parent: ViewGroup): DetailItemMakeInvoiceVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: DetailItemMakeInvoiceViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.detail_item_make_invoice_viewholder,parent,false)
            return DetailItemMakeInvoiceVH(binding)
        }
    }
}

class DetailItemDiffCallback : DiffUtil.ItemCallback<MergedDetailItem>(){
    override fun areItemsTheSame(oldItem: MergedDetailItem, newItem: MergedDetailItem): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: MergedDetailItem, newItem: MergedDetailItem): Boolean {
        return false
    }
}

