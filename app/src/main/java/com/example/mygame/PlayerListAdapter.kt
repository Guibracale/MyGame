package com.example.mygame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class PlayerListAdapter(context: Context, players: List<String>) :
    ArrayAdapter<String>(context, R.layout.item_player, players) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_player, parent, false)
        val playerName = getItem(position)
        val textView = view.findViewById<TextView>(R.id.playerNameTextView)
        textView.text = playerName
        return view
    }
}
