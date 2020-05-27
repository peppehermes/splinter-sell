package it.polito.mad.splintersell.ui.items_of_interest

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.data.ItemModelHolder
import it.polito.mad.splintersell.ui.manageStatus

class ItemsOfInterestListAdapter(private var interestedItemsList: ArrayList<ItemModel>) :
    RecyclerView.Adapter<ItemModelHolder>() {

    fun setInterestedItemsList(interestedItemsList: ArrayList<ItemModel>) {
        this.interestedItemsList = interestedItemsList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemModelHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ItemModelHolder(view)
    }

    override fun getItemCount(): Int {
        return interestedItemsList.size
    }

    override fun onBindViewHolder(holder: ItemModelHolder, position: Int) {
        // Bind the ItemModel object to the ItemModelHolder
        val item = interestedItemsList[position]
        holder.bind(item)

        if (item.status!! != holder.itemView.context.getString(R.string.available)) {

            manageStatus(holder, item.status!!)
            holder.jolly.visibility = View.VISIBLE
            holder.jolly.text = holder.itemView.context.getString(R.string.remove)
            holder.jolly.setOnClickListener {
                FirestoreViewModel().removeNotification(item.documentName!!)
                FirebaseMessaging.getInstance().unsubscribeFromTopic(item.documentName!!)
            }
        } else
            holder.button.text = holder.itemView.context.getString(R.string.view_user)


        // Set the onClick listener
        holder.card.setOnClickListener {
            navigateToItemDetails(holder.itemView, item.documentName!!, item.ownerId!!)
        }

        holder.button.setOnClickListener {
            navigateToUserDetails(holder.itemView, item.ownerId!!)
        }
    }

    private fun navigateToItemDetails(view: View, id: String, ownerid: String) {
        val action = ItemsOfInterestListFragmentDirections.showItemDetails(id, true, ownerid)
        Log.e("POS", id)
        findNavController(view).navigate(action)
    }

    private fun navigateToUserDetails(view: View, id: String) {
        val action = ItemsOfInterestListFragmentDirections.showProfile(id)
        Log.e("USERID", id)
        findNavController(view).navigate(action)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}