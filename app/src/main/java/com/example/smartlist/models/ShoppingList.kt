package com.example.smartlist.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Nombres en ESPAÑOL (coinciden exactamente con Firestore)
data class ShoppingList(
    @DocumentId
    var id: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var completada: Boolean = false,
    var fechaCreacion: Timestamp? = null,
    var cantidadProductos: Int = 0
)