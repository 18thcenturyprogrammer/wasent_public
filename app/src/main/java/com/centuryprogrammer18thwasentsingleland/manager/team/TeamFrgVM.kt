package com.centuryprogrammer18thwasentsingleland.manager.team

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Team
import com.centuryprogrammer18thwasentsingleland.utils.TeamAptCallback
import com.centuryprogrammer18thwasentsingleland.utils.notifyObserver

class TeamFrgVM : ViewModel(), TeamAptCallback {
    private val TAG = TeamFrgVM::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    private val _teams = MutableLiveData<MutableList<Team>>()
    val teams : LiveData<MutableList<Team>>
        get() = _teams

    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    fun updateSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun loadTeams(){
        val temp = mutableListOf<Team>()

        for(key in sharedPrefs.all.keys){
            if(key.startsWith("team_id_")){
                temp.add(Team(key.replace("team_id_",""), sharedPrefs.getString(key,"").replace("team_pass_","")))
            }
        }
        _teams.value = temp
    }


    override fun onTeamSaveCallback(position: Int, team: Team) {
        Log.d(TAG, "===== onTeamUpdateCallback =====")
        Log.d(TAG, "===== position : '${position.toString()}' ===== team : '${team .toString()}' =====")

        // get old team id and remove from shared preference
        val oldTeamId = teams.value!![position].id
        Log.d(TAG, "===== oldTeamId : '${oldTeamId}' ===== ")

        sharedPrefs.edit().remove("team_id_"+oldTeamId).apply()

        // make new shared preferences
        sharedPrefs.edit().putString("team_id_"+team.id,"team_pass_"+team.password).apply()
        Log.d(TAG, "===== team.id : '${team.id}' ===== team.password : '${team.password}' =====")

        if(isManager(teams.value!![position])){
            // editing team id which is connected to manager id

            sharedPrefs.edit().remove("manager_id").apply()
            sharedPrefs.edit().remove("manager_password").apply()

            // make new shared preferences
            sharedPrefs.edit().putString("manager_id",team.id).apply()
            sharedPrefs.edit().putString("manager_password",team.password).apply()
            Log.d(TAG, "===== updated manager_id : '${team.id}' ===== manager_password : '${team.password}' =====")
        }

        // updata teams livedata variable
        val temp = teams.value
        temp?.let {
            it[position] = team
        }
        _teams.value = temp
    }

    override fun onTeamDeleteCallback(position: Int, team : Team) {
        Log.d(TAG, "===== onTeamUpdateCallback =====")
        Log.d(TAG, "===== position : '${position.toString()}' ===== team : '${team.toString()}' =====")

        // i don't allow to delete manager team id
        if(!isManager(team)){
            // NOT manager id, so I can delete it

            sharedPrefs.edit().remove("team_id_"+team.id).apply()
            _teams.value!!.removeAt(position)
            _msgToFrg.value = App.resourses!!.getString(R.string.successfully_delete_team_number)
            _teams.notifyObserver()
        }else{
            _msgToFrg.value = App.resourses!!.getString(R.string.manager_id_cannot_be_deleted)
        }
    }

    override fun onMsgCallback(msg: String) {
        _msgToFrg.value= msg
    }

    fun isManager(team: Team):Boolean{

        val managerId = sharedPrefs.getString("manager_id",null)
        Log.d(TAG,"===== managerId : '${managerId}'  =====")

        return team.id == managerId
    }
}