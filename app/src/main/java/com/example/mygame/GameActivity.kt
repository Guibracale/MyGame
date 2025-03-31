package com.example.mygame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class GameActivity : AppCompatActivity() {
    private lateinit var roomId: String
    private lateinit var database: DatabaseReference
    private lateinit var playerName: String
    private lateinit var tvTargetPlayer: TextView
    private lateinit var tvChallenge: TextView
    private lateinit var btnCompleteChallenge: Button
    private val challenges = listOf(
        "Faça esta pessoa rir",
        "Faça esta pessoa falar 'girassol'",
        "Faça esta pessoa contar uma história engraçada",
        "Descubra um hobby desta pessoa",
        "Faça esta pessoa cantar um trecho de uma música",
        "Pergunte algo curioso sobre esta pessoa",
        "Tente fazer esta pessoa desenhar algo",
        "Convide esta pessoa para um jogo de adivinhação"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvTargetPlayer = findViewById(R.id.tvTargetPlayer)
        tvChallenge = findViewById(R.id.tvChallenge)
        btnCompleteChallenge = findViewById(R.id.btnCompleteChallenge)

        roomId = intent.getStringExtra("ROOM_ID") ?: ""
        playerName = intent.getStringExtra("PLAYER_NAME") ?: ""

        database = FirebaseDatabase.getInstance().getReference("salas").child(roomId)

        listenForGameStart()

        btnCompleteChallenge.setOnClickListener {
            completeChallenge()
        }
    }

    private fun listenForGameStart() {
        database.child("gameStarted").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.value == true) {
                    assignTargetAndChallenge()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    data class PlayerChallenge(
        val target: String = "",
        val challenge: String = ""
    )


    private fun assignTargetAndChallenge() {
        database.child("players").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()

                if (players.size < 2) {
                    Toast.makeText(this@GameActivity, "Mínimo de 2 jogadores para começar!", Toast.LENGTH_SHORT).show()
                    return
                }

                players.shuffle()

                val assignments = mutableMapOf<String, PlayerChallenge>()

                for (i in players.indices) {
                    val targetIndex = (i + 1) % players.size
                    val target = players[targetIndex]
                    val challenge = challenges.random()
                    assignments[players[i]] = PlayerChallenge(target, challenge)
                }

                database.child("game").setValue(assignments).addOnCompleteListener {
                    if (it.isSuccessful) {
                        showPlayerChallenge()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun showPlayerChallenge() {
        database.child("game").child(playerName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(object : GenericTypeIndicator<Pair<String, String>>() {})?.let { (target, challenge) ->
                    tvTargetPlayer.text = "Alvo: $target"
                    tvChallenge.text = "Desafio: $challenge"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun completeChallenge() {
        // Aqui você pode adicionar lógica para validar ou armazenar que o desafio foi concluído
        Toast.makeText(this, "Desafio concluído!", Toast.LENGTH_SHORT).show()

        // Redirecionar para outra tela (ainda não criada)
        val intent = Intent(this, ChallengeCompletedActivity::class.java)
        startActivity(intent)
    }
}
