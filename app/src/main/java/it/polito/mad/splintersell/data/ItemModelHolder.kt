package it.polito.mad.splintersell.data

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.material.card.MaterialCardView
import it.polito.mad.splintersell.R

class ItemModelHolder(v: View) : RecyclerView.ViewHolder(v) {
    val card: MaterialCardView = v.findViewById(R.id.card)
    val image: ImageView = v.findViewById(R.id.card_image)
    var button: Button = v.findViewById(R.id.card_edit)
    var jolly: Button = v.findViewById(R.id.card_jolly_button)
    val title: TextView = v.findViewById(R.id.card_title)
    val description: TextView = v.findViewById(R.id.card_description)
    val price: TextView = v.findViewById(R.id.card_price)
    var mainCategory: String? = null
    var secondCategory: String? = null
    var location: String? = null
    var expireDate: String? = null
    var documentName: String? = null
    var ownerId: String? = null
    var status: String? = null

    fun bind(model: ItemModel) {
        // Bind the ItemModel object to the ItemModelHolder
        description.text = model.description
        title.text = model.title
        val priceText = "${model.price} $"
        price.text = priceText
        mainCategory = model.mainCategory
        secondCategory = model.secondCategory
        location = model.location
        expireDate = model.expireDate
        documentName = model.documentName
        ownerId = model.ownerId
        status = model.status

        //set image into the card
        val sref = storage.child("itemImages/${model.imgPath}")

        image.transitionName = model.imgPath
        Glide.with(image.context)
            .using(FirebaseImageLoader())
            .load(sref)
            .into(image)


        /*Glide.with(holder.itemView.context)
            //.using(FirebaseImageLoader())
            .load("https://firebasestorage.googleapis.com/v0/b/***REMOVED***.appspot.com/o/itemImages%2FG7B0a6aFsOtLcHYmXF26.jpg?alt=media&token=cef98d74-942d-46bc-bce6-60e6ce0ef23a")
            .into(holder.image)*/
    }
}