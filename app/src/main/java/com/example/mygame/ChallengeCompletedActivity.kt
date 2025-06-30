package com.example.mygame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ChallengeCompletedActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var roomId: String
    private lateinit var playerName: String
    private lateinit var tvStanding: TextView
    private lateinit var tvTarget: TextView
    private lateinit var tvChallenge: TextView
    private lateinit var rvStandings: RecyclerView
    private lateinit var btnHome: Button
    private lateinit var standingsAdapter: StandingsAdapter
    private val standingsList = mutableListOf<Pair<String, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_completed)

        tvStanding = findViewById(R.id.tvStanding)
        tvTarget = findViewById(R.id.tvTarget)
        tvChallenge = findViewById(R.id.tvChallenge)
        rvStandings = findViewById(R.id.rvStandings)
        btnHome = findViewById(R.id.btnHome)

        roomId = intent.getStringExtra("ROOM_ID") ?: ""
        playerName = intent.getStringExtra("PLAYER_NAME") ?: ""

        database = FirebaseDatabase.getInstance().getReference("salas").child(roomId)

        standingsAdapter = StandingsAdapter(standingsList)
        rvStandings.layoutManager = LinearLayoutManager(this)
        rvStandings.adapter = standingsAdapter

        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        registerPlayerPosition()
        showPlayerChallenge()
        observeStandings()
    }

    private fun registerPlayerPosition() {
        database.child("standings").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val standings = currentData.getValue(object : GenericTypeIndicator<HashMap<String, Int>>() {})
                    ?: hashMapOf()

                // Se o jogador já tem posição válida (>0), não faz nada
                if (standings[playerName]?.let { it > 0 } == true) {
                    return Transaction.success(currentData)
                }

                // Calcula a próxima posição baseada no número de jogadores com posição >0
                val nextPosition = standings.values.count { it > 0 } + 1
                standings[playerName] = nextPosition
                currentData.value = standings

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    Log.e("Firebase", "Erro ao registrar posição: ${error.message}")
                }
            }
        })
    }

    private fun showPlayerChallenge() {
        database.child("challenges").child(playerName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val challenge = snapshot.child("challenge").getValue(String::class.java) ?: "Desconhecido"
                val target = snapshot.child("target").getValue(String::class.java) ?: "Desconhecido"

                tvChallenge.text = "O seu objetivo era: $challenge"
                tvTarget.text = "O seu alvo era: $target"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro ao carregar desafio: ${error.message}")
            }
        })
    }

    private fun observeStandings() {
        database.child("standings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                standingsList.clear()

                if (!snapshot.exists()) return

                // Processa apenas jogadores que já completaram (posição > 0)
                snapshot.children.forEach { child ->
                    val name = child.key ?: return@forEach
                    val position = child.getValue(Int::class.java) ?: 0
                    if (position > 0) {
                        standingsList.add(Pair(name, position))
                    }
                }

                // Ordena por posição (1º, 2º, etc.)
                standingsList.sortBy { it.second }
                standingsAdapter.notifyDataSetChanged()

                // Atualiza a posição do jogador atual
                standingsList.find { it.first == playerName }?.let {
                    tvStanding.text = "Parabéns, você ficou em ${it.second}º lugar!"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro ao atualizar ranking: ${error.message}")
            }
        })
    }
}