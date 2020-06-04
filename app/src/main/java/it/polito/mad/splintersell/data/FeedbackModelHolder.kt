package it.polito.mad.splintersell.data

import android.view.View
import android.widget.TextView
import android.widget.RatingBar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import it.polito.mad.splintersell.R

class FeedbackModelHolder(v: View) : RecyclerView.ViewHolder(v) {
    val card: MaterialCardView = v.findViewById(R.id.feed_card)
    val nick: TextView = v.findViewById(R.id.card_nick)
    val item: TextView = v.findViewById(R.id.card_item)
    val comment: TextView = v.findViewById(R.id.card_comment)
    val rating: RatingBar = v.findViewById(R.id.card_rating)
    var userid: String? = null
    var itemid: String? = null

    fun bind(model: FeedbackModel) {
        // Bind the ItemModel object to the ItemModelHolder
        nick.text = model.user_nick
        item.text = model.item_title
        comment.text = model.comment
        rating.rating = model.rate!!
        userid = model.id_user
        itemid = model.id_item
    }

}
