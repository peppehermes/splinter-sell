package it.polito.mad.splintersell.ui.feedback_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FeedbackModel
import it.polito.mad.splintersell.data.FirestoreViewModel
import kotlinx.android.synthetic.main.fragment_feedback_list.*


class FeedbackListFragment : Fragment() {

    private val firestoreViewModel: FirestoreViewModel by activityViewModels()

    private lateinit var adapter: FeedbackListAdapter
    private lateinit var listView: RecyclerView
    private var list = arrayListOf<FeedbackModel>()
    private lateinit var externalLayout: ViewGroup
    private val args: FeedbackListFragmentArgs by navArgs()
    private val user = Firebase.auth.currentUser

    private val TAG = "FEEDBACK_LIST_FRAGMENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (args.userID == "currUser")
            firestoreViewModel.getFeedbackData(user!!.uid)
        else
            firestoreViewModel.getFeedbackData(args.userID!!)
        return inflater.inflate(R.layout.fragment_feedback_list, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.user_list_menu, menu)

        val searchFeed = menu.findItem(R.id.action_search)
        val searchView = searchFeed.actionView as SearchView

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

        externalLayout = view.findViewById(R.id.external_layout)
        listView = view.findViewById(R.id.feedback_list)
        adapter = FeedbackListAdapter(list)

        listView.layoutManager = LinearLayoutManager(context)
        listView.setHasFixedSize(true)
        listView.adapter = adapter
        listView.itemAnimator = DefaultItemAnimator()

        firestoreViewModel.myFeedbackList.observe(
                viewLifecycleOwner,
                Observer { FeedbackList ->
                    Log.e(TAG, "UPDATE")
                    adapter.setFeedbackList(FeedbackList as ArrayList<FeedbackModel>)
                    adapter.notifyDataSetChanged()
                    Log.e(TAG, FeedbackList.isEmpty().toString())
                    toggleNoFeedsHere(FeedbackList)
                })

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun toggleNoFeedsHere(list: List<FeedbackModel>) {
        TransitionManager.beginDelayedTransition(externalLayout)
        if (list.isEmpty()) {
            empty_list_feedbacks.visibility = View.VISIBLE
        } else  empty_list_feedbacks.visibility = View.GONE
    }
}