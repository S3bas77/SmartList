package com.example.smartlist

import android.os.Bundle
import android.util.Log
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

    // Binding en lugar de synthetic
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
        // Usar View Binding
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        adapter = ShoppingListAdapter(
            onItemClick = { list -> openListDetail(list) },
            onToggleComplete = { list -> toggleListComplete(list) }
        )

        binding.recyclerLists.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLists.adapter = adapter

        binding.btnPending.setOnClickListener {
            currentFilter = "pending"
            updateButtonStates()
            loadShoppingLists()
        }

        binding.btnCompleted.setOnClickListener {
            currentFilter = "completed"
            updateButtonStates()
            loadShoppingLists()
        }

        updateButtonStates()
        loadShoppingLists()
    }

    private fun updateButtonStates() {
        binding.btnPending.isEnabled = (currentFilter != "pending")
        binding.btnCompleted.isEnabled = (currentFilter != "completed")
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
                Log.w("ListFragment", "Error al cargar listas", error)
                return@addSnapshotListener
            }

            val lists = mutableListOf<ShoppingList>()
            snapshots?.documents?.forEach { document ->
                val list = document.toObject(ShoppingList::class.java)
                list?.id = document.id
                list?.let { lists.add(it) }
            }

            adapter.updateList(lists)
        }
    }

    private fun openListDetail(list: ShoppingList) {
        Toast.makeText(
            requireContext(),
            "Abrir lista: ${list.title}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun toggleListComplete(list: ShoppingList) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("listas")
            .document(list.id)
            .update("completada", !list.completed)
            .addOnFailureListener { e ->
                Log.e("ListFragment", "Error al actualizar lista", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        registration?.remove()
        _binding = null
    }
}