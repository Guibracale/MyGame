package com.example.mygame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class PlayerListAdapter(context: Context, players: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, players) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        val playerName = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = playerName
        return view
    }
}
