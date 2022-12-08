package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Fabricare

class FabricareAddDialogFrgVM : ViewModel() {
    private val TAG = FabricareAddDialogFrgVM::class.java.simpleName

    // these are two way data binding with fabricare_add_dialog_frg.xml
    var fabricareName = ""
    var fabricareNumbPieceStr = ""
    var fabricareBasePriceStr = ""

//    data binding with radio group
//    radio button group data binding ref) https://stackoverflow.com/a/54262153/3151712
    var baseGroupCheckedId = 0
    var partialBaseGroupCheckedId = 0

    private lateinit var sharedPrefs : SharedPreferences

    private lateinit var actViewModel : MakeInvoiceActVM

    private val _messageOnFragment = MutableLiveData<String>()
    val messageOnFragment : LiveData<String>
        get() = _messageOnFragment

    private val _closeDialog = MutableLiveData<Boolean>()
    val closeDialog : LiveData<Boolean>
        get() = _closeDialog

    fun setSharedPreferences(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun setActViewMode(receivedActViewModel:MakeInvoiceActVM){
        actViewModel = receivedActViewModel

    }

    fun clearVars(){
        fabricareName = ""
        fabricareNumbPieceStr = ""
        fabricareBasePriceStr = ""
    }

    fun onClickCancelBtn(){
        clearVars()
        _closeDialog.value = true
    }

    fun onClickAddBtn(){
        if(emptyValidation()){
            // succeeded

            val fabricareNumPiece = fabricareNumbPieceStr.toInt()
            val fabricarePrice = fabricareBasePriceStr.replace("[$,]".toRegex(),"").toDouble()

            val fabricareProcessStr = makeFabricareProcessStr()
            val fabricareProcess = fabricareProcessStr

            val fabricareBasePrice = fabricarePrice
            val fabricareSubTotalPrice = fabricareBasePrice

            val fabricareDryPrice =
                if(fabricareProcessStr == fabricareNumbPieceStr+"_dry_clean_press" || fabricareProcessStr == fabricareNumbPieceStr+"_dry_clean"){
                    fabricarePrice
                }else{
                    0.0
                }

            val fabricareWetPrice =
                if(fabricareProcessStr == fabricareNumbPieceStr+"_wet_clean_press" || fabricareProcessStr == fabricareNumbPieceStr+"_wet_clean"){
                    fabricarePrice
                }else{
                    0.0
                }

            val fabricareAlterationPrice =
                if(fabricareProcessStr == fabricareNumbPieceStr+"_alter"){
                    fabricarePrice
                }else{
                    0.0
                }

            val fabricare = Fabricare(1,fabricareName,fabricareNumPiece,fabricareProcess,fabricareBasePrice,fabricareSubTotalPrice,fabricareDryPrice,fabricareWetPrice,fabricareAlterationPrice)

            actViewModel.addFabricare(fabricare)

            _closeDialog.value = true

        }else{
            _messageOnFragment.value = App.resourses!!.getString(R.string.these_are_not_allowed_empty)
        }
    }

    fun emptyValidation():Boolean{
        // if any variable has empty, something wrong

        return !(fabricareName == "" || fabricareNumbPieceStr =="" || fabricareBasePriceStr =="")
    }

    fun makeFabricareProcessStr():String{
        var fabricareProcessStr = fabricareName

        when(baseGroupCheckedId){
            R.id.rbtnDryFabricareAddDialogFrg->{

                when(partialBaseGroupCheckedId){
                    R.id.rbtnCOFabricareAddDialogFrg->{
                        fabricareProcessStr += "_dry_clean"
                    }
                    R.id.rbtnPOFabricareAddDialogFrg->{
                        fabricareProcessStr +="_dry_press"
                    }
                    else ->{
                        fabricareProcessStr +="_dry_clean_press"
                    }
                }

            }
            R.id.rbtnWetFabricareAddDialogFrg->{
                when(partialBaseGroupCheckedId){
                    R.id.rbtnCOFabricareAddDialogFrg->{
                        fabricareProcessStr += "_wet_clean"
                    }
                    R.id.rbtnPOFabricareAddDialogFrg->{
                        fabricareProcessStr +="_wet_press"
                    }
                    else ->{
                        fabricareProcessStr +="_wet_clean_press"
                    }
                }
            }
            R.id.rbtnAlterFabricareAddDialogFrg->{
                fabricareProcessStr +="_alter"
            }
        }
        return fabricareProcessStr
    }


}