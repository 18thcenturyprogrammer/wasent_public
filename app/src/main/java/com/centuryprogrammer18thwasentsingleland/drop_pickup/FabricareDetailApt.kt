package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Fabricare
import com.centuryprogrammer18thwasentsingleland.data.FabricareDetail
import com.centuryprogrammer18thwasentsingleland.databinding.FabricareDetailViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.FabricareMakeInvoiceViewholderBinding
import com.centuryprogrammer18thwasentsingleland.utils.CurrencyTextWatcher
import com.centuryprogrammer18thwasentsingleland.utils.FabricareDetailDialogCallback
import com.centuryprogrammer18thwasentsingleland.utils.SignedCurrencyTextWatcher
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialString
import kotlinx.android.synthetic.main.fabricare_detail_viewholder.view.*
import kotlinx.android.synthetic.main.manual_dialog_make_invoice.view.*

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class FabricareDetailApt(val callback: FabricareDetailDialogCallback) : androidx.recyclerview.widget.ListAdapter<FabricareDetail,FabricareDetailViewholder>(
    FabricareDetailDiffCallback()
){
    private val TAG = FabricareDetailApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FabricareDetailViewholder {
        val viewholder = FabricareDetailViewholder.from(parent)

        return viewholder
    }

    override fun onBindViewHolder(holder: FabricareDetailViewholder, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder binding process started +++++")
        Log.d(TAG,"+++++ new tag value is '${getItemId(position)}' +++++")

        val item = getItem(position)
        val tag = getItemId(position).toInt()

        with(holder.binding.root){
            tvFabricareDetailVH.text = item.name

            etFabricareDetailVH.tag = tag

            // adding textwatcher for currency format in EditText
            etFabricareDetailVH.addTextChangedListener(
                SignedCurrencyTextWatcher(etFabricareDetailVH)
            )

            etFabricareDetailVH.setText(makeTwoPointsDecialString(item.price))

            Log.d(TAG,"##### item.price | ${item.price.toString()} #####")

            btnChangeFabricareDetailVH.setOnClickListener {
                it.visibility = View.GONE
                etFabricareDetailVH.isEnabled = true
                etFabricareDetailVH.setText("")
                loItemFabricareDetailVH.visibility = View.VISIBLE
            }

            loItemFabricareDetailVH.tag = tag
            btnItemCancelFabricareDetailVH.tag = tag
            btnItemOkFabricareDetailVH.tag = tag

            btnItemCancelFabricareDetailVH.setOnClickListener {
                btnChangeFabricareDetailVH.visibility = View.VISIBLE
                etFabricareDetailVH.isEnabled = false
                loItemFabricareDetailVH.visibility = View.GONE

                callback.onItemCancelCallback()
            }

            btnItemOkFabricareDetailVH.setOnClickListener {
                btnChangeFabricareDetailVH.visibility = View.VISIBLE
                etFabricareDetailVH.isEnabled = false
                loItemFabricareDetailVH.visibility = View.GONE

                if(etFabricareDetailVH.text.isNotEmpty()){
                    callback.onItemOkCallback(position, etFabricareDetailVH.text.replace("[$,]".toRegex(), "").toDouble())
                }else{
                    // user entered empty string, so process as cancelled
                    callback.onItemCancelCallback()
                }


            }
        }

        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}

class FabricareDetailViewholder(val binding: FabricareDetailViewholderBinding) : RecyclerView.ViewHolder(binding.root){
    private val TAG = FabricareDetailViewholder::class.java.simpleName

    fun bind(position: Int){
        Log.d(TAG,"+++++ bind binding process +++++")

    }

    companion object{
        fun from(parent: ViewGroup):FabricareDetailViewholder{
            val inflater = LayoutInflater.from(parent.context)
            val binding : FabricareDetailViewholderBinding = FabricareDetailViewholderBinding.inflate(inflater,parent,false)

            return FabricareDetailViewholder(binding)
        }
    }
}

// DiffUtil ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#3
class FabricareDetailDiffCallback: DiffUtil.ItemCallback<FabricareDetail>(){
    override fun areItemsTheSame(oldItem: FabricareDetail, newItem: FabricareDetail): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: FabricareDetail, newItem: FabricareDetail): Boolean {
        return oldItem == newItem
    }

}