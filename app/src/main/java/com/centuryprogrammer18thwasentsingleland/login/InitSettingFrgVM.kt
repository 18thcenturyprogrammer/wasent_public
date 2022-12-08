package com.centuryprogrammer18thwasentsingleland.login

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.api.WasentServerInterface
import com.centuryprogrammer18thwasentsingleland.data.CleanerInitSetting
import com.centuryprogrammer18thwasentsingleland.repository.AddCleanerInitSettingCallbacks
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.getCurrentTimeString
import com.centuryprogrammer18thwasentsingleland.utils.getCurrentTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.isValidKeyCharaters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


class InitSettingFrgVM : ViewModel() , AddCleanerInitSettingCallbacks {
    private val TAG = InitSettingFrgVM::class.java.simpleName

    var mAuth = FirebaseAuth.getInstance()
    var user : FirebaseUser? = null
    var email : String? = null

    //one way data binding
    var cleanerName : String = ""
    var cleanerPhoneNum : String = ""
    var cleanerStreet : String = ""
    var cleanerCity : String = ""
    var cleanerState : String = ""
    var cleanerZip : String = ""
    var managerId : String =""
    var managerPassword :String = ""
    var tax : String = "0.0"
    var envRate : String = "0.0"
    var envAmount : String = "0.0"


    private lateinit var sharedPrefs: SharedPreferences

    private val _enableSaveBtn= MutableLiveData<Boolean>()
    val enableSaveBtn : LiveData<Boolean>
        get() = _enableSaveBtn

    private val _msgToFrg= MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    private val _setFocusOn = MutableLiveData<EditText>()
    val setFocusOn : LiveData<EditText>
        get() = _setFocusOn

    private val _moveToManagerLoginAct = MutableLiveData<Boolean>()
    val moveToManagerLoginAct : LiveData<Boolean>
        get() = _moveToManagerLoginAct


    fun getCurrentUser(){
        user = mAuth.currentUser

        user?.let{

            // firebase is not allowed to use email as key
            // so i change dot to comma
            email = it.email!!.replace(".",",")
        }
    }

    fun setSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun loadSettings(sharedPrefs: SharedPreferences){

        with(sharedPrefs){
            cleanerName = getString("cleaner_name","")!!
            cleanerPhoneNum = getString("cleaner_phone_num","")!!
            cleanerStreet = getString("cleaner_street_address","")!!
            cleanerCity = getString("cleaner_city","")!!
            cleanerState = getString("cleaner_state","")!!
            cleanerZip = getString("cleaner_zipcode","")!!
            managerId = getString("manager_id","")!!
            managerPassword = getString("manager_password","")!!
            tax = getString("tax","")!!
            envRate = getString("env_rate","")!!
            envAmount = getString("env_amount","")!!
        }
    }

