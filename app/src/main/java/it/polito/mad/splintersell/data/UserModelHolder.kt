import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.material.card.MaterialCardView
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.UserModel
import it.polito.mad.splintersell.data.storage

class UserModelHolder(v: View) : RecyclerView.ViewHolder(v) {
    val card : MaterialCardView = v.findViewById(R.id.card)
    val image: ImageView = v.findViewById(R.id.card_image)
    val button : Button = v.findViewById(R.id.card_edit)
    val nickname : TextView = v.findViewById(R.id.card_title)
    val description : TextView = v.findViewById(R.id.card_description)
    val price : TextView = v.findViewById(R.id.card_price)
    var fullname : String? = null
    var email : String? = null
    var location : String? = null
    var userid : String? = null



    fun bind(model: UserModel) {
        // Bind the UserModel object to the UserModelHolder
        nickname.text = model.nickname
        fullname = model.fullname
        email = model.email
        location = model.location
        userid = model.userid
        description.visibility = View.GONE
        price.visibility = View.GONE



        val sref = storage.child("profileImages/${model.photoName}")

        Glide.with(image.context)
            .using(FirebaseImageLoader())
            .load(sref)
            .into(image)


    }
}