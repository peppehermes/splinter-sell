package it.polito.mad.splintersell.ui.user_list

import UserModelHolder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.ItemModelHolder
import it.polito.mad.splintersell.data.UserModel
import java.util.*
import kotlin.collections.ArrayList

class UserListAdapter (private var UserList: ArrayList<UserModel>, ownerID : String)
    : RecyclerView.Adapter<UserModelHolder>(), Filterable {
    private var userFilterList = ArrayList<UserModel>()

    init {
        userFilterList = UserList
    }

    fun setUsersList(userList: ArrayList<UserModel>) {
        this.UserList = userList
        userFilterList = ArrayList(userList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserModelHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return UserModelHolder(view)
    }

    override fun getItemCount(): Int {
        return userFilterList.size
    }

    override fun onBindViewHolder(holder: UserModelHolder, position: Int) {
        // Bind the ItemModel object to the ItemModelHolder
        val item = userFilterList[position]
        holder.bind(item)

        holder.button.text = "Accept"
/*
    // Set the onClick listener
     holder.card.setOnClickListener {
         navigateToUserProfile(holder.itemView,ownerID)
     }

     holder.button.setOnClickListener {
         navigateToUserDetails(holder.itemView, item.ownerId!!)
     }
*/}
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()

                userFilterList = if (charSearch.isEmpty()) {
                    UserList
                } else {
                    val resultList = ArrayList<UserModel>()
                    for (row in UserList) {
                        if (row.fullname!!.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(Locale.ROOT))
                            || row.nickname!!.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(Locale.ROOT))
                            || row.email!!.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(Locale.ROOT))) {

                            // Add the item if it contains the searched word
                            resultList.add(row)

                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = userFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                userFilterList = results?.values as ArrayList<UserModel>
                notifyDataSetChanged()
            }

        }
    }

    private fun navigateToUserProfile(view: View, id: String) {
        val action = UserListFragmentDirections.goToInterestedUserProfile(id)
        Log.e("POS", id)
        Navigation.findNavController(view).navigate(action)
    }

}