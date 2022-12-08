//package com.centuryprogrammer18thwasentsingleland.drop_pickup.paging_not_used
//
//import com.centuryprogrammer18thwasentsingleland.drop_pickup.InvoiceOrderBriefTaskBoardApt
//import com.centuryprogrammer18thwasentsingleland.drop_pickup.TaskBoardFragmentArgs
//import com.centuryprogrammer18thwasentsingleland.drop_pickup.TaskBoardFragmentDirections
//import com.centuryprogrammer18thwasentsingleland.drop_pickup.TaskBoardFragmentVM
//
//
//import android.content.SharedPreferences
//import android.os.Bundle
//import android.preference.PreferenceManager
//import android.util.Log
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.databinding.DataBindingUtil
//import androidx.fragment.app.activityViewModels
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.Observer
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.centuryprogrammer18thwasentsingleland.R
//import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrderBrief
//import com.centuryprogrammer18thwasentsingleland.databinding.FragmentTaskBoardBinding
//

//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"
//
///**
// * A simple [Fragment] subclass.
// * Use the [TaskBoardFragment.newInstance] factory method to
// * create an instance of this fragment.
// */
//class TaskBoardFragment : Fragment() {
//    private val TAG = TaskBoardFragment::class.java.simpleName
//
//    private lateinit var args : TaskBoardFragmentArgs
//
//    private lateinit var binding : FragmentTaskBoardBinding
//
//    private lateinit var sharedPrefs : SharedPreferences
//
//    private val viewModel : TaskBoardFragmentVM by viewModels()
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        Log.d(TAG,"***** onCreate *****")
//        super.onCreate(savedInstanceState)
//
//        args = TaskBoardFragmentArgs.fromBundle(requireArguments())
//        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
//        viewModel.setSharedPrefs(sharedPrefs)
//        viewModel.setCustomer(args.customer)
//        viewModel.initDataSource()
//    }
//
//    override fun onCreateView(
//
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        Log.d(TAG,"***** onCreateView *****")
//
//        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_task_board,container,false)
//        binding.frg = this
//        binding.viewModel = viewModel
//
//        with(binding){
//            tvFirstNameTaskBoardFrg.text = args.customer.firstName.toString()
//            tvLastNameTaskBoardFrg.text = args.customer.lastName.toString()
//            tvPhoneTaskBoardFrg.text = args.customer.phoneNum.toString()
//            tvEmailTaskBoardFrg.text =args.customer.email.toString()
//        }
//
//        val adapter = InvoiceOrderBriefTaskBoardApt(viewModel,this)
//
//        binding.rvHistoryTaskBoardFrg.adapter = adapter
//        binding.rvHistoryTaskBoardFrg.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
//
//        viewModel.invoiceOrderBriefs.observe(viewLifecycleOwner, Observer {
//            if(it != null){
//                Log.d(TAG,"***** invoiceOrderBriefs updated and has values *****")
//                Log.d(TAG,"***** invoiceOrderBriefs value : '${it.toString()}' *****")
//
//                adapter.submitList(it)
//                adapter.notifyDataSetChanged()
//            }
//        })
//
//
//        return binding.root
//    }
//
//    fun onBtnInvoiceClicked(){
//        Log.d(TAG,"***** onBtnInvoiceClicked *****")
//
//        val action = TaskBoardFragmentDirections.actionTaskBoardFragmentToMakeInvoiceActivity(args.customer)
//        findNavController().navigate(action)
//    }
//
//    // user clicked one of InvoiceOrderBrief , send user InvoiceWorkFrg
//    fun onInovoiceOrderBriedClicked(invoiceOrderBrief: InvoiceOrderBrief){
//        val action = TaskBoardFragmentDirections.actionTaskBoardFragmentToInvoiceWorksFrg(args.customer,invoiceOrderBrief)
//        findNavController().navigate(action)
//    }
//
//}