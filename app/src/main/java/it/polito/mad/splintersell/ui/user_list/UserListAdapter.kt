package it.polito.mad.splintersell.ui.user_list

import UserModelHolder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import java.util.*
import kotlin.collections.ArrayList

class UserListAdapter(private var UserList: ArrayList<UserModel>, itemID: String) :
    RecyclerView.Adapter<UserModelHolder>(), Filterable {
    private var userFilterList = ArrayList<UserModel>()

    private var idItem = itemID

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

        holder.button.text = holder.itemView.context.getString(R.string.accept)

        holder.button.setOnClickListener {

            val builder = AlertDialog.Builder(it.context)
            builder.setMessage("Are you sure you want to accept?").setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    // Delete selected note from database

                    FirestoreViewModel().setSoldTo(item.userId!!, idItem)

                    FirestoreViewModel().updateStatus(
                        holder.itemView.context.getString(R.string.sold), idItem
                    )

                    navigateToMyItemList(holder.itemView)

                }.setNegativeButton("No") { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }

            val alert = builder.create()
            alert.show()

        }

        holder.card.setOnClickListener {
            navigateToUserProfile(holder.itemView, item.userId!!)
        }
    }

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
                                charSearch.toLowerCase(Locale.ROOT)
                            ) || row.nickname!!.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(Locale.ROOT)
                            ) || row.email!!.toLowerCase(Locale.ROOT).contains(
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
        val action = UserListFragmentDirections.showProfile(id)
        Log.e("POS", id)
        findNavController(view).navigate(action)
    }

    private fun navigateToMyItemList(view: View) {
        findNavController(view).navigate(R.id.nav_item_list)

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}