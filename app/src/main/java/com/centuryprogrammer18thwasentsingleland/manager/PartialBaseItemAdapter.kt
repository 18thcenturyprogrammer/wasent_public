package com.centuryprogrammer18thwasentsingleland.manager

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.PartialBaseItem
import com.centuryprogrammer18thwasentsingleland.databinding.PartialBaseitemViewholderBinding
import com.centuryprogrammer18thwasentsingleland.utils.*

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class PartialBaseItemAdapter(val partialBaseItemAdapterInterface: PartialBaseItemAdapterInterface, val partialBaseItemFrgAptInterface: PartialBaseItemFrgAptInterface) : androidx.recyclerview.widget.ListAdapter<PartialBaseItem,PartialBaseItemViewHolder>(PartialBaseItemDiffCallback()){
    private val TAG = PartialBaseItemAdapter::class.java.simpleName

    private val controlEtBtn = PartialBaseItemControlEtBtn()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartialBaseItemViewHolder {
        Log.d(TAG,"+++++ onCreateViewHolder  +++++")

        val viewholder = PartialBaseItemViewHolder.from(parent)

        // adding textwatcher for signed currency format
        viewholder.binding.etAmountPartialBaseItemViewholder.addTextChangedListener(
            SignedCurrencyTextWatcher(viewholder.binding.etAmountPartialBaseItemViewholder)
        )

        viewholder.binding.btnEditPartialBaseItemViewholder.setOnClickListener {
            Log.d(TAG,"+++++ EDIT BUTTON CLICKED  +++++")

            val tag = it.getTag() as Int

            controlEtBtn.userWorkingOn(tag)

            // refresh recycler view for changing viewholder style
            notifyDataSetChanged()
        }


        viewholder.binding.btnOkPartialBaseItemViewholder.setOnClickListener {
            Log.d(TAG,"+++++ ok BUTTON CLICKED +++++")

            val tag = it.getTag() as Int
            Log.d(TAG,"+++++ tag is '${tag.toString()}' +++++")

            val name = viewholder.binding.tvNamePartialBaseItemViewholder.text.toString()
            val numPiece = viewholder.binding.tvNumPiecePartialBaseItemViewholder.text.toString().toInt()
            val process = viewholder.binding.tvProcessPartialBaseItemViewholder.text.toString()
            val dryPrice = viewholder.binding.tvDryPricePartialBaseItemViewholder.text.toString().replace("Dry $","").toDouble()
            val wetPrice = viewholder.binding.tvWetPricePartialBaseItemViewholder.text.toString().replace("Wet $","").toDouble()



            val rateString = viewholder.binding.etRatePartialBaseItemViewholder.text.toString()
            Log.d(TAG,"+++++ rateString is '${rateString}' +++++")

            val rate = if (rateString.isEmpty()) 0f else rateString.toFloat()



            val amountString = viewholder.binding.etAmountPartialBaseItemViewholder.text.toString()
            Log.d(TAG,"+++++ amountString is '${amountString}' +++++")

            val amount = if (amountString.isEmpty()) 0.0 else amountString.replace("[$,]".toRegex(),"").toDouble()



            val key = name+"_"+process

            val partialBaseItem = PartialBaseItem(name,numPiece,process,rate,amount,dryPrice,wetPrice)

            partialBaseItemAdapterInterface.onOkBtnClicked(key, partialBaseItem)

            // close soft keyboard
            partialBaseItemFrgAptInterface.onShowSoftKeyboard(false, viewholder.binding.etRatePartialBaseItemViewholder)

            controlEtBtn.doneWork()
            notifyDataSetChanged()
        }

        return viewholder

    }

    override fun onBindViewHolder(holder: PartialBaseItemViewHolder, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder binding process started +++++")

        val partialBaseitem = getItem(position)
        holder.bind(partialBaseitem,position,controlEtBtn)

    }

}

class PartialBaseItemViewHolder(val binding: PartialBaseitemViewholderBinding) : RecyclerView.ViewHolder(binding.root){
    private val TAG = PartialBaseItemViewHolder::class.java.simpleName

