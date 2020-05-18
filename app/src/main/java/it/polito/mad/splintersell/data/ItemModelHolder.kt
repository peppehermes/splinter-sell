package it.polito.mad.splintersell.data

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import it.polito.mad.splintersell.R

class ItemModelHolder(v: View) : RecyclerView.ViewHolder(v) {
    val card: MaterialCardView = v.findViewById(R.id.card)
    val image: ImageView = v.findViewById(R.id.card_image)
    val button : Button = v.findViewById(R.id.card_edit)
    val title: TextView = v.findViewById(R.id.card_title)
    val description: TextView = v.findViewById(R.id.card_description)
    val price: TextView = v.findViewById(R.id.card_price)
    var mainCategory: String? = null
    var secondCategory: String? = null
    var location: String? = null
    var expireDate: String? = null
    var documentName: String? = null
    var ownerId: String? = null

    fun bind(model: ItemModel) {
        // Bind the ItemModel object to the ItemModelHolder
        description.text = model.description
        title.text = model.title
        price.text = model.price
        mainCategory = model.mainCategory
        secondCategory = model.secondCategory
        location = model.location
        expireDate = model.expireDate
        documentName = model.documentName
        ownerId = model.ownerId
    }
}