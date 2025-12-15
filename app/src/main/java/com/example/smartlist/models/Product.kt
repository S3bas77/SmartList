package com.example.smartlist.models

import com.google.firebase.firestore.DocumentId

data class Product(
    @DocumentId var id: String = "",
    var nombre: String = "",
    var cantidad: Int = 1,
    var marcado: Boolean = false
)