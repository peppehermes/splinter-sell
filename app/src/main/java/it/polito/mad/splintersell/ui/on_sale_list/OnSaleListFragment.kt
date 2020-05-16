package it.polito.mad.splintersell.ui.on_sale_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.data.ItemModelHolder
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.user
import kotlinx.android.synthetic.main.fragment_on_sale_list.*


class OnSaleListFragment : Fragment() {
    private var adapter: FirestoreRecyclerAdapter<ItemModel, ItemModelHolder>? = null

    private var onSaleItemList = mutableListOf<ItemModel>()
    private var firestoreListener: ListenerRegistration? = null
    private var firestoreDB: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestoreDB = FirebaseFirestore.getInstance()

        // Get items on sale
        val query: Query = firestoreDB!!
            .collection("items")
            .whereGreaterThan("ownerId", user!!.uid)
            .whereLessThan("ownerId", user.uid)

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * ItemModel.class instructs the adapter to convert each DocumentSnapshot to a ItemModel object
        val options = FirestoreRecyclerOptions.Builder<ItemModel>()
            .setQuery(query, ItemModel::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<ItemModel, ItemModelHolder>(options) {
            override fun onBindViewHolder(
                holder: ItemModelHolder,
                position: Int,
                model: ItemModel
            ) {
                // Bind the ItemModel object to the ItemModelHolder
                holder.title.text = model.title
                holder.description.text = model.description
                holder.price.text = model.price
                holder.mainCategory = model.mainCategory
                holder.secondCategory = model.secondCategory
                holder.location = model.location
                holder.expireDate = model.expireDate
                holder.documentName = model.documentName
                holder.ownerId = model.ownerId

                holder.button.visibility = View.GONE

                // Set the onClick listener
                holder.card.setOnClickListener {
                    navigateToItemDetails(holder.itemView, model.documentName!!)
                }
            }

            override fun onCreateViewHolder(group: ViewGroup, i: Int): ItemModelHolder {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                val v: View = LayoutInflater.from(group.context)
                    .inflate(R.layout.item_card, group, false)
                return ItemModelHolder(v)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val itemList = inflater.inflate(R.layout.fragment_on_sale_list, container, false)

        val itemRecyclerView = itemList.findViewById<View>(R.id.on_sale_list) as RecyclerView

        adapter!!.notifyDataSetChanged()
        itemRecyclerView.layoutManager = LinearLayoutManager(context)
        itemRecyclerView.setHasFixedSize(true)
        itemRecyclerView.adapter = adapter

        firestoreListener = firestoreDB!!.collection("items")
            .whereGreaterThan("ownerId", user!!.uid)
            .whereLessThan("ownerId", user.uid)
            .addSnapshotListener(EventListener { documentSnapshots, e ->
                if (e != null) {
                    Log.e("OnSale", "Listen failed!", e)
                    return@EventListener
                }

                onSaleItemList = mutableListOf()

                if (documentSnapshots != null) {
                    for (doc in documentSnapshots) {
                        val item = doc.toObject(ItemModel::class.java)
                        item.documentName = doc.id
                        Log.e("IDTAG", "${item.documentName}")
                        onSaleItemList.add(item)
                    }
                }

                adapter!!.notifyDataSetChanged()
                itemRecyclerView.adapter = adapter
            })
        return itemList
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun navigateToItemDetails(view: View, documentName: String) {
        val action = OnSaleListFragmentDirections.showOnSaleItem(documentName, true)
        Log.e("DOC", documentName)
        Navigation.findNavController(view).navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()

        firestoreListener!!.remove()
    }

    override fun onStart() {
        super.onStart()

        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()

        adapter!!.stopListening()
    }
}
