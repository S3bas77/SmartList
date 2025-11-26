package com.example.smartlist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val btn = findViewById<Button>(R.id.btnGoProfile)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        // Mostrar email del usuario actual
        val currentUser = auth.currentUser
        tvWelcome.text = "Bienvenido: ${currentUser?.email ?: "Usuario"}"

        btn.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        // Usa la instancia de auth que ya inicializaste
        val currentUser = auth.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}