package com.example.smartlist.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ShoppingList(
    @DocumentId var id: String = "",
    var title: String = "",
    var description: String = "",
    var completed: Boolean = false,
    var createdAt: Timestamp? = null,
    var productCount: Int = 0  // Contador para mostrar en la lista
)