package com.centuryprogrammer18thwasentsingleland.testjacob

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.PrintingJob
import com.centuryprogrammer18thwasentsingleland.databinding.ActivityTestBinding
import com.centuryprogrammer18thwasentsingleland.manager.Settings.SettingsActivity
import com.centuryprogrammer18thwasentsingleland.utils.GenstarPrint
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TestActivity : AppCompatActivity() , IOCallBack {
    private val TAG = TestActivity::class.java.simpleName

    // data binding
    private lateinit var binding : ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_test)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test)

        binding.btn1.setOnClickListener {

            val mPos = Pos()
            val mUsb = USBPrinting()

            mPos.Set(mUsb)
            mUsb.SetCallBack(this)

            val mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager

            val deviceList = mUsbManager.getDeviceList()


//            Log.d(TAG,"***** device : ${deviceList["/dev/bus/usb/002/004"].toString()} *****")
            Log.d(TAG,"***** device : ${deviceList["/dev/bus/usb/002/005"].toString()} *****")


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
                Toast.makeText(applicationContext, "没有权限", Toast.LENGTH_LONG).show()
            } else {

                Log.d(TAG,"***** has permission printing *****")
                GlobalScope.launch {
                    mUsb.Open(mUsbManager,deviceA,this@TestActivity)
                }

                GlobalScope.launch {
                    val printingJobs = mutableListOf<PrintingJob>()
                    GenstarPrint.printTicket(mPos,576,true,false,true,1, printingJobs )
                    mUsb.Close()
                }
            }

        }


    }

    override fun OnClose() {
        Log.d(TAG,"***** printer IO closed *****")
    }

    override fun OnOpenFailed() {
        Log.d(TAG,"***** printer IO open FAILED *****")
    }

    override fun OnOpen() {
        Log.d(TAG,"***** printer IO opened *****")
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean{

//        ref https://www.journaldev.com/9357/android-actionbar-example-tutorial
        menuInflater.inflate(R.menu.manager_option_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.mnManagerOptionSetting -> moveToManagerSetting()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun moveToManagerSetting(){
        val intent = Intent(this,SettingsActivity::class.java)
        startActivity(intent)
    }





}



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// memo


//swRateAmount.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
//    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
//        if(p1){
//            // rate switch is on
//
//            etRate.isEnabled = true
//            etAmount.isEnabled = false
//        }else{
//            etRate.isEnabled =false
//            etAmount.isEnabled = true
//        }
//    }
//})
//
//
//spProcess.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
//    override fun onNothingSelected(p0: AdapterView<*>?) {
//
//    }
//
//    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//
//        Log.d(TAG,"===== selected item id it in spinner is '${id}'")
//        when(position){
//            1, 2 -> {
//                swRateAmount.isEnabled = true
//                if (swRateAmount.isChecked){
//                    etRate.isEnabled = true
//                    etAmount.isEnabled = false
//                }else{
//                    etRate.isEnabled = false
//                    etAmount.isEnabled = true
//                }
//            }
//            else -> {
//                swRateAmount.isEnabled = false
//                etRate.isEnabled = false
//                etAmount.isEnabled = false
//            }
//        }
//    }
//}
//
//etPrice.addTextChangedListener(CurrencyTextWatcher(etPrice))
