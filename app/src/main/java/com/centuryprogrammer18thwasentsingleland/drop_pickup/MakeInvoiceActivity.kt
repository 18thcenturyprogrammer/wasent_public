package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.conveyor.ConveyorAct
import com.centuryprogrammer18thwasentsingleland.data.FabricareDetail
import com.centuryprogrammer18thwasentsingleland.databinding.*
import com.centuryprogrammer18thwasentsingleland.inventory.InventoryAct
import com.centuryprogrammer18thwasentsingleland.login.ManagerLoginAct
import com.centuryprogrammer18thwasentsingleland.login.ReSignInAct
import com.centuryprogrammer18thwasentsingleland.manager.ItemsPricesActivity
import com.centuryprogrammer18thwasentsingleland.rack.RackAct
import com.centuryprogrammer18thwasentsingleland.release.ReleaseAct
import com.centuryprogrammer18thwasentsingleland.sales.SalesAct
import com.centuryprogrammer18thwasentsingleland.search.SearchAct
import com.centuryprogrammer18thwasentsingleland.singletons.SeletedFabricare
import com.centuryprogrammer18thwasentsingleland.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.coroutines.launch
import java.util.*


class MakeInvoiceActivity : AppCompatActivity(), IOCallBack , FabricareDetailDialogCallback{
    private val TAG = MakeInvoiceActivity::class.java.simpleName

//    safe args ref) https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args
    val args: MakeInvoiceActivityArgs by navArgs()

    private lateinit var binding : ActivityMakeInvoiceBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    val viewModel: MakeInvoiceActVM by viewModels()

    private lateinit var userMakeInvoiceFrg : Fragment

    private var upchargeAlert : AlertDialog? = null
    private var manualAlert : AlertDialog? = null
    private var discountAlert : AlertDialog? = null

    private var manualDetailAlert : AlertDialog? = null
    private var fabricareDetailApt: FabricareDetailApt? = null
    private var tempFabricareDetails: MutableList<FabricareDetail>? = null


    private var numPieceAlert : AlertDialog? = null
    private var commentAlert : AlertDialog? = null
    private var tagAlert : AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check auth.currentUser is null , if currentUser is null, send user to re sign-in
        checkUserLoggedIn()

        setActionBarTitle(getString(R.string.make_invoice))

        Log.d(TAG,"~~~~~ onCreate ~~~~~")
        Log.d(TAG,"~~~~~ args.customer.firstName is ${args.customer.firstName} ~~~~~")

        binding = DataBindingUtil.setContentView(this,R.layout.activity_make_invoice)

        // setting UserMakeInvoiceFrg. this should be called after DataBindingUtil.setContentView
        setFragment()

        initEncrypSharedPrefs()

        binding.viewModel = viewModel
        binding.act = this

        if(!isTeamSignedIn(sharedPrefs)){
            // unauthorized access
            finish()
        }

        viewModel.updateCustomer(args.customer)
        viewModel.updateSharedPrefs(sharedPrefs)

        args.invoiceOrder?.let{
            // there is invoiceOrder coming

            viewModel.initWithInvoiceOrder(it)
            binding.btnHoldMakeInvoiceACT.isEnabled = false
        }



        viewModel.getBaseItemsDetailItems()
        viewModel.makeColorItemList()


        val adapter = FabricareMakeInvoiceApt(viewModel)
        binding.rvFabricaresMakeInvoiceACT.adapter = adapter
        binding.rvFabricaresMakeInvoiceACT.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)

        viewModel.fabricares.observe(this, Observer {
            Log.d(TAG, "***** fabricares is '${it.toString()}' *****")

            adapter.submitList(it)
            adapter.notifyDataSetChanged()

            // move scroll to last
            // scroll recyclerview move add ref) binding.rvFabricaresMakeInvoiceACT.scrollToPosition(it.count()-1)
            binding.rvFabricaresMakeInvoiceACT.scrollToPosition(it.count()-1)
            viewModel.updateQtyPriceStament()

        })

        val colorItemAdapter = ColorItemApt(viewModel)

        binding.rvColorItemMakeInvoiceACT.adapter = colorItemAdapter
        binding.rvColorItemMakeInvoiceACT.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

        viewModel.colorItems.observe(this, Observer {
            colorItemAdapter.submitList(it)
            colorItemAdapter.notifyDataSetChanged()
        })
        
