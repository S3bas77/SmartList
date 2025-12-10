package com.example.smartlist

data class Product(
    var id: String = "",
    var name: String = "",
    var quantity: Int = 1,
    var checked: Boolean = false
)
