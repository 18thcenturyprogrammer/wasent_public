package com.centuryprogrammer18thwasentsingleland.manager.team

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R

class TeamAddDialogFrgVM : ViewModel() {
    private val TAG = TeamAddDialogFrgVM::class.java.simpleName

    lateinit var sharedPrefs : SharedPreferences

    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    private val _closeAddTeamAlert = MutableLiveData<Boolean>()
    val closeAddTeamAlert : LiveData<Boolean>
        get() = _closeAddTeamAlert

    // two way data binding team_add_dialog_frg.xml
    var teamId = MutableLiveData<String>("")
    var teamPass = MutableLiveData<String>("")

    fun updateSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun onClickCancelBtn(){
        Log.d(TAG,"----- onClickCancelBtn -----")

        // clean variables
        teamId.value = ""
        teamPass.value = ""

        _closeAddTeamAlert.value = true
    }

    fun onClickOkBtn(){
        Log.d(TAG,"----- onClickOkBtn -----")

        if(!teamId.value!!.isEmpty()
            && !teamPass.value!!.isEmpty()
            && (teamId.value!!.length > 3)
            && (teamPass.value!!.length > 3)
            && !duplicateId(teamId.value!!)
            && !duplicatePass(teamPass.value!!)
        ){

            sharedPrefs.edit().putString("team_id_"+teamId.value, "team_pass_"+teamPass.value).apply()

            // clean variables
            teamId.value = ""
            teamPass.value = ""

            _msgToFrg.value = App.resourses!!.getString(R.string.successfully_add_team_member)
            _closeAddTeamAlert.value = true
        }else{
            _msgToFrg.value = App.resourses!!.getString(R.string.all_have_to_be_filled_not_duplicated_more_than_4_chars)
        }
    }

    fun duplicateId(id:String):Boolean{
        return sharedPrefs.all.keys.contains("team_id_"+id)
    }

    fun duplicatePass(pass:String):Boolean{
        return sharedPrefs.all.values.contains("team_pass_"+pass)
    }
}