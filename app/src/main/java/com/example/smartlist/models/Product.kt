package com.example.smartlist.models

import com.google.firebase.firestore.DocumentId

data class Product(
    @DocumentId var id: String = "",
    var name: String = "",
    var quantity: Int = 1,
    var checked: Boolean = false
)