    fun bind(partialBaseItem : PartialBaseItem, position: Int, controlEtBtn:PartialBaseItemControlEtBtn){
        Log.d(TAG,"+++++ bind binding process +++++")
        Log.d(TAG,"+++++ ControlEtBtn.getPositionUserWorkingOn is '${controlEtBtn.getPositionUserWorkingOn().toString()}' +++++")


        val tag = position
        Log.d(TAG,"+++++ created position Tag is '${tag.toString()}' +++++")

        // two rows will be colorPrimary
        // next two rows will be colorThird
        val quotient = position/2
        if(quotient%2 ==0){
            // even number

            binding.cvPartialBaseItemViewholder.setCardBackgroundColor(ContextCompat.getColor(binding.cvPartialBaseItemViewholder.context, R.color.colorPrimary))
        }else{
            // odd number

            binding.cvPartialBaseItemViewholder.setCardBackgroundColor(ContextCompat.getColor(binding.cvPartialBaseItemViewholder.context, R.color.colorThird))
        }

        binding.tvNamePartialBaseItemViewholder.text = partialBaseItem.name
        binding.tvNumPiecePartialBaseItemViewholder.text = partialBaseItem.numPiece.toString()
        binding.tvProcessPartialBaseItemViewholder.text = partialBaseItem.process

        binding.tvDryPricePartialBaseItemViewholder.text = "Dry $"+makeTwoPointsDecialString(partialBaseItem.drycleanPressPrice)
        binding.tvWetPricePartialBaseItemViewholder.text = "Wet $"+makeTwoPointsDecialString(partialBaseItem.wetcleanPressPrice)

        binding.etRatePartialBaseItemViewholder.setText(partialBaseItem.rate.toString())

        binding.etAmountPartialBaseItemViewholder.setText(makeTwoPointsDecialString(partialBaseItem.amount))

        if(partialBaseItem.rate != 0.0f){
            binding.tvCalDryPricePartialBaseItemViewholder.text = "$"+makeTwoPointsDecialString(partialBaseItem.drycleanPressPrice + (partialBaseItem.drycleanPressPrice * partialBaseItem.rate))
        }else{
            binding.tvCalDryPricePartialBaseItemViewholder.text = "$"+makeTwoPointsDecialString(partialBaseItem.drycleanPressPrice + partialBaseItem.amount)
        }

        if(partialBaseItem.rate != 0.0f){
            binding.tvCalWetPricePartialBaseItemViewholder.text = "$"+makeTwoPointsDecialString(partialBaseItem.wetcleanPressPrice + (partialBaseItem.wetcleanPressPrice * partialBaseItem.rate))
        }else{
            binding.tvCalWetPricePartialBaseItemViewholder.text = "$"+makeTwoPointsDecialString(partialBaseItem.wetcleanPressPrice + partialBaseItem.amount)
        }


        when(controlEtBtn.whatStatusForThis(tag)){
            PartialBaseItemControlEtBtn.Status.USER_CLICKED_EDIT_BUTTON -> {
                Log.d(TAG,"+++++ ControlEtBtn.Status.USER_CLICKED_EDIT_BUTTON +++++")

                binding.etAmountPartialBaseItemViewholder.isEnabled = true
                binding.etRatePartialBaseItemViewholder.isEnabled = true
                binding.btnEditPartialBaseItemViewholder.isEnabled = false
                binding.btnOkPartialBaseItemViewholder.isEnabled = true

                binding.etRatePartialBaseItemViewholder.setOnFocusChangeListener { view, hasFocus ->
                    if(hasFocus){
                        Log.d(TAG,"+++++ rate editext got focus +++++")
                        val editText = view as EditText
                        editText.setText("")
                        binding.etAmountPartialBaseItemViewholder.setText(0.0.toString())
                    }
                }

                binding.etAmountPartialBaseItemViewholder.setOnFocusChangeListener { view, hasFocus ->
                    if(hasFocus){
                        Log.d(TAG,"+++++ amount editext got focus +++++")
                        val editText = view as EditText
                        editText.setText("")
                        binding.etRatePartialBaseItemViewholder.setText(0.0f.toString())
                    }
                }

            }
            else ->{
                Log.d(TAG,"+++++ ControlEtBtn.Status.NOTHING_HAPPENED +++++")

                binding.etAmountPartialBaseItemViewholder.isEnabled = false
                binding.etRatePartialBaseItemViewholder.isEnabled = false
                binding.btnEditPartialBaseItemViewholder.isEnabled = true
                binding.btnOkPartialBaseItemViewholder.isEnabled = false
            }
        }


//        ref) tag viewholder recyclerview https://stackoverflow.com/a/5291891/3151712
//        setting up tag
        binding.btnEditPartialBaseItemViewholder.setTag(tag)
        binding.btnOkPartialBaseItemViewholder.setTag(tag)
    }

    companion object{
        fun from(parent: ViewGroup):PartialBaseItemViewHolder{
            val inflater = LayoutInflater.from(parent.context)
            val binding:PartialBaseitemViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.partial_baseitem_viewholder,parent,false)

            return PartialBaseItemViewHolder(binding)
        }
    }
}



// DiffUtil ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#3
class PartialBaseItemDiffCallback: DiffUtil.ItemCallback<PartialBaseItem>(){
    override fun areItemsTheSame(oldItem: PartialBaseItem, newItem: PartialBaseItem): Boolean {
        return oldItem.name+"_"+oldItem.process == newItem.name+"_"+newItem.process
    }

    override fun areContentsTheSame(oldItem: PartialBaseItem, newItem: PartialBaseItem): Boolean {
        return oldItem == newItem
    }

}


class PartialBaseItemControlEtBtn{

    //    ref) kotlin enum https://jacob-cs.tumblr.com/post/621497149813882881/kotlin-enum-class
    enum class Status{
        NOTHING_HAPPENED,
        USER_CLICKED_EDIT_BUTTON
    }

    // default value is -1
    // this will indicated which item user working on
    private var positionUserWorkingOn : Int = -1

    private var status : Status = Status.NOTHING_HAPPENED

    fun userWorkingOn(position: Int){
        positionUserWorkingOn = position
        status= Status.USER_CLICKED_EDIT_BUTTON
    }

    fun doneWork(){
        positionUserWorkingOn = -1
        status = Status.NOTHING_HAPPENED
    }

    fun whatStatusForThis(position : Int) : Status{
        if (position == positionUserWorkingOn){
            return Status.USER_CLICKED_EDIT_BUTTON
        }else{
            return Status.NOTHING_HAPPENED
        }
    }

    fun getPositionUserWorkingOn(): Int{
        return positionUserWorkingOn
    }
}