//        binding.tbtnSplitMakeInvoiceACT.setOnCheckedChangeListener { tbtn, isSelected ->
//            if(isSelected){
//                viewModel.
//            }
//        }

        viewModel.messageToActivity.observe(this, Observer {
            if(it.isNotEmpty()){
                Toast(this).showCustomToast(this,it)
            }
        })


        viewModel.selectedFabricare.observe(this, Observer {

            // added because error ocurred when i removed all fabricare from list
            if(it.fabricare != null){
                Log.d(TAG, "***** user select Selectedfabricare value is '${it.toString()}' *****")
                Log.d(TAG, "***** index|${it.index}, name|${it.fabricare!!.name}, process|${it.fabricare!!.process} *****")

                adapter.notifyDataSetChanged()

                binding.rvFabricaresMakeInvoiceACT.smoothScrollToPosition(it.index)
            }
        })

        viewModel.qty.observe(this, Observer {
            binding.tvDryQtyMakeInvoiceACT.text = getString(R.string.dry_qty,it["dry"])
            binding.tvWetQtyMakeInvoiceACT.text = getString(R.string.wet_qty,it["wet"])
            binding.tvAlterationQtyMakeInvoiceACT.text = getString(R.string.alter_qty,it["alter"])
            binding.tvCleanQtyMakeInvoiceACT.text = getString(R.string.clean_qty,it["clean"])
            binding.tvPressQtyMakeInvoiceACT.text = getString(R.string.press_qty,it["clean"])

            val total = it["dry"]!!+it["wet"]!!+it["alter"]!!+it["clean"]!!+it["press"]!!
            binding.tvTotalQtyMakeInvoiceACT.text = getString(R.string.total,total)
        })

        viewModel.priceStatement.observe(this, Observer {
            binding.tvSubtotalMakeInvoiceACT.text = getString(R.string.subtotal_dollar,makeTwoPointsDecialString(it["subTotal"]!!))
            binding.tvDiscountMakeInvoiceACT.text = getString(R.string.discount_dollar,makeTwoPointsDecialString(it["discount"]!!))
            binding.tvTaxMakeInvoiceACT.text = getString(R.string.tax_dollar, makeTwoPointsDecialString(it["tax"]!!))
            binding.tvEnvMakeInvoiceACT.text = getString(R.string.env_charge_dollar, makeTwoPointsDecialString(it["env"]!!))
            binding.tvTotalMakeInvoiceACT.text = getString(R.string.total_dollar, makeTwoPointsDecialString(it["total"]!!))
            binding.tvPrepaidMakeInvoiceACT.text = getString(R.string.prepaid_dollar, makeTwoPointsDecialString(it["prepaid"]!!))
            binding.tvBalanceMakeInvoiceACT.text = getString(R.string.balance_dollar, makeTwoPointsDecialString(it["balance"]!!))

        })
