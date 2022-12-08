package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceOrderTaskBoardViewholderBinding
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialString
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar


// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class InvoiceOrderTaskBoardApt (val viewModel: TaskBoardFragmentVM, val frg:TaskBoardFragment): ListAdapter<InvoiceOrder, InvoiceOrderTaskBoardVH>(InvoiceOrderDiffCallback()){
    private val TAG = InvoiceOrderTaskBoardApt::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceOrderTaskBoardVH {
        Log.d(TAG,"+++++ onCreateViewHolder +++++")


        val viewholder = InvoiceOrderTaskBoardVH.from(parent)

        viewholder.binding.cvTaskBoardVH.setOnClickListener { layout ->
            val index = layout.getTag() as Int

            val clickedInvoiceOrder = getItem(index)

            frg.onInovoiceOrderClicked(clickedInvoiceOrder!!)

        }

        return viewholder
    }

    override fun onBindViewHolder(holder: InvoiceOrderTaskBoardVH, position: Int) {
        Log.d(TAG,"+++++ onBindViewHolder +++++")

        val item = getItem(position)
        item?.let {
            holder.bind(it, position, viewModel, frg)
        }

    }
}

class InvoiceOrderTaskBoardVH(val binding: InvoiceOrderTaskBoardViewholderBinding): RecyclerView.ViewHolder(binding.root){
    private val TAG = InvoiceOrderTaskBoardVH::class.java.simpleName

    fun bind(invoiceOrder: InvoiceOrder, position: Int, viewModel: TaskBoardFragmentVM, frg:TaskBoardFragment ){
        Log.d(TAG,"+++++ DetailItemMakeInvoiceVH bind +++++")

        val tag = position

        binding.viewModel = viewModel
        binding.frg = frg

        binding.cvTaskBoardVH.setTag(tag)

        binding.tvInvoiceIdTaskBoardVH.text = invoiceOrder.id.toString()
        binding.tvCreatedAtaskBoardVH.text = invoiceOrder.createdAt
        binding.tvDueDateTimeTaskBoardVH.text = invoiceOrder.dueDateTime
        binding.tvRackLocationTaskBoardVH.text = invoiceOrder.rackLocation
        binding.tvBalanceTaskBoardVH.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["balance"]!!)

        val totalQty =
            invoiceOrder.qtyTable!!["dry"]!!+
                    invoiceOrder.qtyTable!!["wet"]!!+
                    invoiceOrder.qtyTable!!["alter"]!!+
                    invoiceOrder.qtyTable!!["press"]!!+
                    invoiceOrder.qtyTable!!["clean"]!!
        binding.tvTotalQtyTaskBoardVH.text = totalQty.toString()
    }


    companion object {
        fun from(parent: ViewGroup): InvoiceOrderTaskBoardVH{
            val inflater = LayoutInflater.from(parent.context)
            val binding: InvoiceOrderTaskBoardViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.invoice_order_task_board_viewholder,parent,false)
            return InvoiceOrderTaskBoardVH(binding)
        }
    }
}

class InvoiceOrderDiffCallback : DiffUtil.ItemCallback<InvoiceOrder>(){
    override fun areItemsTheSame(oldItem: InvoiceOrder, newItem: InvoiceOrder): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: InvoiceOrder, newItem: InvoiceOrder): Boolean {
        return oldItem == newItem
    }
}