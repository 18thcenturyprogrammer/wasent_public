package com.centuryprogrammer18thwasentsingleland.manager

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.BaseItem
import com.centuryprogrammer18thwasentsingleland.databinding.BaseitemViewholderBinding
import com.centuryprogrammer18thwasentsingleland.utils.*


// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class BaseItemAdapter(val baseItemFrgAdapterInterface: BaseItemFrgAdapterInterface, val baseItemVMAdapterInterface: BaseItemVMAdapterInterface) : androidx.recyclerview.widget.ListAdapter<BaseItem,BaseItemViewHolder>(BaseItemDiffCallback()){
    private val TAG = BaseItemAdapter::class.java.simpleName

    private val controlEtBtn = ControlEtBtn()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseItemViewHolder {
        val viewholder = BaseItemViewHolder.from(parent, baseItemFrgAdapterInterface )

        // adding textwatcher for currency format in EditText
        viewholder.binding.etPriceBaseItemViewholder.addTextChangedListener(
            CurrencyTextWatcher(viewholder.binding.etPriceBaseItemViewholder))


        // ref) adding listener https://stackoverflow.com/a/39271313/3151712
        viewholder.binding.btnEditBaseItemViewholder.setOnClickListener {
            Log.d(TAG,"+++++ EDIT BUTTON CLICKED +++++")

            // tag has poistion index number
            val tag = it.getTag() as Int
            Log.d(TAG,"+++++ tag is '${tag.toString()}' +++++")

            controlEtBtn.userWorkingOn(tag)

            // refresh recycler view for changing viewholder style
            notifyDataSetChanged()
        }

        viewholder.binding.btnOkBaseItemViewholder.setOnClickListener {
            Log.d(TAG,"+++++ ok BUTTON CLICKED +++++")

            val tag = it.getTag() as Int
            Log.d(TAG,"+++++ tag is '${tag.toString()}' +++++")


            val name = viewholder.binding.tvNameBaseItemViewholder.text.toString()
            val numPiece = viewholder.binding.tvNumPieceBaseItemViewholder.text.toString().toInt()
            val process = viewholder.binding.tvProcessBaseItemViewholder.text.toString()
            val price = viewholder.binding.etPriceBaseItemViewholder.text.toString().replace("[$,]".toRegex(), "").toDouble()
            

            val key = name+"_"+process

            val baseItem = BaseItem(name,numPiece,process,price)

            baseItemVMAdapterInterface.onOkBtnClicked(key, baseItem)
            baseItemFrgAdapterInterface.onShowSoftKeyboard(false,viewholder.binding.etPriceBaseItemViewholder)

            controlEtBtn.doneWork()
            notifyDataSetChanged()
        }

        return viewholder

    }

    override fun onBindViewHolder(holder: BaseItemViewHolder, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder binding process started +++++")

        val item = getItem(position)
        holder.bind(item,position,controlEtBtn)

    }
}

class BaseItemViewHolder(val binding: BaseitemViewholderBinding, val baseItemFrgAdapterInterface:BaseItemFrgAdapterInterface) : RecyclerView.ViewHolder(binding.root){
    private val TAG = BaseItemViewHolder::class.java.simpleName

    fun bind(basicItem : BaseItem, position: Int,controlEtBtn:ControlEtBtn){
        Log.d(TAG,"+++++ bind binding process +++++")
        Log.d(TAG,"+++++ ControlEtBtn.getPositionUserWorkingOn is '${controlEtBtn.getPositionUserWorkingOn().toString()}' +++++")


        val tag = position
        Log.d(TAG,"+++++ created position Tag is '${tag.toString()}' +++++")

        // three rows will be colorPrimary
        // next three rows will be colorThird
        val quotient = position/3
        if(quotient%2 ==0){
            // even number

            binding.cvBaseItemViewholder.setCardBackgroundColor(ContextCompat.getColor(binding.cvBaseItemViewholder.context, R.color.colorPrimary))
        }else{
            // odd number

            binding.cvBaseItemViewholder.setCardBackgroundColor(ContextCompat.getColor(binding.cvBaseItemViewholder.context, R.color.colorThird))
        }

        binding.tvNameBaseItemViewholder.text = basicItem.name

        binding.tvNumPieceBaseItemViewholder.text = basicItem.numPiece.toString()
        binding.tvProcessBaseItemViewholder.text = basicItem.process


        binding.etPriceBaseItemViewholder.setText(makeTwoPointsDecialString(basicItem.price))

        when(controlEtBtn.whatStatusForThis(tag)){
            ControlEtBtn.Status.USER_CLICKED_EDIT_BUTTON -> {
                binding.etPriceBaseItemViewholder.isEnabled = true

                // set focus on edittext programmatically
                binding.etPriceBaseItemViewholder.requestFocus()
                baseItemFrgAdapterInterface.onShowSoftKeyboard(true,binding.etPriceBaseItemViewholder)

                binding.btnEditBaseItemViewholder.isEnabled = false
                binding.btnOkBaseItemViewholder.isEnabled = true
            }
            else ->{
                binding.etPriceBaseItemViewholder.isEnabled = false
                binding.btnEditBaseItemViewholder.isEnabled = true
                binding.btnOkBaseItemViewholder.isEnabled = false
            }
        }


//        ref) tag viewholder recyclerview https://stackoverflow.com/a/5291891/3151712
//        setting up tag
        binding.etPriceBaseItemViewholder.setTag(tag)
        binding.btnEditBaseItemViewholder.setTag(tag)
        binding.btnOkBaseItemViewholder.setTag(tag)
    }

    companion object{
        fun from(parent: ViewGroup, baseItemFrgAdapterInterface:BaseItemFrgAdapterInterface):BaseItemViewHolder{
            val inflater = LayoutInflater.from(parent.context)
            val binding : BaseitemViewholderBinding = BaseitemViewholderBinding.inflate(inflater,parent,false)

            return BaseItemViewHolder(binding, baseItemFrgAdapterInterface)
        }
    }
}



// DiffUtil ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#3
class BaseItemDiffCallback: DiffUtil.ItemCallback<BaseItem>(){
    override fun areItemsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean {
        return oldItem.name+"_"+oldItem.process == newItem.name+"_"+newItem.process
    }

    override fun areContentsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean {
        return oldItem == newItem
    }

}


class ControlEtBtn{

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



