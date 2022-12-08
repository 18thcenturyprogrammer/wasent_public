//package com.centuryprogrammer18thwasentsingleland.drop_pickup.paging_not_used
//
//import android.graphics.Color
//import android.text.method.ScrollingMovementMethod
//import android.util.Log
//import android.view.LayoutInflater`
//import android.view.ViewGroup
//import androidx.core.content.ContextCompat
//import androidx.databinding.DataBindingUtil
//import androidx.paging.PagedListAdapter
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.RecyclerView
//import com.centuryprogrammer18thwasentsingleland.App
//import com.centuryprogrammer18thwasentsingleland.R
//import com.centuryprogrammer18thwasentsingleland.data.BaseItem
//import com.centuryprogrammer18thwasentsingleland.data.Customer
//import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrderBrief
//import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceOrderBriefTaskBoardViewholderBinding
//import com.centuryprogrammer18thwasentsingleland.databinding.SearchedCustomerStartViewholderBinding
//
//// recycler view adpater
//// using DiffUtil and ListAdapter
//// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
//// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt
//
//class InvoiceOrderBriefTaskBoardApt (val viewModel: TaskBoardFragmentVM, val frg:TaskBoardFragment): PagedListAdapter<InvoiceOrderBrief,InvoiceOrderBriefTaskBoardVH>(InvoiceOrderBriefTaskBoardDiffCallback()){
//    private val TAG = InvoiceOrderBriefTaskBoardApt::class.java.simpleName
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceOrderBriefTaskBoardVH {
//        Log.d(TAG,"+++++ onCreateViewHolder +++++")
//
//
//        val viewholder = InvoiceOrderBriefTaskBoardVH.from(parent)
//
//        viewholder.binding.loTaskBoardVH.setOnClickListener {layout ->
//            val index = layout.getTag() as Int
//
//            val clickedInvoiceOrderBrief = getItem(index)
//
//            frg.onInovoiceOrderBriedClicked(clickedInvoiceOrderBrief!!)
//
//        }
//
//        return viewholder
//    }
//
//    override fun onBindViewHolder(holder: InvoiceOrderBriefTaskBoardVH, position: Int) {
//        Log.d(TAG,"+++++ onBindViewHolder +++++")
//
//        val item = getItem(position)
//        item?.let {
//            holder.bind(it, position, viewModel, frg)
//        }
//
//    }
//}
//
//class InvoiceOrderBriefTaskBoardVH(val binding: InvoiceOrderBriefTaskBoardViewholderBinding): RecyclerView.ViewHolder(binding.root){
//    private val TAG = InvoiceOrderBriefTaskBoardVH::class.java.simpleName
//
//    fun bind(invoiceOrderBrief: InvoiceOrderBrief, position: Int, viewModel: TaskBoardFragmentVM, frg:TaskBoardFragment ){
//        Log.d(TAG,"+++++ DetailItemMakeInvoiceVH bind +++++")
//
//        val tag = position
//
//        binding.viewModel = viewModel
//        binding.frg = frg
//
//        if(invoiceOrderBrief.pickedAtTimestamp == null){
//            // not picked up yet
//
//            // android background color ref) https://stackoverflow.com/q/18033260/3151712
//            binding.loTaskBoardVH.setBackgroundColor(Color.parseColor("#5951c9"))
//        }else{
//            // order has picked up already
//
//            binding.loTaskBoardVH.setBackgroundColor(Color.parseColor("#f759ed"))
//        }
//
//        binding.loTaskBoardVH.setTag(tag)
//
//
//        binding.tvInvoiceIdTaskBoardVH.text = invoiceOrderBrief.invoiceOrderId.toString()
//        binding.tvCreatedAtaskBoardVH.text = invoiceOrderBrief.createdAt
//        binding.tvDueDateTimeTaskBoardVH.text = invoiceOrderBrief.dueDateTime
//        binding.tvPickedUpAtTaskBoardVH.text = invoiceOrderBrief.pickedUpAt
//        binding.tvRackLocationTaskBoardVH.text = invoiceOrderBrief.rackLocation
//        binding.tvBalanceTaskBoardVH.text = "balance"
//        binding.tvTotalQtyTaskBoardVH.text = "qyt total"
//    }
//
//
//    companion object {
//        fun from(parent: ViewGroup): InvoiceOrderBriefTaskBoardVH{
//            val inflater = LayoutInflater.from(parent.context)
//            val binding: InvoiceOrderBriefTaskBoardViewholderBinding = DataBindingUtil.inflate(inflater, R.layout.invoice_order_task_board_viewholder,parent,false)
//            return InvoiceOrderBriefTaskBoardVH(binding)
//        }
//    }
//}
//
//class InvoiceOrderBriefTaskBoardDiffCallback : DiffUtil.ItemCallback<InvoiceOrderBrief>(){
//    override fun areItemsTheSame(oldItem: InvoiceOrderBrief, newItem: InvoiceOrderBrief): Boolean {
//        return oldItem.invoiceOrderId == newItem.invoiceOrderId
//    }
//
//    override fun areContentsTheSame(oldItem: InvoiceOrderBrief, newItem: InvoiceOrderBrief): Boolean {
//        return oldItem == newItem
//    }
//}