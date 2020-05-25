package it.polito.mad.splintersell.ui.on_sale_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import it.polito.mad.splintersell.MainActivity
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.ui.signin.SignInViewModel
import kotlinx.android.synthetic.main.fragment_on_sale_list.*


class OnSaleListFragment : Fragment() {
    private val firestoreViewModel: FirestoreViewModel by viewModels()
    private val signInViewModel: SignInViewModel by activityViewModels()

    private lateinit var adapter: OnSaleListAdapter
    private lateinit var listView: RecyclerView
    private var list = arrayListOf<ItemModel>()
    private lateinit var filterLayout: View
    private lateinit var externalLayout: ViewGroup

    private val TAG = "ON_SALE_LIST_FRAGMENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        firestoreViewModel.fetchAvailableItemListFromFirestore()

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
        val layout = inflater.inflate(R.layout.fragment_on_sale_list, container, false)
        filterLayout = layout.findViewById(R.id.filter_layout)
        return layout
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.on_sale_item_list_menu, menu)

        // Set the textual search
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var visible: Boolean = filterLayout.visibility == View.VISIBLE

        return when (item.itemId) {
            R.id.filter -> {
                TransitionManager.beginDelayedTransition(externalLayout)
                visible = !visible

                filterLayout.visibility = if (visible) View.VISIBLE else View.GONE

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity?)?.refreshDataForDrawer()

        (activity as AppCompatActivity).supportActionBar?.show()

        generateToken()

        externalLayout = view.findViewById(R.id.external_layout)

        manageSpinner()

        listView = view.findViewById(R.id.on_sale_list)
        adapter = OnSaleListAdapter(list)

        listView.layoutManager = LinearLayoutManager(context)
        listView.setHasFixedSize(true)
        listView.adapter = adapter
        listView.itemAnimator = DefaultItemAnimator()

        // Check if new items have been added
        updateUI()

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        // Set click listener for FILTER button
        val filterButton = view.findViewById<Button>(R.id.filter_button)
        filterButton.setOnClickListener { button ->
            filterAction(button)
        }

        val resetButton = view.findViewById<Button>(R.id.reset_button)
        resetButton.setOnClickListener { button ->
            resetAction(button)
        }

    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun toggleNoItemsHere(list: List<ItemModel>) {
        TransitionManager.beginDelayedTransition(externalLayout)
        if (list.isEmpty()) empty_list.visibility = View.VISIBLE
        else empty_list.visibility = View.GONE
    }

    private fun updateUI() {
        firestoreViewModel.availableItemList.observe(viewLifecycleOwner, Observer {

            // Update UI
            firestoreViewModel.firestoreRepository.getItemData()
            firestoreViewModel.onSaleItemList.observe(
                viewLifecycleOwner,
                Observer { onSaleItemList ->
                    Log.e(TAG, "UPDATE")
                    adapter.setOnSaleItemList(onSaleItemList as ArrayList<ItemModel>)
                    adapter.notifyDataSetChanged()
                    //Log.e(TAG, onSaleItemList.isEmpty().toString())
                    toggleNoItemsHere(onSaleItemList)
                })

        })
    }

    private fun resetAction(view: View) {
        updateUI()

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        // Close the filter drawer
        TransitionManager.beginDelayedTransition(externalLayout)
        filterLayout.visibility = View.GONE
    }

    private fun filterAction(view: View) {
        // Take the category to filter for
        val mainCategory = dropdown_main_category.text.toString()
        val secondCategory = dropdown_sub_category.text.toString()

        val minimumPrice: String = price_min.text.toString()
        val maximumPrice: String = price_max.text.toString()

        val minPrice = if (minimumPrice == "") 0
        else minimumPrice.toInt()

        val maxPrice = if (maximumPrice == "") Int.MAX_VALUE
        else maximumPrice.toInt()

        if (mainCategory.isEmpty()) {
            // Filter only by price tag
            firestoreViewModel.firestoreRepository.getItemData(minPrice, maxPrice)
            Snackbar.make(view, "List updated", Snackbar.LENGTH_SHORT).show()
        } else {
            if (secondCategory.isEmpty()) {
                // Send toast asking to choose the second category
                spinner_2.requestFocus()
                Snackbar.make(view, "Please, specify a Sub Category", Snackbar.LENGTH_LONG)
                    .setAction("OKAY") { // Nothing to do here
                    }.show()
            } else {
                firestoreViewModel.firestoreRepository.getItemData(
                    minPrice, maxPrice, mainCategory, secondCategory
                )
                Snackbar.make(view, "List updated", Snackbar.LENGTH_SHORT).show()
            }
        }

        val filteredList = firestoreViewModel.onSaleItemList.value as ArrayList<ItemModel>

        adapter.setOnSaleItemList(filteredList)
        adapter.notifyDataSetChanged()
        toggleNoItemsHere(filteredList)
        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        // Close the filter drawer
        TransitionManager.beginDelayedTransition(externalLayout)
        filterLayout.visibility = View.GONE

    }

    private fun manageSpinner() {
        val adapter = ArrayAdapter(
            this.requireContext(),
            R.layout.spinner_text,
            resources.getStringArray(R.array.macroCategories)
        )

        dropdown_main_category!!.setAdapter(adapter)
        dropdown_main_category.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                when (position) {

                    0 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.arts)
                        )
                    )
                    1 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.sports)
                        )
                    )
                    2 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.baby)
                        )
                    )
                    3 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.women)
                        )
                    )
                    4 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.men)
                        )
                    )
                    5 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.electronics)
                        )
                    )
                    6 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.games)
                        )
                    )
                    7 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.automotive)
                        )
                    )

                }
            }
    }

    fun generateToken() {

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("IstanceId", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token
                FirestoreViewModel().updateToken(token.toString())
            })
    }
}
