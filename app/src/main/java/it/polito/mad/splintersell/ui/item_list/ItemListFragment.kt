package it.polito.mad.splintersell.ui.item_list

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.polito.mad.splintersell.*
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.data.ItemModelHolder
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_item_list.*
import java.io.File
import java.io.FileInputStream

class ItemListFragment : Fragment() {
    private val firestoreViewModel: FirestoreViewModel by viewModels()

    lateinit var myItemList: LiveData<List<ItemModel>>
    private var adapter: FirestoreRecyclerAdapter<ItemModel, ItemModelHolder>? = null
    private val user = Firebase.auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestoreViewModel.fetchMyItemListFromFirestore()
        myItemList = firestoreViewModel.myItemList

        // Take my items
        val query: Query = FirebaseFirestore.getInstance()
            .collection("items")
            .whereEqualTo("ownerId", user!!.uid)
            .whereEqualTo("status","Available")

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
                holder.bind(model)

                // Set the onClick listener
                holder.card.setOnClickListener {
                    navigateToItemDetails(holder.itemView, model.documentName!!)
                }
                holder.button.setOnClickListener {
                    navigateToItemEdit(holder.itemView, model.documentName!!)
                }
            }

            override fun onCreateViewHolder(group: ViewGroup, i: Int): ItemModelHolder {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                val v: View = LayoutInflater.from(group.context)
                    .inflate(R.layout.item_card, group, false)
                return ItemModelHolder(v)
            }

            override fun onDataChanged() {
                // Called each time there is a new query snapshot. You may want to use this method
                // to hide a loading spinner or check for the "no documents" state and update your UI.
                // ...

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val itemList = inflater.inflate(R.layout.fragment_item_list, container, false)

        val itemRecyclerView = itemList.findViewById<View>(R.id.item_list) as RecyclerView

        adapter!!.notifyDataSetChanged()
        itemRecyclerView.layoutManager = LinearLayoutManager(this.context)
        itemRecyclerView.setHasFixedSize(true)
        itemRecyclerView.adapter = adapter

        myItemList.observe(viewLifecycleOwner, Observer { items ->
            hideNoItemsHere(items)
        })

        return itemList
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        fab.setOnClickListener {
            val documentName = "${user!!.uid}_${myItemList.value!!.size}"
            val action = ItemListFragmentDirections.newItem(documentName)
            Log.e("DOC", documentName)
            it.findNavController().navigate(action)
        }
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun navigateToItemDetails(view: View, id: String) {
        val action = ItemListFragmentDirections.showItemDetail(id, false)
        Log.e("POS", id)
        Navigation.findNavController(view).navigate(action)
    }

    private fun navigateToItemEdit(view: View, id: String) {
        val action = ItemListFragmentDirections.editListItem(id)
        Navigation.findNavController(view).navigate(action)
    }

    private fun hideNoItemsHere(list: List<ItemModel>) {
        if (list.isEmpty())
            empty_list.visibility = View.VISIBLE
        else
            empty_list.visibility = View.GONE
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
