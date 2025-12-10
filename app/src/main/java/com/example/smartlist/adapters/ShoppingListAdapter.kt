package com.example.smartlist.adapters

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
    private val onToggleComplete: (ShoppingList) -> Unit
) : RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvCount: TextView = itemView.findViewById(R.id.tvCount)
        val btnMarkComplete: MaterialButton = itemView.findViewById(R.id.btnMarkComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]

        holder.tvTitle.text = list.title
        holder.tvDescription.text = list.description
        holder.tvCount.text = "${list.productCount} productos"

        // Sin colores por ahora
        if (list.completed) {
            holder.btnMarkComplete.text = "Desmarcar"
        } else {
            holder.btnMarkComplete.text = "Marcar"
        }

        holder.btnMarkComplete.setOnClickListener {
            onToggleComplete(list)
        }

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