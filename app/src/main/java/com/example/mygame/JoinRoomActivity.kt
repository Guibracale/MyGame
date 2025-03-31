package com.example.mygame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class JoinRoomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_room)

        val etRoomId = findViewById<EditText>(R.id.etRoomId)
        val etPlayerName = findViewById<EditText>(R.id.etPlayerName) // Adicionando campo de nome
        val btnConfirmJoin = findViewById<Button>(R.id.btnConfirmJoin)

        btnConfirmJoin.setOnClickListener {
            val roomId = etRoomId.text.toString()
            val playerName = etPlayerName.text.toString()

            if (roomId.isNotEmpty() && playerName.isNotEmpty()) {
                joinRoom(roomId, playerName)
            } else {
                Toast.makeText(this, "Digite um ID e um nome válidos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun joinRoom(roomId: String, playerName: String) {
        val database = FirebaseDatabase.getInstance().getReference("salas")

        database.child(roomId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                database.child(roomId).child("players").child(playerName).setValue(playerName)
                    .addOnSuccessListener {
                        val intent = Intent(this, LobbyActivity::class.java)
                        intent.putExtra("ROOM_ID", roomId)
                        intent.putExtra("PLAYER_NAME", playerName)
                        startActivity(intent)
                    }
            } else {
                Toast.makeText(this, "Sala não encontrada!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
