package it.polito.mad.splintersell.ui.item_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.R

class ItemDetailsAdapter(private val myDataset: Array<Detail>):
    RecyclerView.Adapter<ItemDetailsAdapter.MyViewHolder>(){

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(v:View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.detail_title)
        val description: TextView = v.findViewById(R.id.detail_description)

        fun bind(d: Detail) {
            // Take user data and spread its content
            title.text = d.title
            description.text = d.description
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val detailView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(detailView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.bind(myDataset[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}