package com.example.smartlist.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Producto(
    @DocumentId
    var id: String = "",

    @PropertyName("nombre")
    var nombre: String = "",

    @PropertyName("cantidad")
    var cantidad: Int = 1,

    @PropertyName("marcado")
    var marcado: Boolean = false,

    @PropertyName("fechaAgregado")
    var fechaAgregado: com.google.firebase.Timestamp? = null
)