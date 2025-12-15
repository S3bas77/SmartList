package com.example.smartlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartlist.databinding.FragmentUsuarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Usuario : Fragment() {

    private var _binding: FragmentUsuarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        cargarDatosUsuario()

        binding.btnEditarPerfil.setOnClickListener {
            mostrarDialogoEditarNombre()
        }

        binding.btnLogOut.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cargarDatosUsuario() {
        val user = auth.currentUser

        if (user != null) {
            // Mostrar email
            binding.correoUsuario.text = "Correo: ${user.email}"

            // Cargar nombre desde Firestore
            db.collection("usuarios").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombre = document.getString("nombre") ?: "Usuario"
                        binding.nombreUsuario.text = "Nombre: $nombre"
                    } else {
                        // Si no existe el documento, crearlo
                        crearDocumentoUsuario(user.uid, user.email ?: "")
                        binding.nombreUsuario.text = "Nombre: Usuario"
                    }
                }
                .addOnFailureListener {
                    binding.nombreUsuario.text = "Nombre: Error al cargar"
                }
        }
    }

    private fun crearDocumentoUsuario(userId: String, email: String) {
        val userData = hashMapOf(
            "email" to email,
            "nombre" to "Usuario",
            "fechaRegistro" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("usuarios").document(userId)
            .set(userData)
            .addOnSuccessListener {
                // Documento creado
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al crear perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEditarNombre() {
        // Diálogo simple para editar nombre
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Editar Nombre")

        val input = android.widget.EditText(requireContext())
        input.hint = "Ingresa tu nombre"

        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val nuevoNombre = input.text.toString().trim()
            if (nuevoNombre.isNotEmpty()) {
                actualizarNombreUsuario(nuevoNombre)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun actualizarNombreUsuario(nombre: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId)
            .update("nombre", nombre)
            .addOnSuccessListener {
                binding.nombreUsuario.text = "Nombre: $nombre"
                Toast.makeText(requireContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cerrarSesion() {
        auth.signOut()

        val prefs = requireContext().getSharedPreferences("SmartListPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // 3. Ir a LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()

        Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}