package it.polito.mad.splintersell

import java.util.*

data class Item(val title: String,
                val description: String,
                val price: Float
) {
    constructor(photoUri: String,
                title: String,
                description: String,
                price: Float,
                category: String,
                location: String,
                expire_date: Date) : this(title, description, price)
}