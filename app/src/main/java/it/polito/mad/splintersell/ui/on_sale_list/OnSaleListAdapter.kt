package it.polito.mad.splintersell.ui.on_sale_list

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.data.ItemModelHolder

class OnSaleListAdapter: RecyclerView.Adapter<ItemModelHolder>() {
    private var onSaleItemList = listOf<ItemModel>()

    fun setOnSaleItemList(onSaleItemList: List<ItemModel>) {
        this.onSaleItemList = onSaleItemList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemModelHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ItemModelHolder(view)
    }

    override fun getItemCount(): Int {
        return if (onSaleItemList.isNullOrEmpty())
            0
        else
            onSaleItemList.size
    }

    override fun onBindViewHolder(holder: ItemModelHolder, position: Int) {
        // Bind the ItemModel object to the ItemModelHolder
        val item = onSaleItemList[position]
        holder.bind(item)

        // Hide EDIT button
        holder.button.visibility = View.GONE

        // Set the onClick listener
        holder.card.setOnClickListener {
            navigateToItemDetails(holder.itemView, item.documentName!!)
        }
    }

    private fun navigateToItemDetails(view: View, id: String) {
        val action = OnSaleListFragmentDirections.showOnSaleItem(id, true)
        Log.e("POS", id)
        Navigation.findNavController(view).navigate(action)
    }
}