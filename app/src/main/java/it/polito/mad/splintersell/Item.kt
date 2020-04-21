package it.polito.mad.splintersell

import java.util.*

data class Item(
                val title: String,
                val description: String,
                val price: String,
                val category: String
) {
    constructor(
                photoUri: String,
                title: String,
                description: String,
                price: String,
                category: String,
                location: String,
                expire_date: String) :  this(title, description, price,category)
}