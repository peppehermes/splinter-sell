package it.polito.mad.splintersell.ui.item_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
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
        holder.b.setOnClickListener(View.OnClickListener { v ->
            var bundle= Bundle()
            bundle.putString("EDIT_TITLE", items[position].title)
            bundle.putString("EDIT_DESCRIPTION", items[position].description)
            bundle.putString("EDIT_PRICE", items[position].price.toString())
            bundle.putInt("EDIT_POSITION",position)
            v.findNavController().navigate(R.id.action_nav_item_list_to_nav_edit_item,bundle)
        })
    }

    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.card_title)
        val description: TextView = v.findViewById(R.id.card_description)
        val price: TextView = v.findViewById(R.id.card_price)
        val b : Button=v.findViewById(R.id.card_edit)

        fun bind(i: Item) {
            // Take item data and spread it
            title.text =  i.title
            description.text = i.description
            price.text = i.price.toString()

        }
    }
}