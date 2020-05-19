package it.polito.mad.splintersell.ui.wish_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference

import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.*
import it.polito.mad.splintersell.ui.on_sale_list.OnSaleListAdapter
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.android.synthetic.main.fragment_wish_list.*
import java.util.concurrent.TimeUnit

class WishList : Fragment() {

    private val firestoreViewModel: FirestoreViewModel by viewModels()

    private var list = arrayListOf<ItemModel>()
    private lateinit var adapter: OnSaleListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firestoreViewModel.fetchAllNotificationsFromFirestore()
        val wishList = inflater.inflate(R.layout.fragment_wish_list, container, false)

        return wishList
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemRecyclerView = view.findViewById<View>(R.id.wish_list) as RecyclerView

        adapter = OnSaleListAdapter(list)
        itemRecyclerView.layoutManager = LinearLayoutManager(context)
        itemRecyclerView.setHasFixedSize(true)
        itemRecyclerView.adapter = adapter

        firestoreViewModel.myNotificationsList.observe(viewLifecycleOwner, Observer {

            firestoreViewModel.firestoreRepository.getNotificationData()
            firestoreViewModel.wishItemsList.observe(viewLifecycleOwner, Observer {wishItemList ->
                adapter.setOnSaleItemList(wishItemList as ArrayList<ItemModel>)
                adapter.notifyDataSetChanged()
                hideNoItemsHere(wishItemList)

            })
        })

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun hideNoItemsHere(list: List<ItemModel>) {
        if (list.isEmpty())
            empty_list_wish.visibility = View.VISIBLE
        else
            empty_list_wish.visibility = View.GONE
    }


}