    fun onCleanerNameFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onCleanerNameFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        editText.setText(editText.text.trim().toString())

        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){

            // required edittext non-empty ref) https://stackoverflow.com/a/53994791/3151712
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }else{

                // firebase not allow some special characters for key
                // . $ [ ] # / not allowed
                // check if there is not allowed characters
                if(!isValidKeyCharaters(editText.text.toString())){
                    editText.setError(App.resourses!!.getString(R.string.not_allowed_special_chars))
                }
            }
        }
    }

    fun onCleanerPhoneFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onCleanerPhoneFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        editText.setText(editText.text.trim().toString())

        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }else{
                if(editText.text.length != 14){
                    // phone number length is not correct

                    editText.setError(App.resourses!!.getString(R.string.not_correct_phonenum))
                }
            }
        }
    }

    fun onCleanerStreetFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onCleanerStreetFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        editText.setText(editText.text.trim().toString())

        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }
        }
    }

    fun onCleanerCityFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onCleanerCityFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        editText.setText(editText.text.trim().toString())

        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }
        }
    }

    fun onCleanerStateFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onCleanerStateFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        editText.setText(editText.text.trim().toString())

        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }else{
                if(editText.text.length != 2){
                    // phone number length is not correct

                    editText.setError(App.resourses!!.getString(R.string.state_2_chars))
                }
            }
        }
    }

    fun onCleanerZipFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onCleanerZipFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        editText.setText(editText.text.trim().toString())

        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }else{
                if(editText.text.length != 5){
                    // phone number length is not correct

                    editText.setError(App.resourses!!.getString(R.string.zip_5_chars))
                }
            }
        }
    }

    fun onManagerIdFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onManagerIdFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        editText.setText(editText.text.trim().toString())


        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }else{
                if(editText.text.length < 4){
                    // phone number length is not correct

                    editText.setError(App.resourses!!.getString(R.string.manager_id_least_4_chars))
                }
            }
        }
    }

    fun onManagerPassFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onManagerPassFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        editText.setText(editText.text.trim().toString())

        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }else{
                if(editText.text.length < 4){
                    // phone number length is not correct

                    editText.setError(App.resourses!!.getString(R.string.manager_pass_least_4_chars))
                }
            }
        }
    }


    fun onTaxFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onTaxFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        editText.setText(editText.text.trim().toString())

        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }
        }
    }

    fun onEnvRateFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onEnvRateFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        editText.setText(editText.text.trim().toString())

        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }
        }
    }

    fun onEnvAmountFocusChange(view: View,hasFocus:Boolean ){
        Log.d(TAG, "----- onEnvAmountFocusChange -----")
        Log.d(TAG, "----- view :'${view.id}' ----- hasFocus : '${hasFocus.toString()}' -----")

        var editText = view as EditText

        // losing focus means user entered something and leaving
        // so do some validation before leaving and show messages
        if (!hasFocus){
            if(editText.text.isEmpty()){
                // empty and user try to leave

                editText.setError(App.resourses!!.getString(R.string.empty_not_allowed))
            }
        }
    }

    fun onClickSaveBtn(){
        _enableSaveBtn.value = false

        if(validationFields()){
            // validation is succeeded
            Log.d(TAG,"----- validation is succeeded -----")

            // make new CleanerInitSetting to save into firebase database
            val cleanerInitSetting = CleanerInitSetting(
                cleanerName,
                cleanerPhoneNum,
                cleanerStreet,
                cleanerCity,
                cleanerState,
                cleanerZip,
                managerId,
                managerPassword,
                tax,
                envRate,
                envAmount,
                user!!.uid,
                email,
                getCurrentTimeString(),
                getCurrentTimestamp()
            )

            Repository.saveCleanerInitSetting(cleanerInitSetting,this)
        }else{
            // validation failed , show button , so user can try again

            _enableSaveBtn.value = true
        }
    }

    override fun onAddCleanerInitSettingCall(
        isSuccess: Boolean,
        cleanerInitSetting: CleanerInitSetting
    ) {
        if(isSuccess){
            Log.d(TAG, "----- add cleaner init setting into Firebase is Succeeded -----")

            user?.let {
                val prefs = mutableMapOf<String,String>()

                prefs.put("cleaner_name", cleanerName.trim())
                prefs.put("owner_email", it.email!!)
                prefs.put("cleaner_phone_num", cleanerPhoneNum.trim())
                prefs.put("cleaner_street_address", cleanerStreet.trim())
                prefs.put("cleaner_city", cleanerCity.trim())
                prefs.put("cleaner_state", cleanerState.trim())
                prefs.put("cleaner_zipcode", cleanerZip.trim())
                prefs.put("manager_id", managerId.trim())
                prefs.put("manager_password", managerPassword.trim())
                prefs.put("tax", tax.trim())
                prefs.put("env_rate", envRate.trim())
                prefs.put("env_amount", envAmount.trim())

                saveSettings(prefs)
            }
        }else{

            Log.d(TAG, "----- add cleaner init setting into Firebase is Failed -----")

            // something wrong, so show button to user try again
            _enableSaveBtn.value = true
            _msgToFrg.value = App.resourses!!.getString(R.string.problem_save_into_database)
        }
    }

    fun saveSettings(prefs: MutableMap<String,String>){

        // save shared preferences
        sharedPrefs.edit().apply {
            putString("cleaner_name", prefs["cleaner_name"] as String)
            putString("owner_email", prefs["owner_email"] as String)
            putString("cleaner_phone_num", prefs["cleaner_phone_num"] as String)
            putString("cleaner_street_address", prefs["cleaner_street_address"] as String)
            putString("cleaner_city", prefs["cleaner_city"] as String)
            putString("cleaner_state", prefs["cleaner_state"] as String)
            putString("cleaner_zipcode", prefs["cleaner_zipcode"] as String)
            putString("manager_id", prefs["manager_id"] as String)
            putString("manager_password", prefs["manager_password"] as String)
            putString("tax", prefs["tax"] as String)
            putString("env_rate", prefs["env_rate"] as String)
            putString("env_amount", prefs["env_amount"] as String)
            putString("team_id_"+prefs["manager_id"] as String, "team_pass_"+prefs["manager_password"] as String)
            putString("logged_team_id", prefs["manager_id"] as String)
        }.apply()

        _moveToManagerLoginAct.value = true
    }

    fun validationFields():Boolean{
        if(!cleanerName.trim().isEmpty()
            && !cleanerPhoneNum.trim().isEmpty()
            && !cleanerStreet.trim().isEmpty()
            && !cleanerCity.trim().isEmpty()
            && !cleanerState.trim().isEmpty()
            && !cleanerZip.trim().isEmpty()
            && !managerId.trim().isEmpty()
            && !managerPassword.trim().isEmpty()
            && !tax.trim().isEmpty()
            && !envRate.trim().isEmpty()
            && !envAmount.trim().isEmpty()
        ){
            // firebase not allow some special characters for key
            // . $ [ ] # / not allowed
            // check if there is not allowed characters
            if(isValidKeyCharaters(cleanerName)){
                if(cleanerPhoneNum.length == 14) {
                    if (cleanerState.length == 2) {
                        if (cleanerZip.length == 5) {
                            if(managerId.length > 3){
                                if(managerPassword.length > 3){
                                    if(!duplicateId(managerId)){
                                        if(!duplicatePass(managerPassword)){
                                            return true
                                        }else{
                                            _msgToFrg.value = App.resourses!!.getString(R.string.duplicated_pass)
                                            return false
                                        }
                                    }else{
                                        _msgToFrg.value = App.resourses!!.getString(R.string.duplicated_id)
                                        return false
                                    }
                                }
                                else{
                                    _msgToFrg.value = App.resourses!!.getString(R.string.manager_pass_least_4_chars)
                                    return false
                                }
                            }
                            else{
                                _msgToFrg.value = App.resourses!!.getString(R.string.manager_id_least_4_chars)
                                return false
                            }
                        } else {
                            _msgToFrg.value = App.resourses!!.getString(R.string.zip_5_chars)
                            return false
                        }
                    } else {
                        _msgToFrg.value = App.resourses!!.getString(R.string.state_2_chars)
                        return false
                    }
                }else{
                    _msgToFrg.value = App.resourses!!.getString(R.string.not_correct_phonenum)
                    return false
                }
            }else{
                _msgToFrg.value = App.resourses!!.getString(R.string.not_allowed_special_chars)
                return false
            }
        }else{
            _msgToFrg.value = App.resourses!!.getString(R.string.empty_not_allowed)
            return false
        }
    }

    fun duplicateId(id:String):Boolean{
        return sharedPrefs.all.keys.contains("team_id_"+id) && sharedPrefs.getString("manager_id","") != id
    }

    fun duplicatePass(pass:String):Boolean{
        return sharedPrefs.all.values.contains("team_pass_"+pass) && sharedPrefs.getString("manager_password","") != pass
    }
}