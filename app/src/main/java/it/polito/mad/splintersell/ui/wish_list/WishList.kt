package it.polito.mad.splintersell.ui.wish_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.ui.on_sale_list.OnSaleListAdapter
import kotlinx.android.synthetic.main.fragment_wish_list.*

class WishList : Fragment() {
    private val TAG = "WISHLIST"

    private val firestoreViewModel: FirestoreViewModel by viewModels()

    private var list = arrayListOf<ItemModel>()
    private lateinit var adapter: OnSaleListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firestoreViewModel.fetchAllNotificationsFromFirestore()

        return inflater.inflate(R.layout.fragment_wish_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemRecyclerView = view.findViewById<View>(R.id.wish_list) as RecyclerView

        adapter = OnSaleListAdapter(list)
        itemRecyclerView.layoutManager = LinearLayoutManager(context)
        itemRecyclerView.setHasFixedSize(true)
        itemRecyclerView.adapter = adapter

        firestoreViewModel.myNotificationsList.observe(viewLifecycleOwner, Observer {

            Log.e(TAG, "NOTIFICATIONS UPDATED")
            firestoreViewModel.firestoreRepository.getNotificationData()
            firestoreViewModel.wishItemsList.observe(viewLifecycleOwner, Observer { wishItemList ->
                Log.e(TAG, "WISHLIST UPDATED")
                for (elem in wishItemList) Log.e("TAG", elem.toString())
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
        if (list.isNullOrEmpty()) {
            Log.e(TAG, "CACCA")
            empty_list_wish.visibility = View.VISIBLE
        } else {
            Log.e(TAG, "PUPÙ")
            empty_list_wish.visibility = View.GONE
        }

    }


}



