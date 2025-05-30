package it.polito.mad.splintersell.ui.on_sale_list

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.data.ItemModelHolder
import java.util.*
import kotlin.collections.ArrayList

class OnSaleListAdapter(private var onSaleItemList: ArrayList<ItemModel>) :
    RecyclerView.Adapter<ItemModelHolder>(), Filterable {
    private var itemFilterList = ArrayList<ItemModel>()

    init {
        itemFilterList = onSaleItemList
    }

    fun setOnSaleItemList(onSaleItemList: ArrayList<ItemModel>) {
        this.onSaleItemList = onSaleItemList
        itemFilterList = ArrayList(onSaleItemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemModelHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ItemModelHolder(view)
    }

    override fun getItemCount(): Int {
        return itemFilterList.size
    }

    override fun onBindViewHolder(holder: ItemModelHolder, position: Int) {
        // Bind the ItemModel object to the ItemModelHolder
        val item = itemFilterList[position]
        holder.bind(item)

        //manageStatus(holder, item.status!!)

        holder.button.text = holder.itemView.context.getString(R.string.view_user)


        // Set the onClick listener
        holder.card.setOnClickListener {
            navigateToItemDetails(holder.itemView, item.documentName!!, item.ownerId!!)
        }

        holder.button.setOnClickListener {
            navigateToUserDetails(holder.itemView, item.ownerId!!)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()

                itemFilterList = if (charSearch.isEmpty()) {
                    onSaleItemList
                } else {
                    val resultList = ArrayList<ItemModel>()
                    for (row in onSaleItemList) {
                        if (row.title!!.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(Locale.ROOT)
                            ) || row.description!!.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(Locale.ROOT)
                            ) || row.location!!.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(Locale.ROOT)
                            )
                        ) {

                            // Add the item if it contains the searched word
                            resultList.add(row)

                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = itemFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                itemFilterList = results?.values as ArrayList<ItemModel>
                notifyDataSetChanged()
            }

        }
    }

    private fun navigateToItemDetails(view: View, id: String, ownerId: String) {
        val action = OnSaleListFragmentDirections.showItemDetails(id, true, ownerId)
        Log.e("POS", id)
        findNavController(view).navigate(action)
    }

    private fun navigateToUserDetails(view: View, id: String) {
        val action = OnSaleListFragmentDirections.showProfile(id)
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