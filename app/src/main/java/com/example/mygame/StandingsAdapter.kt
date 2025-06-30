package com.example.mygame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StandingsAdapter(private val standingsList: List<Pair<String, Int>>) :
    RecyclerView.Adapter<StandingsAdapter.StandingsViewHolder>() {

    class StandingsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPosition: TextView = view.findViewById(R.id.tvPosition)
        val tvPlayerName: TextView = view.findViewById(R.id.tvPlayerName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StandingsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_standing, parent, false)
        return StandingsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StandingsViewHolder, position: Int) {
        val (playerName, rankingPosition) = standingsList[position]
        holder.tvPosition.text = "${rankingPosition}º"
        holder.tvPlayerName.text = playerName
    }

    override fun getItemCount() = standingsList.size
}
