package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.FrgDetailMakeInvoiceBinding

class DetailMakeInvoiceFrg : Fragment() {
    private val TAG = DetailMakeInvoiceFrg::class.java.simpleName

    private lateinit var binding: FrgDetailMakeInvoiceBinding

    // this is activity scope view model
    private lateinit var actViewModel: MakeInvoiceActVM

    private val viewModel: DetailMakeInvoiceFrgVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        actViewModel = (activity as MakeInvoiceActivity).viewModel
        viewModel.actViewModel = actViewModel

        binding =
            DataBindingUtil.inflate(inflater, R.layout.frg_detail_make_invoice, container, false)

        binding.frg = this
        binding.viewModel = viewModel

        val adapter = DetailItemMakeInvoiceApt(actViewModel,viewModel,this)

        // recyclerview will show MergedDetailItem
        // MergedDetailItem has DetailItem data, DetailBaseItem data combined.
        // mergedDetailItems will have all DetailItem and DetailBaseItem depend on whether there is DetailBaseItem belong to BaseItem
        binding.rvDetailItemMakeInvoice.adapter = adapter
        binding.rvDetailItemMakeInvoice.layoutManager =
            GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)


        actViewModel.detailItems.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "***** actViewModel detailItems changed detected in DetailMakeInvoiceFrg *****")

            for(item in it){
                Log.d(TAG, "*****  key|${item.key} , name|${item.value.name} , category|${item.value.category} *****")
            }


            viewModel.updateDetailItems(it)

            // 중복이라 예상된다. viewModel의 DetailItems를 수정하면 아래의 viewModel.detailItems.observe()호출되기 때문
//            viewModel.updateMergedDetailItems()
        })

        actViewModel.detailBaseItems.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "***** actViewModel detailBaseItems changed detected in DetailMakeInvoiceFrg *****")

            for(item in it){
                Log.d(TAG, "*****  key|${item.key} , name|${item.value.name} , category|${item.value.baseItemProcess}, rate|${item.value.rate}, amount|${item.value.amount} *****")
            }

            viewModel.updateDetailBaseItems(it)
            viewModel.updateMergedDetailItems("general")

        })

        actViewModel.selectedFabricare.observe(viewLifecycleOwner, Observer {
            // selected fabricare is changed

            // added because error ocurred when i removed all fabricare from list
            if(it.fabricare != null){
                Log.d(TAG, "***** actViewModel selectedFabricare changed detected in DetailMakeInvoiceFrg *****")

                Log.d(TAG, "***** index|${it.index} , name|${it.fabricare!!.name}, process|${it.fabricare!!.process} , subTotalPrice|${it.fabricare!!.subTotalPrice} *****")


                viewModel.updateSelectedFabricare(it)
                viewModel.updateMergedDetailItems("general")
            }
        })



        viewModel.detailItems.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "***** viewModel itemsWillShown changed detected in DetailMakeInvoiceFrg *****")
            for(item in it){
                Log.d(TAG, "*****  key|${item.key} , name|${item.value.name} , category|${item.value.category} *****")
            }

            viewModel.updateMergedDetailItems("general")

        })

        viewModel.mergedDetailItems.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "***** viewModel mergedDetailItems changed detected in DetailMakeInvoiceFrg *****")
            for(item in it){
                Log.d(TAG, "*****  key|${item.key} , name|${item.value.name}} , category|${item.value.category}}, baseItemProcess|${item.value.baseItemProcess}}, rate|${item.value.rate}}, amount|${item.value.amount}} *****")
            }

            adapter.submitList(ArrayList(it.values))
            adapter.notifyDataSetChanged()
        })

        // user clicked MergedBaseItem ,but it doesn't have DetailBaseItem , so we need to send user to add dialog to add new one
        viewModel.newDeatilBaseItem.observe(viewLifecycleOwner, Observer {
            it?.let {
                val action = DetailMakeInvoiceFrgDirections.actionDetailMakeInvoiceFrgToDetailBaseItemAddDialogFrg(it)
                findNavController().navigate(action)
                viewModel.setNullNewDeatilBaseItem()
            }
        })


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    // this is called from frg_detail_make_invoice.xml
    fun onClickAddDetail(){
        val action = DetailMakeInvoiceFrgDirections.actionDetailMakeInvoiceFrgToDetailItemAddDialogFrg2()
        findNavController().navigate(action)
    }


}