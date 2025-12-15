package com.example.smartlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartlist.adapters.ShoppingListAdapter
import com.example.smartlist.databinding.FragmentListBinding
import com.example.smartlist.models.ShoppingList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class List : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ShoppingListAdapter
    private var registration: ListenerRegistration? = null
    private var currentFilter = "pending"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        adapter = ShoppingListAdapter(
            onItemClick = { list -> openListDetail(list) },
            onDeleteList = { list -> deleteShoppingList(list) }
        )

        binding.recyclerLists.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLists.adapter = adapter

        currentFilter = "pending"

        binding.btnPending.setOnClickListener {
            if (currentFilter != "pending") {
                currentFilter = "pending"
                updateButtonStates()
                loadShoppingLists()
            }
        }

        binding.btnCompleted.setOnClickListener {
            if (currentFilter != "completed") {
                currentFilter = "completed"
                updateButtonStates()
                loadShoppingLists()
            }
        }
        updateButtonStates()
        loadShoppingLists()
    }
    private fun deleteShoppingList(list: ShoppingList) {
        val userId = auth.currentUser?.uid ?: return

        // Confirmación antes de eliminar
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar lista")
            .setMessage("¿Estás seguro de eliminar '${list.titulo}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Eliminar la lista y sus productos
                db.collection("usuarios")
                    .document(userId)
                    .collection("listas")
                    .document(list.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "✅ Lista eliminada", Toast.LENGTH_SHORT).show()
                        // Recargar listas
                        loadShoppingLists()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun updateButtonStates() {
        if (currentFilter == "pending") {
            binding.btnPending.setBackgroundColor(Color.parseColor("#6200EE"))
            binding.btnPending.setTextColor(Color.WHITE)
            binding.btnCompleted.setBackgroundColor(Color.TRANSPARENT)
            binding.btnCompleted.setTextColor(Color.parseColor("#6200EE"))
        } else {
            binding.btnPending.setBackgroundColor(Color.TRANSPARENT)
            binding.btnPending.setTextColor(Color.parseColor("#6200EE"))
            binding.btnCompleted.setBackgroundColor(Color.parseColor("#6200EE"))
            binding.btnCompleted.setTextColor(Color.WHITE)
        }
    }
    private fun loadShoppingLists() {
        val userId = auth.currentUser?.uid ?: return

        registration?.remove()

        val query = db.collection("usuarios")
            .document(userId)
            .collection("listas")
            .whereEqualTo("completada", currentFilter == "completed")
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)

        registration = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.e("DEBUG_FIREBASE", "ERROR: ${error.message}")
                return@addSnapshotListener
            }

            // 🔍 ESTOS LOGS SON CRÍTICOS:
            Log.d("DEBUG_FIREBASE", "=== INICIO DATOS FIREBASE ===")
            Log.d("DEBUG_FIREBASE", "Documentos recibidos: ${snapshots?.size() ?: 0}")

            val lists = mutableListOf<ShoppingList>()
            snapshots?.documents?.forEach { document ->
                // 1. Datos CRUDOS de Firestore
                Log.d("DEBUG_FIREBASE", "--- Documento ID: ${document.id} ---")
                Log.d("DEBUG_FIREBASE", "Datos crudos: ${document.data}")

                // 2. Mapear a objeto
                val list = document.toObject(ShoppingList::class.java)
                Log.d("DEBUG_FIREBASE", "Objeto mapeado: ${list?.titulo} | ${list?.descripcion}")

                list?.id = document.id
                list?.let { lists.add(it) }
            }

            Log.d("DEBUG_FIREBASE", "=== FIN DATOS FIREBASE ===")
            adapter.updateList(lists)
        }
    }

    private fun openListDetail(list: ShoppingList) {
        val intent = Intent(requireContext(), ListDetailActivity::class.java)
        intent.putExtra("LISTA_ID", list.id)
        intent.putExtra("LISTA_TITULO", list.titulo)
        intent.putExtra("LISTA_COMPLETADA", list.completada)
        startActivity(intent)
    }

    private fun toggleListComplete(list: ShoppingList) {
        val userId = auth.currentUser?.uid ?: return

        val newCompletedState = !list.completada

        db.collection("usuarios")
            .document(userId)
            .collection("listas")
            .document(list.id)
            .update("completada", newCompletedState)
            .addOnSuccessListener {
                // FORZAR recarga del listener actual
                loadShoppingLists()

                val message = if (newCompletedState)
                    "Lista movida a Lisats Terminadas"
                else
                    "Lista movida a Listas Pendientes"

                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        registration?.remove()
        _binding = null
    }
}