package it.polito.mad.splintersell.ui.on_sale_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.MainActivity
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.android.synthetic.main.fragment_item_list.empty_list
import kotlinx.android.synthetic.main.fragment_wish_list.*


class OnSaleListFragment : Fragment() {
    private val firestoreViewModel: FirestoreViewModel by viewModels()

    private lateinit var adapter: OnSaleListAdapter
    private lateinit var listView: RecyclerView
    private var list = arrayListOf<ItemModel>()

    private val TAG = "ON_SALE_LIST_FRAGMENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firestoreViewModel.fetchAllItemListFromFirestore()
        return inflater.inflate(R.layout.fragment_on_sale_list, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Close the soft Keyboard, if open
                hideKeyboardFrom(requireContext(), view!!)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.on_sale_list)
        adapter = OnSaleListAdapter(list)

        listView.layoutManager = LinearLayoutManager(context)
        listView.setHasFixedSize(true)
        listView.adapter = adapter
        listView.itemAnimator = DefaultItemAnimator()

        // Check if new items have been added
        firestoreViewModel.allItemList.observe(viewLifecycleOwner, Observer {

            // Update UI
            firestoreViewModel.firestoreRepository.getItemData()
            firestoreViewModel.onSaleItemList.observe(viewLifecycleOwner, Observer {onSaleItemList ->
                Log.e(TAG, "UPDATE")
                adapter.setOnSaleItemList(onSaleItemList as ArrayList<ItemModel>)
                adapter.notifyDataSetChanged()
                //Log.e(TAG, onSaleItemList.isEmpty().toString())
                hideNoItemsHere(onSaleItemList)
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
            empty_list.visibility = View.VISIBLE
        else
            empty_list.visibility = View.GONE
    }
}
