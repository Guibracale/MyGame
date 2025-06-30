package com.example.mygame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

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

    private fun completeChallenge() {
        database.child("standings").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val standings = currentData.getValue(object : GenericTypeIndicator<HashMap<String, Int>>() {})
                    ?: hashMapOf()

                // Se o jogador já tem posição válida (>0), apenas redireciona
                if (standings[playerName]?.let { it > 0 } == true) {
                    return Transaction.success(currentData)
                }

                // Calcula a próxima posição baseada nos jogadores que já completaram
                val nextPosition = standings.values.count { it > 0 } + 1
                standings[playerName] = nextPosition
                currentData.value = standings

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    Toast.makeText(this@GameActivity, "Erro ao completar desafio", Toast.LENGTH_SHORT).show()
                } else if (committed) {
                    val intent = Intent(this@GameActivity, ChallengeCompletedActivity::class.java)
                    intent.putExtra("ROOM_ID", roomId)
                    intent.putExtra("PLAYER_NAME", playerName)
                    startActivity(intent)
                    finish()
                }
            }
        })
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

    private fun assignTargetAndChallenge() {
        database.child("players").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playersList = ArrayList<String>()
                for (playerSnapshot in snapshot.children) {
                    playerSnapshot.getValue(String::class.java)?.let { playersList.add(it) }
                }

                if (playersList.size < 2) {
                    Toast.makeText(
                        this@GameActivity,
                        "Mínimo de 2 jogadores para começar!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                Collections.shuffle(playersList)
                val challengesMap = hashMapOf<String, Any>()
                val standingsMap = hashMapOf<String, Int>()

                for (i in playersList.indices) {
                    val player = playersList[i]
                    val target = playersList[(i + 1) % playersList.size]
                    val challenge = challenges.random()

                    challengesMap[player] = hashMapOf(
                        "target" to target,
                        "challenge" to challenge
                    )
                    // Inicializa sem posição (não aparece no ranking até completar)
                    standingsMap[player] = 0
                }

                val updates = hashMapOf<String, Any>(
                    "challenges" to challengesMap,
                    "standings" to standingsMap
                )

                database.updateChildren(updates).addOnSuccessListener {
                    val myChallenge = challengesMap[playerName] as? Map<*, *>
                    tvTargetPlayer.text = "Alvo: ${myChallenge?.get("target")}"
                    tvChallenge.text = "Desafio: ${myChallenge?.get("challenge")}"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GameActivity, "Erro ao carregar jogadores!", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}