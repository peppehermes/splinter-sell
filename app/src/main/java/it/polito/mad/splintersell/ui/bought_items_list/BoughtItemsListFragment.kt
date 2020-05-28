package it.polito.mad.splintersell.ui.bought_items_list

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.ui.sign_in.SignInViewModel
import kotlinx.android.synthetic.main.fragment_bought_items_list.*


class BoughtItemsListFragment : Fragment() {

    private val firestoreViewModel: FirestoreViewModel by activityViewModels()
    private val signInViewModel: SignInViewModel by activityViewModels()
    private lateinit var externalLayout: ViewGroup
    private var list = arrayListOf<ItemModel>()
    private lateinit var adapter: BoughtItemsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        signInViewModel.authenticate()
        firestoreViewModel.fetchSoldItemListFromFirestore()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bought_items_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        externalLayout = view.findViewById(R.id.external_layout)

        val itemRecyclerView = view.findViewById<View>(R.id.bought_list) as RecyclerView

        adapter = BoughtItemsListAdapter(list)
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
            empty_list_bought.visibility = View.VISIBLE
        } else {
            empty_list_bought.visibility = View.GONE
        }
    }

    private fun updateUI() {

        firestoreViewModel.soldItemsList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { soldList ->
            Log.e("HELLO", "UPDATE")
            adapter.setSoldList(soldList as ArrayList<ItemModel>)
            adapter.notifyDataSetChanged()
            toggleNoItemsHere(soldList)
        })

    }
}
