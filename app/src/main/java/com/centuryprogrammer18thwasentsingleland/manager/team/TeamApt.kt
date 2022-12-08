package com.centuryprogrammer18thwasentsingleland.manager.team

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.centuryprogrammer18thwasentsingleland.data.Team
import com.centuryprogrammer18thwasentsingleland.databinding.DetailItemViewholderBinding
import com.centuryprogrammer18thwasentsingleland.databinding.TeamViewholderBinding
import com.centuryprogrammer18thwasentsingleland.utils.TeamAptCallback

// recycler view adpater
// using DiffUtil and ListAdapter
// ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding/index.html?index=..%2F..android-kotlin-fundamentals#7
// ref) source code https://github.com/google-developer-training/android-kotlin-fundamentals-apps/blob/master/RecyclerViewDiffUtilDataBinding/app/src/main/java/com/example/android/trackmysleepquality/sleeptracker/SleepNightAdapter.kt

class TeamApt(val callback: TeamAptCallback): ListAdapter<Team, TeamViewHolder>(TeamDiffCallback()) {
    private val TAG = TeamApt::class.java.simpleName

    private var longClickedTag: Int? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val viewHolder = TeamViewHolder.from(parent)

        viewHolder.binding.btnHamTeamVH.setOnLongClickListener { view ->
            Log.d(TAG,"***** Long clicked *****")

            longClickedTag = view.tag as Int

            // i will chang UI for long clicked viewholder, so refresh it
            notifyDataSetChanged()
            return@setOnLongClickListener true
        }

        viewHolder.binding.btnSaveTeamVH.setOnClickListener{ view ->

            var teamId =""
            var teamPass =""
            val tag = view.tag as Int

            if(viewHolder.binding.etIdTeamVH.tag == tag){
                teamId = viewHolder.binding.etIdTeamVH.text.toString()
            }
            if(viewHolder.binding.etPassTeamVH.tag == tag){
                teamPass = viewHolder.binding.etPassTeamVH.text.toString()
            }

            if(!teamId.isEmpty()
                && !teamPass.isEmpty()
                && (teamId.length>3)
                && (teamPass.length>3)) {
                // validation team id and password succeeded

                callback.onTeamSaveCallback(view.tag as Int, Team(teamId, teamPass))

                with(viewHolder.binding) {

                    if(etIdTeamVH.tag == tag){
                        etIdTeamVH.isEnabled = false
                    }

                    if(etPassTeamVH.tag == tag){
                        etPassTeamVH.isEnabled = false
                    }

                    if(btnSaveTeamVH.tag == tag){
                        btnSaveTeamVH.visibility = android.view.View.GONE
                    }

                    if(btnDeleteTeamVH.tag == tag){
                        btnDeleteTeamVH.visibility = android.view.View.GONE
                    }

                    if(btnHamTeamVH.tag == tag){
                        btnHamTeamVH.visibility = android.view.View.VISIBLE
                    }
                }
                longClickedTag = null
            }
        }


        viewHolder.binding.btnDeleteTeamVH.setOnClickListener{ view ->
            val tag = view.tag as Int

            val position = view.tag as Int

            callback.onTeamDeleteCallback(position,getItem(position))

            with(viewHolder.binding) {

                if(etIdTeamVH.tag == tag){
                    etIdTeamVH.isEnabled = false
                }

                if(etPassTeamVH.tag == tag){
                    etPassTeamVH.isEnabled = false
                }

                if(btnSaveTeamVH.tag == tag){
                    btnSaveTeamVH.visibility = android.view.View.GONE
                }

                if(btnDeleteTeamVH.tag == tag){
                    btnDeleteTeamVH.visibility = android.view.View.GONE
                }

                if(btnHamTeamVH.tag == tag){
                    btnHamTeamVH.visibility = android.view.View.VISIBLE
                }
            }

            longClickedTag = null
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val item = getItem(position)
        val tag = position

        holder.binding.cvTeamVH.setTag(tag)

        holder.binding.etIdTeamVH.setTag(tag)
        holder.binding.etPassTeamVH.setTag(tag)
        holder.binding.btnSaveTeamVH.setTag(tag)
        holder.binding.btnDeleteTeamVH.setTag(tag)
        holder.binding.btnHamTeamVH.setTag(tag)

        if(longClickedTag == position){
            Log.d(TAG,"***** ul change for long clicked  *****")
            Log.d(TAG,"***** longClickedTag : '${longClickedTag.toString()}' ***** position : '${position.toString()}'  *****")

            with(holder.binding){
                etIdTeamVH.isEnabled = true
                etPassTeamVH.isEnabled = true
                btnSaveTeamVH.visibility = android.view.View.VISIBLE
                btnDeleteTeamVH.visibility = android.view.View.VISIBLE
                btnHamTeamVH.visibility = android.view.View.GONE
            }
        }

        holder.bind(item)
    }


}

class TeamViewHolder(val binding: TeamViewholderBinding) : RecyclerView.ViewHolder(binding.root){
    private val TAG = TeamViewHolder::class.java.simpleName

    fun bind(team: Team){
        binding.etIdTeamVH.setText(team.id)
        binding.etPassTeamVH.setText(team.password)
    }

    companion object{
        fun from(parent: ViewGroup):TeamViewHolder{
            val layoutInflater = LayoutInflater.from(parent.context)

            val binding = TeamViewholderBinding.inflate(layoutInflater)

            return TeamViewHolder(binding)
        }
    }
}

class TeamDiffCallback : DiffUtil.ItemCallback<Team>(){
    override fun areItemsTheSame(oldTeam: Team, newTeam: Team): Boolean {
        return oldTeam.id == newTeam.id
    }

    override fun areContentsTheSame(oldTeam: Team, newTeam: Team): Boolean {
        return oldTeam == newTeam
    }

}