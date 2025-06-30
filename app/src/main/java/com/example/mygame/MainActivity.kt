package com.example.mygame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnCreateRoom = findViewById<Button>(R.id.btnCreateRoom)
        val btnJoinRoom = findViewById<Button>(R.id.btnJoinRoom)

        btnCreateRoom.setOnClickListener {
            showCreateRoomDialog()
        }

        btnJoinRoom.setOnClickListener {
            startActivity(Intent(this, JoinRoomActivity::class.java))
        }
    }

    private fun showCreateRoomDialog() {
        val roomId = UUID.randomUUID().toString().substring(0, 6)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("ID da sala: $roomId")

        val input = EditText(this)
        input.hint = "Seu nome"
        builder.setView(input)

        builder.setPositiveButton("Confirmar") { _, _ ->
            val playerName = input.text.toString().trim()
            if (playerName.isEmpty()) {
                Toast.makeText(this, "Digite seu nome!", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            createRoom(roomId, playerName)
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun createRoom(roomId: String, playerName: String) {
        val database = FirebaseDatabase.getInstance().getReference("salas")

        val roomData = hashMapOf(
            "roomId" to roomId,
            "creator" to playerName,
            "players" to hashMapOf(playerName to playerName),
            "gameStarted" to false,
            "standings" to hashMapOf<String, Int>(), // Rankings por jogador
            "challenges" to hashMapOf<String, Any>()  // Desafios por jogador
        )

        database.child(roomId).setValue(roomData).addOnSuccessListener {
            Toast.makeText(this, "Sala criada com sucesso!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, LobbyActivity::class.java)
            intent.putExtra("ROOM_ID", roomId)
            intent.putExtra("PLAYER_NAME", playerName)
            startActivity(intent)
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao criar sala!", Toast.LENGTH_SHORT).show()
        }
    }

}
