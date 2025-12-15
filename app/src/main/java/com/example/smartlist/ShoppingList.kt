package com.example.smartlist

import com.google.firebase.Timestamp

data class ShoppingList(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var completed: Boolean = false,
    var createdAt: Timestamp? = null
)
