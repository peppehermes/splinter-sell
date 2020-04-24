package it.polito.mad.splintersell.ui.item_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.Item
import it.polito.mad.splintersell.R

class ItemCardAdapter(val items: ArrayList<Item>): RecyclerView.Adapter<ItemCardAdapter.ViewHolder>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])

        holder.itemView.setOnClickListener {
            val action = ItemListFragmentDirections.showItemDetail(position)
            it.findNavController().navigate(action)
        }

        holder.button.setOnClickListener {
            val action = ItemListFragmentDirections.editListItem(position)
            it.findNavController().navigate(action)
        }
    }

    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.card_image)
        val title: TextView = v.findViewById(R.id.card_title)
        val description: TextView = v.findViewById(R.id.card_description)
        val price: TextView = v.findViewById(R.id.card_price)
        val button : Button = v.findViewById(R.id.card_edit)

        fun bind(i: Item) {
            // Take item data and spread it
            if (i.image != null)
                image.setImageBitmap(i.image)
            title.text =  i.title
            description.text = i.description
            price.text = i.price
        }
    }
}