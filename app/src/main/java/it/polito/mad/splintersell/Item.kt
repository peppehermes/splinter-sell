package it.polito.mad.splintersell

import android.graphics.Bitmap

data class Item(
    val id: Int,
    val image: Bitmap?,
    val title: String,
    val description: String,
    val price: String,
    val category: String
) {
    constructor(
        id: Int,
        image: Bitmap,
        title: String,
        description: String,
        price: String,
        category: String,
        location: String,
        expire_date: String) :  this(id, image, title, description, price, category)
}