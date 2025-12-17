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

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Lista temporal de productos
    private val productos = mutableListOf<Producto>()

    data class Producto(
        val nombre: String,
        val cantidad: Int
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Botón Cancelar: Limpia el formulario
        binding.btnCancel.setOnClickListener {
            limpiarFormulario()
        }

        // Botón Guardar: Guarda lista con productos
        binding.btnSave.setOnClickListener {
            guardarListaConProductos()
        }

        // Botón Agregar Producto: Añade producto a la lista temporal
        binding.btnAddProduct.setOnClickListener {
            agregarProducto()
        }

        // Actualizar UI inicial
        actualizarListaProductosUI()
    }

    private fun agregarProducto() {
        val nombre = binding.etProductName.text.toString().trim()
        val cantidadStr = binding.etProductQuantity.text.toString().trim()

        // Validaciones
        if (nombre.isEmpty()) {
            binding.etProductName.error = "Ingresa un nombre"
            return
        }

        val cantidad = if (cantidadStr.isEmpty()) 1 else cantidadStr.toIntOrNull() ?: 1

        if (cantidad <= 0) {
            binding.etProductQuantity.error = "La cantidad debe ser mayor a 0"
            return
        }

        // Agregar producto a la lista temporal
        productos.add(Producto(nombre, cantidad))

        // Actualizar UI
        actualizarListaProductosUI()

        // Limpiar campos del formulario de producto
        binding.etProductName.text?.clear()
        binding.etProductQuantity.setText("1")

        Toast.makeText(requireContext(), "Producto agregado", Toast.LENGTH_SHORT).show()
    }

    private fun actualizarListaProductosUI() {
        if (productos.isEmpty()) {
            binding.tvProductsList.text = "No hay productos agregados..."
            binding.tvProductCount.text = "Total: 0 productos"
            return
        }

        val listaTexto = StringBuilder()
        productos.forEachIndexed { index, producto ->
            listaTexto.append("${index + 1}. ${producto.nombre} (x${producto.cantidad})\n")
        }

        binding.tvProductsList.text = listaTexto.toString()
        binding.tvProductCount.text = "Total: ${productos.size} productos"
    }

    private fun guardarListaConProductos() {
        val titulo = binding.etTitle.text.toString().trim()

        // Validaciones
        if (titulo.isEmpty()) {
            binding.etTitle.error = "Ingresa un título para la lista"
            return
        }

        if (productos.isEmpty()) {
            Toast.makeText(requireContext(), "Agrega al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val listData = hashMapOf(
            "titulo" to titulo,
            "descripcion" to binding.etDescription.text.toString().trim(),
            "completada" to false,
            "fechaCreacion" to FieldValue.serverTimestamp(),
            "cantidadProductos" to productos.size
        )

        db.collection("usuarios")
            .document(userId)
            .collection("listas")
            .add(listData)
            .addOnSuccessListener { documentReference ->
                val listaId = documentReference.id
                // 2. Guardar productos en subcolección
                guardarProductosEnFirestore(userId, listaId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al crear lista: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarProductosEnFirestore(userId: String, listaId: String) {
        // Usar batch para guardar todos los productos atómicamente
        val batch = db.batch()

        productos.forEach { producto ->
            val productRef = db.collection("usuarios")
                .document(userId)
                .collection("listas")
                .document(listaId)
                .collection("productos")
                .document()

            val productData = hashMapOf(
                "nombre" to producto.nombre,
                "cantidad" to producto.cantidad,
                "marcado" to false,
                "fechaAgregado" to FieldValue.serverTimestamp()
            )

            batch.set(productRef, productData)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Lista guardada con ${productos.size} productos", Toast.LENGTH_SHORT).show()
                limpiarFormulario()

                (activity as? Navbar)?.navigateToTab(R.id.list)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al guardar productos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarFormulario() {
        // Limpiar todos los campos
        binding.etTitle.text?.clear()
        binding.etDescription.text?.clear()
        binding.etProductName.text?.clear()
        binding.etProductQuantity.setText("1")
        binding.tvProductsList.text = "No hay productos agregados..."
        binding.tvProductCount.text = "Total: 0 productos"
        productos.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}