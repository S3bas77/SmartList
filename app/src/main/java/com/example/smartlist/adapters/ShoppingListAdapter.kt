package com.example.smartlist.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartlist.R
import com.example.smartlist.models.ShoppingList
import com.google.android.material.button.MaterialButton

class ShoppingListAdapter(
    private var lists: MutableList<ShoppingList> = mutableListOf(),
    private val onItemClick: (ShoppingList) -> Unit,
    private val onDeleteList: (ShoppingList) -> Unit  // Cambiado de toggleComplete a delete
) : RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvCount: TextView = itemView.findViewById(R.id.tvCount)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnMarkComplete) // Mismo ID pero función diferente
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]

        Log.d("ADAPTER_DEBUG", "Mostrando lista: ${list.titulo}")

        // Mostrar datos
        holder.tvTitle.text = list.titulo
        holder.tvDescription.text = list.descripcion
        holder.tvCount.text = "${list.cantidadProductos} productos"

        // Cambiar texto del botón a "Eliminar"
        holder.btnDelete.text = "Eliminar"
        holder.btnDelete.setBackgroundColor(holder.itemView.context.getColor(R.color.pastel_red))

        // Si la lista está completada, mostrar con estilo diferente
        if (list.completada) {
            holder.tvTitle.setStrikeThrough(true)
            holder.tvTitle.alpha = 0.6f
            holder.tvDescription.alpha = 0.6f
        } else {
            holder.tvTitle.setStrikeThrough(false)
            holder.tvTitle.alpha = 1f
            holder.tvDescription.alpha = 1f
        }

        // Botón eliminar
        holder.btnDelete.setOnClickListener {
            onDeleteList(list)
        }

        // Clic en toda la tarjeta para ver detalle
        holder.itemView.setOnClickListener {
            onItemClick(list)
        }
    }

    override fun getItemCount() = lists.size

    fun updateList(newLists: List<ShoppingList>) {
        lists.clear()
        lists.addAll(newLists)
        notifyDataSetChanged()
    }
}

// Extensión para texto tachado
fun TextView.setStrikeThrough(strikeThrough: Boolean) {
    paintFlags = if (strikeThrough) {
        paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}