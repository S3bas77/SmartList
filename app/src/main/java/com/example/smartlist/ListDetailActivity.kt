package com.example.smartlist

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartlist.adapters.ProductAdapter
import com.example.smartlist.databinding.ActivityListDetailBinding
import com.example.smartlist.models.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ListDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ProductAdapter
    private var registration: ListenerRegistration? = null

    private var listaId: String = ""
    private var listaCompletada: Boolean = false
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos de la lista
        listaId = intent.getStringExtra("LISTA_ID") ?: return finish()
        val listaTitulo = intent.getStringExtra("LISTA_TITULO") ?: "Lista"
        listaCompletada = intent.getBooleanExtra("LISTA_COMPLETADA", false)

        // Configurar toolbar
        binding.toolbar.title = listaTitulo
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userId = auth.currentUser?.uid ?: return finish()

        adapter = ProductAdapter { producto -> toggleProductCheck(producto) }
        binding.recyclerProducts.layoutManager = LinearLayoutManager(this)
        binding.recyclerProducts.adapter = adapter

        updateCompleteButton()

        binding.btnToggleComplete.setOnClickListener {
            toggleListComplete()
        }
        loadProducts()
    }

    private fun loadProducts() {
        registration?.remove()

        val query = db.collection("usuarios")
            .document(userId)
            .collection("listas")
            .document(listaId)
            .collection("productos")
            .orderBy("nombre")

        registration = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            val productos = mutableListOf<Producto>()
            snapshots?.documents?.forEach { document ->
                val producto = document.toObject(Producto::class.java)
                producto?.id = document.id
                producto?.let { productos.add(it) }
            }

            adapter.submitList(productos)
            updateProductCount(productos.size)
        }
    }

    private fun toggleProductCheck(producto: Producto) {
        // Actualizar estado local inmediatamente
        val nuevoEstado = !producto.marcado

        // Actualizar Firestore
        db.collection("usuarios")
            .document(userId)
            .collection("listas")
            .document(listaId)
            .collection("productos")
            .document(producto.id)
            .update("marcado", nuevoEstado)
            .addOnSuccessListener {
                // Opcional: Notificar al adaptador del cambio
                adapter.notifyItemChanged(getProductPosition(producto.id))
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                // Revertir en UI si falla
            }
    }
    private fun getProductPosition(productId: String): Int {
        // Buscar posición del producto en la lista
        return adapter.getProductoList().indexOfFirst { it.id == productId }
    }

    private fun toggleListComplete() {
        val nuevoEstado = !listaCompletada

        db.collection("usuarios")
            .document(userId)
            .collection("listas")
            .document(listaId)
            .update("completada", nuevoEstado)
            .addOnSuccessListener {
                listaCompletada = nuevoEstado
                updateCompleteButton()

                // Si está completada, marcar todos los productos
                if (nuevoEstado) {
                    marcarTodosProductosComoCompletados()
                }

                val mensaje = if (nuevoEstado)
                    "Lista marcada como terminada"
                else
                    "Lista marcada como pendiente"

                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun marcarTodosProductosComoCompletados() {
        db.collection("usuarios")
            .document(userId)
            .collection("listas")
            .document(listaId)
            .collection("productos")
            .get()
            .addOnSuccessListener { snapshots ->
                val batch = db.batch()
                snapshots?.documents?.forEach { document ->
                    val productRef = db.collection("usuarios")
                        .document(userId)
                        .collection("listas")
                        .document(listaId)
                        .collection("productos")
                        .document(document.id)

                    batch.update(productRef, "marcado", true)
                }

                batch.commit()
            }
    }

    private fun updateCompleteButton() {
        if (listaCompletada) {
            binding.btnToggleComplete.text = "Desmarcar como terminada"
            binding.btnToggleComplete.setBackgroundColor(getColor(R.color.pastel_green))
        } else {
            binding.btnToggleComplete.text = "Marcar como terminada"
            binding.btnToggleComplete.setBackgroundColor(getColor(R.color.pastel_red))
        }
    }

    private fun updateProductCount(count: Int) {
        binding.tvProductCount.text = "Productos: $count"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        registration?.remove()
    }
}