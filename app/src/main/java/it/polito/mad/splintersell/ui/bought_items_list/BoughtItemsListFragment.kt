package it.polito.mad.splintersell.ui.bought_items_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.MainActivity

import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import kotlinx.android.synthetic.main.fragment_bought_items_list.*


class BoughtItemsListFragment : Fragment() {

    private val firestoreViewModel: FirestoreViewModel by viewModels()
    private lateinit var externalLayout: ViewGroup
    private var list = arrayListOf<ItemModel>()
    private lateinit var adapter: BoughtItemsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                findNavController().navigate(R.id.nav_on_sale_list)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firestoreViewModel.fetchSoldItemListFromFirestore()

        return inflater.inflate(R.layout.fragment_bought_items_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        externalLayout = view.findViewById(R.id.external_layout)
        (activity as MainActivity?)?.refreshDataForDrawer()

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
            adapter.setSoldList(soldList as ArrayList<ItemModel>)
            adapter.notifyDataSetChanged()
            toggleNoItemsHere(soldList)

        })

    }
}
