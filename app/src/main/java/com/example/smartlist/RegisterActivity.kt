package com.example.smartlist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore  // ¡Inicializar Firestore!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()  // ¡Inicializar aquí!

        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)

        // ⚠️ AGREGAR campo para nombre (necesitas EditText en tu XML)
        val nameEditText = findViewById<EditText>(R.id.etName)

        val btn = findViewById<Button>(R.id.btnRegister)

        btn.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val nombre = nameEditText.text.toString().trim()  // ⬅️ Obtener nombre del EditText

            if (email.isEmpty() || password.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ⚠️ ERROR ORIGINAL: pasabas EditText en lugar de String
            // CORREGIDO: pasar email y password (Strings)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Guardar datos del usuario en Firestore
                        val user = auth.currentUser
                        user?.let {
                            val userData = hashMapOf(
                                "email" to email,      // ⬅️ Usar email (String)
                                "nombre" to nombre,    // ⬅️ Usar nombre (String)
                                "fechaRegistro" to FieldValue.serverTimestamp()
                            )

                            db.collection("usuarios").document(it.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    // Usuario creado en Firestore también
                                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    // ⬅️ "e" ya tiene tipo inferido (Exception)
                                    Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}