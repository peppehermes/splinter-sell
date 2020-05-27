package it.polito.mad.splintersell.ui.items_of_interest

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import kotlinx.android.synthetic.main.fragment_wish_list.*

class ItemsOfInterestListFragment : Fragment() {
    private val TAG = "WISHLIST"

    private val firestoreViewModel: FirestoreViewModel by activityViewModels()
    private lateinit var externalLayout: ViewGroup
    private var list = arrayListOf<ItemModel>()
    private lateinit var adapter: ItemsOfInterestListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firestoreViewModel.fetchAllNotificationsFromFirestore()
        firestoreViewModel.fetchAllItemListFromFirestore()

        return inflater.inflate(R.layout.fragment_wish_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        externalLayout = view.findViewById(R.id.external_layout)

        val itemRecyclerView = view.findViewById<View>(R.id.wish_list) as RecyclerView

        adapter = ItemsOfInterestListAdapter(list)
        itemRecyclerView.layoutManager = LinearLayoutManager(context)
        itemRecyclerView.setHasFixedSize(true)
        itemRecyclerView.adapter = adapter

        updateUI()

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun toggleNoItemsHere(list: List<ItemModel>) {
        TransitionManager.beginDelayedTransition(externalLayout)
        if (list.isNullOrEmpty()) {
            empty_list_wish.visibility = View.VISIBLE
        } else {
            empty_list_wish.visibility = View.GONE
        }
    }

    private fun updateUI() {
        firestoreViewModel.myNotificationsList.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "NOTIFICATIONS UPDATED")
            firestoreViewModel.getNotificationData()
            firestoreViewModel.wishItemsList.observe(viewLifecycleOwner, Observer { wishItemList ->
                Log.e(TAG, "WISH LIST UPDATED")
                for (elem in wishItemList) Log.e(TAG, elem.toString())
                adapter.setInterestedItemsList(wishItemList as ArrayList<ItemModel>)
                adapter.notifyDataSetChanged()
                toggleNoItemsHere(wishItemList)
            })
            //firestoreViewModel.wishItemsList.removeObserver
        })

        firestoreViewModel.allItemList.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "ITEM LIST UPDATED")
            firestoreViewModel.getNotificationData()
            firestoreViewModel.wishItemsList.observe(viewLifecycleOwner, Observer { wishItemList ->
                Log.e(TAG, "WISH LIST UPDATED")
                for (elem in wishItemList) Log.e(TAG, elem.toString())
                adapter.setInterestedItemsList(wishItemList as ArrayList<ItemModel>)
                adapter.notifyDataSetChanged()
                toggleNoItemsHere(wishItemList)
            })
        })
    }

}



