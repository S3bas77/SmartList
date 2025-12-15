package com.example.smartlist.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartlist.R
import com.example.smartlist.models.Producto

class ProductAdapter(
    private val onProductCheck: (Producto) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var productos: MutableList<Producto> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbProducto: CheckBox = itemView.findViewById(R.id.cbProducto)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productos[position]

        holder.tvNombre.text = producto.nombre
        holder.tvCantidad.text = "Cantidad: ${producto.cantidad}"
        holder.cbProducto.isChecked = producto.marcado

        // Tachar texto si está marcado
        if (producto.marcado) {
            holder.tvNombre.paintFlags = holder.tvNombre.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvCantidad.paintFlags = holder.tvCantidad.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.tvNombre.paintFlags = holder.tvNombre.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvCantidad.paintFlags = holder.tvCantidad.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.cbProducto.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != producto.marcado) {
                onProductCheck(producto)
            }
        }
    }

    override fun getItemCount() = productos.size

    fun submitList(newProductos: List<Producto>) {
        productos.clear()
        productos.addAll(newProductos)
        notifyDataSetChanged()
    }
}