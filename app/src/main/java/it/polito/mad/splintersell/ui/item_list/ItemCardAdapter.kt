package it.polito.mad.splintersell.ui.item_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import it.polito.mad.splintersell.Item
import it.polito.mad.splintersell.R

class ItemCardAdapter(private var items: ArrayList<Item>): RecyclerView.Adapter<ItemCardAdapter.ViewHolder>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            items[position],
            { pos: Int ->
                navigateToItemDetails(holder.itemView, pos)
            },
            { pos: Int ->
                navigateToItemEdit(holder.itemView, pos)
            })
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    // Other methods
    private fun navigateToItemDetails(view: View, position: Int) {
        val action = ItemListFragmentDirections.showItemDetail(position)
        Navigation.findNavController(view).navigate(action)
    }

    private fun navigateToItemEdit(view: View, position: Int) {
        val action = ItemListFragmentDirections.editListItem(position)
        Navigation.findNavController(view).navigate(action)
    }

    fun updateItems(newItems: ArrayList<Item>) : ArrayList<Item> {
        val diffs = DiffUtil.calculateDiff(
            ItemDiffCallback(items, newItems))
        items = newItems // update data
        diffs.dispatchUpdatesTo(this) // animate UI
        return items
    }

    class ItemDiffCallback(private val items: ArrayList<Item>,
        private val newItems: ArrayList<Item>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = items.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return items[oldItemPosition].id == newItems[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val (image1, title1, description1, price1, category1) = items[oldItemPosition]
            val (image2, title2, description2, price2, category2) = newItems[newItemPosition]

            return image1 == image2 &&
                title1 == title2 &&
                description1 == description2 &&
                price1 == price2 &&
                category1 == category2
        }
    }

    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val card: MaterialCardView = v.findViewById(R.id.card)
        private val image: ImageView = v.findViewById(R.id.card_image)
        private val button : Button = v.findViewById(R.id.card_edit)
        val title: TextView = v.findViewById(R.id.card_title)
        val description: TextView = v.findViewById(R.id.card_description)
        val price: TextView = v.findViewById(R.id.card_price)

        fun bind(i: Item, showDetails: (Int) -> Unit, editItem: (Int) -> Unit) {
            // Take item data and spread it
            if (i.image != null)
                image.setImageBitmap(i.image)
            title.text =  i.title
            description.text = i.description
            if (i.price != "Price") {
                val tmp = "${i.price} $"
                price.text = tmp
            }
            else price.text = i.price

            // Set the onClick listener
            card.setOnClickListener { showDetails(adapterPosition) }
            button.setOnClickListener { editItem(adapterPosition) }
        }

        fun unbind() { card.setOnClickListener(null); button.setOnClickListener(null) }
    }
}