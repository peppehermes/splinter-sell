package it.polito.mad.splintersell.ui.item_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.Item
import it.polito.mad.splintersell.R

class ItemCardAdapter(val items: Array<Item>): RecyclerView.Adapter<ItemCardAdapter.ViewHolder>() {
    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.card_title)
        val description: TextView = v.findViewById(R.id.card_description)
        val price: TextView = v.findViewById(R.id.card_price)

        fun bind(i: Item) {
            // Take item data and spread it
            title.text =  i.title
            description.text = i.description
            price.text = i.price.toString()
        }
    }
}