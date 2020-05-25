package it.polito.mad.splintersell.ui.bought_items_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.data.ItemModelHolder
import it.polito.mad.splintersell.ui.manageStatus

class BoughtItemsListAdapter(private var soldList: ArrayList<ItemModel>) :
    RecyclerView.Adapter<ItemModelHolder>() {


    fun setSoldList(soldList: ArrayList<ItemModel>) {
        this.soldList = soldList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemModelHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ItemModelHolder(view)
    }

    override fun getItemCount(): Int {
        return soldList.size
    }

    override fun onBindViewHolder(holder: ItemModelHolder, position: Int) {
        // Bind the ItemModel object to the ItemModelHolder
        val item = soldList[position]
        holder.bind(item)


            manageStatus(holder, item.status!!)
            holder.jolly.visibility = View.VISIBLE
            holder.jolly.text = holder.itemView.context.getString(R.string.remove)
            holder.jolly.setOnClickListener {
                FirestoreViewModel().firestoreRepository.removeNotification(item.documentName!!)
                FirebaseMessaging.getInstance().unsubscribeFromTopic(item.documentName!!)
                //Todo: qui va chiamato il frammento del feedback. cambia il bottone come preferisci(jolly)
            }

    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}