package it.polito.mad.splintersell.ui.feedback_list

import UserModelHolder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FeedbackModel
import it.polito.mad.splintersell.data.FeedbackModelHolder
import java.util.*
import kotlin.collections.ArrayList

class FeedbackListAdapter(private var FeedbackList: ArrayList<FeedbackModel>) :
    RecyclerView.Adapter<FeedbackModelHolder>(), Filterable {
    private var feedFilterList = ArrayList<FeedbackModel>()

    init {
        feedFilterList = FeedbackList
    }

    fun setFeedbackList(feedbackList: ArrayList<FeedbackModel>) {
        this.FeedbackList = feedbackList
        feedFilterList = ArrayList(feedbackList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackModelHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_card, parent, false)
        return FeedbackModelHolder(view)
    }

    override fun getItemCount(): Int {
        return feedFilterList.size
    }

    override fun onBindViewHolder(holder: FeedbackModelHolder, position: Int) {
        // Bind the FeedbackModel object to the feedbackModelHolder
        val item = feedFilterList[position]
        holder.bind(item)

        holder.nick.setOnClickListener {
            navigateToUserProfile(holder.itemView, item.id_user!!)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()

                feedFilterList = if (charSearch.isEmpty()) {
                    FeedbackList
                } else {
                    val resultList = ArrayList<FeedbackModel>()
                    for (row in FeedbackList) {
                        if (row.user_nick!!.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(Locale.ROOT)
                            ) || row.id_item!!.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(Locale.ROOT)
                            ) || row.comment!!.toLowerCase(Locale.ROOT).contains(
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
                filterResults.values = feedFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                feedFilterList = results?.values as ArrayList<FeedbackModel>
                notifyDataSetChanged()
            }

        }
    }

    private fun navigateToUserProfile(view: View, id: String) {
        val action = FeedbackListFragmentDirections.showUserProfile(id)
        Log.e("POS", id)
        findNavController(view).navigate(action)
    }

}