package com.example.smartlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartlist.databinding.FragmentEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class Edit : Fragment() {

    // Binding en lugar de synthetic
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Usar View Binding
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Acceder a las vistas a través del binding
        binding.btnCancel.setOnClickListener {
            // Limpiar campos
            binding.etTitle.setText("")
            binding.etDescription.setText("")
        }

        binding.btnSave.setOnClickListener {
            saveShoppingList()
        }

        binding.btnAddProduct.setOnClickListener {
            Toast.makeText(requireContext(), "Agregar producto - implementar luego", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveShoppingList() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Ingresa un título"
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val listData = hashMapOf(
            "titulo" to title,
            "descripcion" to description,
            "completada" to false,
            "fechaCreacion" to FieldValue.serverTimestamp(),
            "cantidadProductos" to 0
        )

        db.collection("usuarios")
            .document(userId)
            .collection("listas")
            .add(listData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Lista guardada exitosamente", Toast.LENGTH_SHORT).show()
                binding.etTitle.setText("")
                binding.etDescription.setText("")
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}