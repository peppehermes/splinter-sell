package it.polito.mad.splintersell.ui.on_sale_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.data.ItemModelHolder
import kotlinx.android.synthetic.main.fragment_item_list.*


class OnSaleListFragment : Fragment() {
    private val firestoreViewModel: FirestoreViewModel by viewModels()

    private lateinit var adapter: OnSaleListAdapter
    private lateinit var listView: RecyclerView

    private val TAG = "ON_SALE_LIST_FRAGMENT"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_on_sale_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.on_sale_list)
        adapter = OnSaleListAdapter()

        listView.layoutManager = LinearLayoutManager(context)
        listView.setHasFixedSize(true)
        listView.adapter = adapter
        listView.itemAnimator = DefaultItemAnimator()

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        firestoreViewModel.onSaleItemList.observe(viewLifecycleOwner, Observer { onSaleItemList ->

            // Update UI
            adapter.setOnSaleItemList(onSaleItemList)
            adapter.notifyDataSetChanged()
            //Log.e(TAG, onSaleItemList.isEmpty().toString())
            hideNoItemsHere(onSaleItemList)

        })
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun hideNoItemsHere(list: List<ItemModel>) {
        if (list.isEmpty())
            empty_list.visibility = View.VISIBLE
        else
            empty_list.visibility = View.GONE
    }
}
