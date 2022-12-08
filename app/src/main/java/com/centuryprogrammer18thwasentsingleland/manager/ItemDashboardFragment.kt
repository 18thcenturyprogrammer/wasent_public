package com.centuryprogrammer18thwasentsingleland.manager

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.FragmentItemDashboardBinding

class ItemDashboardFragment : Fragment() {
    private val TAG = ItemDashboardFragment::class.java.simpleName

    private lateinit var binding : FragmentItemDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // i don't need soft keyboard, remove keyboard
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_HIDDEN,0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_dashboard, container, false)

        binding.btnGoItemFragment.setOnClickListener {
            Log.d(TAG,"***** btnGoItemFragment clicked *****")
            val action = ItemDashboardFragmentDirections.actionItemDashboardFragmentToItemFragment()
            findNavController().navigate(action)
        }

        binding.btnGoBaseItemFrg.setOnClickListener {
            Log.d(TAG,"***** btnGoBaseItemFrg clicked *****")
            val action = ItemDashboardFragmentDirections.actionItemDashboardFragmentToBaseItemFragment()
            findNavController().navigate(action)
        }

        binding.btnGoPartialBaseItemFrg.setOnClickListener {
            Log.d(TAG,"***** btnGoPartialBaseItemFrg clicked *****")
            val action = ItemDashboardFragmentDirections.actionItemDashboardFragmentToPartialBaseItemFragment()
            findNavController().navigate(action)
        }

        binding.btnGoDetailItemFrg.setOnClickListener {
            Log.d(TAG,"***** btnGoDetailItemFrg clicked *****")
            val action = ItemDashboardFragmentDirections.actionItemDashboardFragmentToDetailItemFrg()
            findNavController().navigate(action)
        }

        binding.btnGoDetailBaseItemFrg.setOnClickListener {
            Log.d(TAG,"***** btnGoDetailBaseItemFrg clicked *****")

            val action = ItemDashboardFragmentDirections.actionItemDashboardFragmentToDetailBaseItemFrg()
            findNavController().navigate(action)

        }

        return binding.root
    }

}