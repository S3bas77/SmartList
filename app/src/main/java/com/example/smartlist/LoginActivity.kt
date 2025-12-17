package com.example.smartlist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(this)

        // Verificar si ya está logueado
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, Navbar::class.java))
            finish()
        }

        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val btn = findViewById<Button>(R.id.btnLogin)
        val goToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btn.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Guardar sesión
                        sessionManager.saveAuthToken("firebase_token")
                        sessionManager.saveUserEmail(email)
                        sessionManager.setLoggedIn(true)

                        Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Navbar::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}