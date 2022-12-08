package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.FrgBaseItemMakeInvoiceBinding


class BaseItemMakeInvoiceFrg : Fragment() {

    private val TAG = BaseItemMakeInvoiceFrg::class.java.simpleName

    private lateinit var binding : FrgBaseItemMakeInvoiceBinding

    // this is activity scope view model
    private lateinit var actViewModel: MakeInvoiceActVM
    private val viewModel: BaseItemMakeInvoiceFrgVM by viewModels()

    private var selectedBaseItem : String = "dryclean_press"
    private var selectedPartilBaseItem : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        actViewModel = (activity as MakeInvoiceActivity).viewModel
        viewModel.actViewModel = actViewModel

        binding = DataBindingUtil.inflate(inflater,R.layout.frg_base_item_make_invoice, container,false)

        binding.viewModel = viewModel
        binding.frg = this

        val adapter = BaseItemMakeInvoiceAdapter(actViewModel)
        binding.rvBaseItemMakeInvoice.adapter = adapter
        binding.rvBaseItemMakeInvoice.layoutManager = GridLayoutManager(requireContext(),5, GridLayoutManager.VERTICAL,false)

        actViewModel.drycleanBaseItems.observe(viewLifecycleOwner, Observer {
            viewModel.chooseDryPress()

            // set base item radio group as default
            setDefaultBaseItemRBTNG()

            // set partial base item radio group as default
            setDefaultPartialBaseItemRBTNG()
        })


        // for user select one of radio button which in base item [dryclean , wetclean , alteration only ]
        binding.rbtngBaseItemMakeInvoice.setOnCheckedChangeListener { radioGroup, btnId ->

            // set partial radio group as default
            setDefaultPartialBaseItemRBTNG()


            when (btnId) {
                R.id.rbtDrycleanBaseItemMakeInvoice -> {
                    Log.d(TAG, "----- rbtDrycleanBaseItemMakeInvoice selected -----")

                    // set flag as user choose dryclean
                    selectedBaseItem = "dryclean_press"

                    // let user can choose partial base item
                    binding.rbtngPartialBaseItemMakeInvoice.visibility = View.VISIBLE

                    viewModel.chooseDryPress()
                }

                R.id.rbtWetcleanBaseItemMakeInvoice -> {
                    Log.d(TAG, "----- rbtWetcleanBaseItemMakeInvoice selected -----")

                    // set flag as user chose wetclean
                    selectedBaseItem = "wetclean_press"

                    // let user can choose partial base item
                    binding.rbtngPartialBaseItemMakeInvoice.visibility = View.VISIBLE

                    viewModel.chooseWetPress()
                }
                R.id.rbtAOBaseItemMakeInvoice -> {
                    Log.d(TAG, "----- rbtAOBaseItemMakeInvoice selected -----")

                    // set flag as user chose alteration only
                    selectedBaseItem = "alteration_only"

                    // there is no partial base item for alteration only
                    binding.rbtngPartialBaseItemMakeInvoice.visibility = View.INVISIBLE

                    // data load
                    viewModel.chooseAlterationOnly()
                }

            }
        }

        // for user select partial base item [clean_only, press_only]
        binding.rbtngPartialBaseItemMakeInvoice.setOnCheckedChangeListener { radioGroup, btnId ->
            when(btnId){
                R.id.rbtnCOPartialBaseItemMakeInvoice -> {
                    Log.d(TAG, "----- rbtnCOPartialBaseItemMakeInvoice selected -----")

                    // set flag as user chose clean only
                    selectedPartilBaseItem = "clean_only"

                    // check what user selected in base item radio group and data load
                    when(selectedBaseItem){
                        "dryclean_press" ->{
                            viewModel.chooseDryCleanOnly()
                        }
                        "wetclean_press" ->{
                            viewModel.chooseWetCleanOnly()
                        }
                    }
                }
                R.id.rbtnPOPartialBaseItemMakeInvoice -> {
                    Log.d(TAG, "----- rbtnPOPartialBaseItemMakeInvoice selected -----")
                    // set flag as user chose press only
                    selectedPartilBaseItem = "press_only"

                    // check what user selected in base item radio group and data load
                    when(selectedBaseItem){
                        "dryclean_press" ->{
                            viewModel.chooseDryPressOnly()
                        }
                        "wetclean_press" ->{
                            viewModel.chooseWetPressOnly()
                        }
                    }
                }
            }
        }

        // unselectable radio button ref) https://stackoverflow.com/a/32496959/3151712
        binding.rbtnCOPartialBaseItemMakeInvoice.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                if ((view as RadioButton).isChecked) {
                    // set partial radio group as default
                    setDefaultPartialBaseItemRBTNG()

                    when(selectedBaseItem){
                        "dryclean_press" -> {
                            viewModel.chooseDryPress()
                        }
                        "wetclean_press" -> {
                            viewModel.chooseWetPress()
                        }

                    }

                    // Prevent the system from re-checking it
                    return true
                }
                return false
            }
        })

        // unselectable radio button ref) https://stackoverflow.com/a/32496959/3151712
        binding.rbtnPOPartialBaseItemMakeInvoice.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                if ((view as RadioButton).isChecked) {

                    // set partial radio group as default
                    setDefaultPartialBaseItemRBTNG()

                    when(selectedBaseItem){
                        "dryclean_press" -> {
                            viewModel.chooseDryPress()
                        }
                        "wetclean_press" -> {
                            viewModel.chooseWetPress()
                        }

                    }

                    // Prevent the system from re-checking it
                    return true
                }
                return false
            }
        })


        viewModel.itemsWillShown.observe(viewLifecycleOwner, Observer {
            if(it.count() > 0){
                adapter.submitList(ArrayList(it.values))
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


    // set base item radio button group as default
    fun setDefaultBaseItemRBTNG(){
        // If the button was already checked, uncheck them all
        binding.rbtngBaseItemMakeInvoice.check(R.id.rbtDrycleanBaseItemMakeInvoice)

        // set flag as user chose dryclean and press
        selectedBaseItem = "dryclean_press"
    }

    // set partial base item radio button group as default
    fun setDefaultPartialBaseItemRBTNG(){
        // If the button was already checked, uncheck them all
        binding.rbtngPartialBaseItemMakeInvoice.clearCheck()

        // set flag as user chose nothing
        selectedPartilBaseItem = ""
    }


    // called from frg_base_item_make_invoice.xml
    fun onClickTempAdd(){

        // send user to make temp Fabricare
        val action = BaseItemMakeInvoiceFrgDirections.actionBaseItemMakeInvoiceFrgToFabricareAddDialogFrg()
        findNavController().navigate(action)
    }


    fun onClickAddItem(){
        Log.d(TAG,"----- onClick() -----")

        val action = BaseItemMakeInvoiceFrgDirections.actionBaseItemMakeInvoiceFrgToItemAddDialogFragment2()
        findNavController().navigate(action)
    }

}