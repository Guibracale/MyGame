package com.example.mygame

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class LobbyActivity : AppCompatActivity() {
    private lateinit var roomId: String
    private lateinit var playerName: String
    private lateinit var database: DatabaseReference
    private lateinit var playerListView: ListView
    private lateinit var btnStartGame: Button
    private val players = mutableListOf<String>()
    private var isCreator = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        val tvRoomId = findViewById<TextView>(R.id.tvRoomId)
        playerListView = findViewById(R.id.playerListView)
        btnStartGame = findViewById(R.id.btnStartGame)

        roomId = intent.getStringExtra("ROOM_ID") ?: ""
        playerName = intent.getStringExtra("PLAYER_NAME") ?: ""

        tvRoomId.text = "Sala ID: $roomId"

        database = FirebaseDatabase.getInstance().getReference("salas").child(roomId)

        checkIfCreator()
        observePlayers()
        listenForGameStart()

        btnStartGame.setOnClickListener {
            startGame()
        }
    }

    private fun checkIfCreator() {
        database.child("creator").get().addOnSuccessListener { snapshot ->
            isCreator = snapshot.value == playerName
            btnStartGame.visibility = if (isCreator) View.VISIBLE else View.GONE
        }
    }

    private fun observePlayers() {
        database.child("players").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                players.clear()
                for (playerSnapshot in snapshot.children) {
                    playerSnapshot.getValue(String::class.java)?.let { players.add(it) }
                }
                updatePlayerList()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LobbyActivity, "Erro ao carregar jogadores!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePlayerList() {
        val adapter = PlayerListAdapter(this, players)
        playerListView.adapter = adapter
    }

    private fun startGame() {
        database.child("gameStarted").setValue(true)
    }

    private fun listenForGameStart() {
        database.child("gameStarted").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.value == true) {
                    val intent = Intent(this@LobbyActivity, GameActivity::class.java)
                    intent.putExtra("ROOM_ID", roomId)
                    intent.putExtra("PLAYER_NAME", playerName)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