2
        viewModel.closeUpchargeDialog.observe(this, Observer {
            if (it){
                upchargeAlert?.dismiss()
            }
        })

        viewModel.closeManualDialog.observe(this, Observer {
            if (it){
                manualAlert?.dismiss()
            }
        })

        viewModel.closeDiscountDialog.observe(this, Observer {
            if (it){
                discountAlert?.dismiss()
            }
        })

        viewModel.closeNumPieceDialog.observe(this, Observer {
            if (it){
                numPieceAlert?.dismiss()
            }
        })

        viewModel.closeCommentDialog.observe(this, Observer {
            if (it){
                commentAlert?.dismiss()
            }
        })

        viewModel.closeTagDialog.observe(this, Observer {
            if (it){
                tagAlert?.dismiss()
            }
        })


        viewModel.dueDate.observe(this, Observer { dueDate ->
            Log.d(TAG,"***** dueDate selected : dueDate : '${dueDate}' *****")

            if (!dueDate.isEmpty()){
                // show tag dialog , so user can enter tag and color
                showTagDialog()
            }
        })

        viewModel.tag.observe(this, Observer {
            if(it.isNotEmpty()){
                tagAlert?.dismiss()
                binding.btnDiscountMakeInvoiceACT.performClick()
            }
        })

        viewModel.moveToDropPickupACT.observe(this, Observer {
            if(it){
                // successfully new InvoiceOrder added, send user to DropPickupActivity

                val intent = Intent(this, DropPickupActivity::class.java)
                startActivity(intent)
            }
        })

        viewModel.printing.observe(this, Observer {
            if(it.count() > 0 ){
                val mPos = Pos()
                val mUsb = USBPrinting()

                mPos.Set(mUsb)
                mUsb.SetCallBack(this)

                val mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager

                val deviceList = mUsbManager.getDeviceList()

//            this genstar machine has two usb devices, and /dev/bus/usb/002/005 is printer
//            vendor-id="4070" product-id="33054" check in device_filter.xml
//            val deviceA = deviceList["/dev/bus/usb/002/004"]
                val deviceA = deviceList["/dev/bus/usb/002/005"]

                val mPermissionIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(this.getApplicationInfo().packageName),
                    0
                )

                if (!mUsbManager.hasPermission(deviceA)) {

                    Log.d(TAG,"***** has NO permission printing *****")

                    mUsbManager.requestPermission(deviceA, mPermissionIntent)

//							 Permission denied
                    Toast(this).showCustomToast(this,getString(R.string.permission_denied))
                } else {

                    Log.d(TAG,"***** has permission printing *****")

                    mUsb.Open(mUsbManager,deviceA, this)

                    // android coroutine scope ref) https://developer.android.com/topic/libraries/architecture/coroutines#lifecyclescope
                    lifecycleScope.launch {

                        Log.d(TAG,"***** right before GenstarPrint.printTicket *****")
                        Log.d(TAG,"***** printing from VM : ${it.toString()} *****")

                        // makeTopTicket is extension func of Fragment which makes PrintingJob for top part of ticket
//                        val topTicket = makeTopTicket(sharedPrefs)

                        // add part of ticket for dropoff
//                        topTicket.addAll(it)

                        GenstarPrint.printTicket(mPos,576,true,false,true,1, it )
                        mUsb.Close()
                    }
                }
            }
        })



        binding.btnUpchargeMakeInvoiceACT.setOnClickListener(object: View.OnClickListener{

            override fun onClick(p0: View?) {
                // if user choose one BaseItem and remove , viewModel.selectedFabricare is not null, and
                // process will go into if loop and erro will be thrown, so
                // i have to check viewModel.selectedFabricare.value!!.isThereSelectedOne() for making sure if there is selectedFabricare.
                if (viewModel.selectedFabricare.value != null && viewModel.selectedFabricare.value!!.isThereSelectedOne()){
                    val upchargeAlertBuilder = AlertDialog.Builder(this@MakeInvoiceActivity)
                    upchargeAlertBuilder.setMessage(R.string.please_enter_upcharge_amount)
                    upchargeAlertBuilder.setCancelable(false)

                    val upchargeBinding = DataBindingUtil.inflate<UpchargeDialogMakeInvoiceBinding>(layoutInflater,R.layout.upcharge_dialog_make_invoice,null,false)
                    upchargeBinding.viewModel = viewModel
                    upchargeBinding.selectedFabricare = viewModel.selectedFabricare.value

                    // adding text watcher for signed currency
                    upchargeBinding.etUpchargeDialogMakeInvoice.addTextChangedListener(
                        SignedCurrencyTextWatcher(upchargeBinding.etUpchargeDialogMakeInvoice)
                    )
                    upchargeAlertBuilder.setView(upchargeBinding.root)
                    upchargeAlert = upchargeAlertBuilder.create()

                    upchargeAlert?.show()
                }
            }
        })

        binding.btnManualMakeInvoiceACT.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {

                // if user choose one BaseItem and remove it , viewModel.selectedFabricare is not null, and
                // process will go into if loop and error will be thrown, so
                // i have to check viewModel.selectedFabricare.value!!.isThereSelectedOne() for making sure if there is selectedFabricare.
                if (viewModel.selectedFabricare.value != null && viewModel.selectedFabricare.value!!.isThereSelectedOne()){
                    val manualAlertBuilder = AlertDialog.Builder(this@MakeInvoiceActivity)
                    manualAlertBuilder.setMessage(R.string.please_change_price_manually)
                    manualAlertBuilder.setCancelable(false)

                    val manualBinding = DataBindingUtil.inflate<ManualDialogMakeInvoiceBinding>(layoutInflater,R.layout.manual_dialog_make_invoice,null,false)
                    manualBinding.viewModel = viewModel
                    manualBinding.selectedFabricare = viewModel.selectedFabricare.value

                    // adding text watcher for signed currency
                    manualBinding.etManuallDialogMakeInvoice.addTextChangedListener(
                        CurrencyTextWatcher(manualBinding.etManuallDialogMakeInvoice)
                    )
                    manualAlertBuilder.setView(manualBinding.root)
                    manualAlert = manualAlertBuilder.create()

                    manualAlert?.show()
                }
            }
        })

        binding.btnDiscountMakeInvoiceACT.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                Log.d(TAG,"*****  viewModel.selectedFabricare.value : ${viewModel.selectedFabricare.value.toString()} *****")

                // if user choose one BaseItem and remove , viewModel.selectedFabricare is not null, and
                // process will go into if loop and erro will be thrown, so
                // i have to check viewModel.selectedFabricare.value!!.isThereSelectedOne() for making sure if there is selectedFabricare.
                if (viewModel.selectedFabricare.value != null && viewModel.selectedFabricare.value!!.isThereSelectedOne()){
                    val discountAlertBuilder = AlertDialog.Builder(this@MakeInvoiceActivity)
                    discountAlertBuilder.setMessage(R.string.please_enter_discount)
                    discountAlertBuilder.setCancelable(false)

                    val discountBinding = DataBindingUtil.inflate<DiscountDialogMakeInvoiceBinding>(layoutInflater,R.layout.discount_dialog_make_invoice,null,false)
                    discountBinding.viewModel = viewModel
                    discountBinding.selectedFabricare = viewModel.selectedFabricare.value
                    
                    discountBinding.rbtngTypeDCDialogMakeInvoice.setOnCheckedChangeListener { radioGroup, checkedId ->
                        when(checkedId){
                            R.id.rbEntireDCDialogMakeInvoice -> {
                                discountBinding.etAmountDCDialogMakeInvoice.isEnabled = true
                                discountBinding.etRateDCDialogMakeInvoice.isEnabled = true
                            }
                            else -> {
                                discountBinding.etAmountDCDialogMakeInvoice.isEnabled = false
                                discountBinding.etRateDCDialogMakeInvoice.isEnabled = true
                            }
                        }
                    }

                    // adding text watcher for signed currency
                    discountBinding.etAmountDCDialogMakeInvoice.addTextChangedListener(
                        SignedCurrencyTextWatcher(discountBinding.etAmountDCDialogMakeInvoice)
                    )

                    // if etRateDCDialogMakeInvoice has focus, the other etAmountDCDialogMakeInvoice will be zero as default
                    discountBinding.etRateDCDialogMakeInvoice.setOnFocusChangeListener { view, hasFocus ->
                        if(hasFocus){
                            Log.d(TAG,"***** rate editext got focus *****")
                            val editText = view as EditText
                            editText.setText("")
                            discountBinding.etAmountDCDialogMakeInvoice.setText(0.0.toString())
                        }
                    }

                    discountBinding.etAmountDCDialogMakeInvoice.setOnFocusChangeListener { view, hasFocus ->
                        if(hasFocus){
                            Log.d(TAG,"***** amount editext got focus *****")
                            val editText = view as EditText
                            editText.setText("")
                            discountBinding.etRateDCDialogMakeInvoice.setText(0.0f.toString())
                        }
                    }

                    discountAlertBuilder.setView(discountBinding.root)
                    discountAlert = discountAlertBuilder.create()

                    discountAlert?.show()
                }
            }
        })

        binding.btnManualDetailMakeInvoiceACT.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                Log.d(TAG,"***** btnManualDetailMakeInvoiceACT button clicked *****")

                // if user choose one BaseItem and remove it , viewModel.selectedFabricare is not null, and
                // process will go into if loop and error will be thrown, so
                // i have to check viewModel.selectedFabricare.value!!.isThereSelectedOne() for making sure if there is selectedFabricare.
                if (viewModel.selectedFabricare.value != null && viewModel.selectedFabricare.value!!.isThereSelectedOne()){
                    val fabricareDetails = viewModel.selectedFabricare.value!!.fabricare?.fabricareDetails

                    // check if there is FabricareDetail or not
                    if(fabricareDetails != null && fabricareDetails.count() >0 ){

                        // copy clone mutablelist value ref) https://stackoverflow.com/a/51770492/3151712
                        tempFabricareDetails = fabricareDetails?.toMutableList()

                        val manualDetailAlertBuilder = AlertDialog.Builder(this@MakeInvoiceActivity)
                        manualDetailAlertBuilder.setMessage(R.string.please_change_detail_price_manually)
                        manualDetailAlertBuilder.setCancelable(false)

                        val manualDetailBinding = DataBindingUtil.inflate<ManualDetailDialogMakeInvoiceBinding>(layoutInflater,R.layout.manual_detail_dialog_make_invoice,null,false)
                        manualDetailBinding.act = this@MakeInvoiceActivity
                        manualDetailBinding.viewModel = viewModel
                        manualDetailBinding.selectedFabricare = viewModel.selectedFabricare.value

                        fabricareDetailApt = FabricareDetailApt(this@MakeInvoiceActivity)
                        manualDetailBinding.rvManualDetailDialog.adapter = fabricareDetailApt
                        manualDetailBinding.rvManualDetailDialog.layoutManager = LinearLayoutManager(this@MakeInvoiceActivity,LinearLayoutManager.VERTICAL,false)


                        fabricareDetailApt?.submitList(tempFabricareDetails)
                        fabricareDetailApt?.notifyDataSetChanged()


                        manualDetailAlertBuilder.setView(manualDetailBinding.root)
                        manualDetailAlert = manualDetailAlertBuilder.create()

                        manualDetailAlert?.show()

                        manualDetailAlert?.window
                            ?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                        manualDetailAlert?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

//                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)

                    }


                }
            }
        })



        binding.btnNumPieceMakeInvoiceACT.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {

                // if user choose one BaseItem and remove , viewModel.selectedFabricare is not null, and
                // process will go into if loop and erro will be thrown, so
                // i have to check viewModel.selectedFabricare.value!!.isThereSelectedOne() for making sure if there is selectedFabricare.
                if (viewModel.selectedFabricare.value != null && viewModel.selectedFabricare.value!!.isThereSelectedOne()){
                    val numPieceAlertBuilder = AlertDialog.Builder(this@MakeInvoiceActivity)
                    numPieceAlertBuilder.setMessage(R.string.please_num_piece)
                    numPieceAlertBuilder.setCancelable(false)

                    val numPieceBinding = DataBindingUtil.inflate<NumPieceDialogMakeInvoiceBinding>(layoutInflater,R.layout.num_piece_dialog_make_invoice,null,false)
                    numPieceBinding.viewModel = viewModel
                    numPieceBinding.selectedFabricare = viewModel.selectedFabricare.value

                    numPieceAlertBuilder.setView(numPieceBinding.root)
                    numPieceAlert = numPieceAlertBuilder.create()

                    numPieceAlert?.show()
                }

            }
        })

        binding.btnItemCommentMakeInvoiceACT.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {

                // if user choose one BaseItem and remove , viewModel.selectedFabricare is not null, and
                // process will go into if loop and erro will be thrown, so
                // i have to check viewModel.selectedFabricare.value!!.isThereSelectedOne() for making sure if there is selectedFabricare.
                if (viewModel.selectedFabricare.value != null && viewModel.selectedFabricare.value!!.isThereSelectedOne()){
                    val commentAlertBuilder = AlertDialog.Builder(this@MakeInvoiceActivity)
                    commentAlertBuilder.setMessage(R.string.please_comment)
                    commentAlertBuilder.setCancelable(false)

                    val commentBinding = DataBindingUtil.inflate<CommentDialogMakeInvoiceBinding>(layoutInflater,R.layout.comment_dialog_make_invoice,null,false)
                    commentBinding.viewModel = viewModel
                    commentBinding.selectedFabricare = viewModel.selectedFabricare.value
                    commentBinding.etCommentDialogMakeInvoice.setText(viewModel.selectedFabricare.value!!.fabricare!!.comment)

                    commentAlertBuilder.setView(commentBinding.root)
                    commentAlert = commentAlertBuilder.create()

                    commentAlert?.show()

                }

            }
        })
    }


    // showing tag entering dialog
    fun showTagDialog(){

        val tagAlertDialogBuilder = AlertDialog.Builder(this@MakeInvoiceActivity)
        tagAlertDialogBuilder.setMessage(R.string.please_rack_location)
        tagAlertDialogBuilder.setCancelable(false)

        val tagDialogBinding = DataBindingUtil.inflate<TagDialogMakeInvoiceBinding>(layoutInflater,R.layout.tag_dialog_make_invoice,null,false)
        tagDialogBinding.viewModel = viewModel

        val tagColors = resources.getStringArray(R.array.tag_colors)

//         spinner item space ref) https://stackoverflow.com/a/54526961/3151712
        tagDialogBinding.spnTagColorsTagDialogMakeInvoice.adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,tagColors)

        tagAlertDialogBuilder.setView(tagDialogBinding.root)
        tagAlert = tagAlertDialogBuilder.create()

        tagAlert?.show()

    }



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// option menu

    override fun onCreateOptionsMenu(menu: Menu?): Boolean{
        Log.d(TAG,"===== onCreateOptionsMenu  =====")

//        ref https://www.journaldev.com/9357/android-actionbar-example-tutorial

        if(isManagerSignedIn(sharedPrefs)){
            // manager signed in ,so make action bar for manager
            menuInflater.inflate(R.menu.manager_option_small_menu,menu)
        }else{
            // normal action bar for team

            menuInflater.inflate(R.menu.team_option_men,menu)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){

            R.id.mnManagerHome, R.id.mnTeamHome -> moveToDropPickupAct()
            R.id.mnManagerSearch, R.id.mnTeamSearch -> moveToSearchAct()
            R.id.mnManagerAddTeam -> addTeam()
            R.id.mnManagerRack, R.id.mnTeamRack -> moveToRackAct()
            R.id.mnManagerItemsPrices -> moveToItemsPricesAct()
            R.id.mnManagerInventory, R.id.mnTeamInventory -> moveToInventoryAct()
            R.id.mnManagerConveyor, R.id.mnTeamConveyor -> moveToConveyorAct()
            R.id.mnManagerSales -> moveToSalesAct()
            R.id.mnManagerRelease, R.id.mnTeamRelease-> moveToReleaseAct()
            R.id.mnManagerOptionSetting -> moveToInitSettingFrg()
            R.id.mnManagerSignout, R.id.mnTeamSignout  -> signout()

        }

        return super.onOptionsItemSelected(item)
    }

    fun moveToDropPickupAct(){
        Log.d(TAG, "***** moveToDropPickupAct *****")

        val intent = Intent(this,DropPickupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToSearchAct(){
        Log.d(TAG, "***** moveToSearchAct *****")

        val intent = Intent(this, SearchAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun addTeam(){
        val navCont = findNavController(R.id.myNavHostFragment)
        navCont.setGraph(R.navigation.team_nav)
    }

    fun moveToRackAct(){
        Log.d(TAG, "***** moveToRackAct *****")

        val intent = Intent(this,RackAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToItemsPricesAct(){
        Log.d(TAG, "***** moveToItemsPricesAct *****")

        val intent = Intent(this,ItemsPricesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToInventoryAct(){
        Log.d(TAG, "***** moveToInventoryAct *****")

        val intent = Intent(this, InventoryAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToConveyorAct(){
        Log.d(TAG, "***** moveToConveyorAct *****")

        val intent = Intent(this, ConveyorAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToSalesAct(){
        Log.d(TAG, "***** moveToSalesAct *****")

        val intent = Intent(this, SalesAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToReleaseAct(){
        Log.d(TAG, "***** moveToReleaseAct *****")

        val intent = Intent(this, ReleaseAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToInitSettingFrg(){
        val navCont = findNavController(R.id.myNavHostFragment)
        navCont.setGraph(R.navigation.manager_firebase_login_nav)
    }

    fun signout(){
        // clean logged team id from shared preferences
        sharedPrefs.edit().putString("logged_team_id",null).apply()

        // send user team login fragment through ManagerLoginAct
        val intent = Intent(this, ManagerLoginAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun checkUserLoggedIn(){
        var auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null){
            // user status is logged out, user need to logged in , send user ReSignin

            val intent = Intent(this, ReSignInAct::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    fun initEncrypSharedPrefs(){
        // shared preference encryption ref) https://garageprojects.tech/encryptedsharedpreferences-example/
        // ref) https://medium.com/@Naibeck/android-security-encryptedsharedpreferences-ea239e717e5f
        // ref) https://proandroiddev.com/encrypted-preferences-in-android-af57a89af7c8
        //(1) create or retrieve masterkey from Android keystore
        //masterkey is used to encrypt data encryption keys
        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        //(2) Get instance of EncryptedSharedPreferences class
        // as part of the params we pass the storage name, reference to
        // masterKey, context and the encryption schemes used to
        // encrypt SharedPreferences keys and values respectively.
        sharedPrefs = EncryptedSharedPreferences.create(
            "default_shared",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun setActionBarTitle(subTitle: String?) {
        val title = StringBuilder().append(getString(R.string.app_name)).append(" - ").append(subTitle).toString()
        Log.d(TAG,"***** title is ${title} *****")
        supportActionBar?.setTitle(title)
    }


    fun onClickDoneBtn(){
        Log.d(TAG,"----- onClickDoneBtn -----")

        if(viewModel.fabricares.value !=null && viewModel.fabricares.value!!.count() != 0){
            // get today date
            val cal: Calendar = Calendar.getInstance()
            val year: Int = cal.get(Calendar.YEAR)
            val month: Int = cal.get(Calendar.MONTH)
            val day: Int = cal.get(Calendar.DAY_OF_MONTH)
//            val hour: Int = cal.get(Calendar.HOUR)
//            val minutes: Int = cal.get(Calendar.HOUR)
//            val ampm: Int = cal.get(Calendar.AM_PM)

            // date picker for setting up due date
            // date picker ref) https://stackoverflow.com/a/61865742/3151712
            // select date event will be handled by viewModel
            val dialog = DatePickerDialog(this,viewModel, year, month, day)

            // disable past dates
            // disable date picker past ref) https://stackoverflow.com/a/61865742/3151712
            dialog.datePicker.minDate = cal.timeInMillis
            dialog.show()
        }
    }


    // setting UserMakeInvoiceFrg. this should be called after DataBindingUtil.setContentView
    fun setFragment(){

        // programmatically navigation pass parameter ref) https://stackoverflow.com/a/56448153/3151712
        // ref) https://stackoverflow.com/a/54613997/3151712
        // getting basic concept , but don't follow this ref) https://stackoverflow.com/a/53922244/3151712
        val navCont = findNavController(R.id.toUserMakeInvoiceFrg)
        val bundle = Bundle()
        bundle.putSerializable("customer",args.customer)
        navCont.setGraph(R.navigation.user_make_invoice_nav,bundle)
    }



    // implementing IOCallBack
    override fun OnClose() {
        Log.d(TAG,"***** printer IO closed *****")
    }

    override fun OnOpenFailed() {
        Log.d(TAG,"***** printer IO open FAILED *****")
    }

    override fun OnOpen() {
        Log.d(TAG,"***** printer IO opened *****")
    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// for manualDetailAlert

    // implementing FabricareDetailDialogCallback
    override fun onItemCancelCallback() {
        Log.d(TAG, "***** onItemCancelCallback *****")

        fabricareDetailApt!!.submitList(tempFabricareDetails)
        fabricareDetailApt!!.notifyDataSetChanged()
    }

    override fun onItemOkCallback(position: Int, newPrice: Double) {
        Log.d(TAG, "***** onItemOkCallback *****")
        Log.d(TAG, "***** position | ${position.toString()} *****")
        Log.d(TAG, "***** newPrice | ${newPrice.toString()} *****")

        tempFabricareDetails!![position].price = newPrice

        var index = 1
        for(item in tempFabricareDetails!!){
            Log.d(TAG, "***** index | ${index.toString()} *****")
            Log.d(TAG, "***** item.name | ${item.name} *****")
            Log.d(TAG, "***** item.price | ${item.price.toString()} *****")
            index += 1
        }

        fabricareDetailApt!!.submitList(tempFabricareDetails)
        fabricareDetailApt!!.notifyDataSetChanged()
    }

    fun onCancelBtnClickManualDetail(){
        Log.d(TAG,"***** onCancelBtnClickManualDetail *****")

        tempFabricareDetails = viewModel.selectedFabricare.value!!.fabricare?.fabricareDetails?.toMutableList()

        fabricareDetailApt?.submitList(tempFabricareDetails)
        fabricareDetailApt?.notifyDataSetChanged()

        manualDetailAlert?.dismiss()
    }

    fun onSaveBtnClickManualDetail(selectedFabricare: SeletedFabricare){
        Log.d(TAG,"***** onSaveBtnClickManualDetail *****")

        viewModel.updateFabricareWithFabricareDetails(selectedFabricare, tempFabricareDetails!!)

        manualDetailAlert?.dismiss()
    }
}