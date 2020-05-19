package it.polito.mad.splintersell.ui.user_list

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import kotlinx.android.synthetic.main.fragment_user_list.*


class UserListFragment : Fragment() {

        private val firestoreViewModel: FirestoreViewModel by viewModels()

        private lateinit var adapter: UserListAdapter
        private lateinit var listView: RecyclerView
        private var list = arrayListOf<UserModel>()

        private val args: UserListFragmentArgs by navArgs()

        private val TAG = "USER_LIST_FRAGMENT"

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setHasOptionsMenu(true)
        }

        override fun onCreateView(
                inflater: LayoutInflater, container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View? {
        // Inflate the layout for this fragment
                firestoreViewModel.fetchAllNotificationsFromFirestore()
                return inflater.inflate(R.layout.fragment_user_list, container, false)
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

        listView = view.findViewById(R.id.user_list)
        adapter = UserListAdapter(list,args.itemID)

                listView.layoutManager = LinearLayoutManager(context)
                listView.setHasFixedSize(true)
                listView.adapter = adapter
                listView.itemAnimator = DefaultItemAnimator()

                firestoreViewModel.myNotificationsList.observe(viewLifecycleOwner, Observer {
                        //Update UI
                        firestoreViewModel.firestoreRepository.getUsersData(args.itemID)
                        firestoreViewModel.interestedUserList.observe(viewLifecycleOwner, Observer {UsersList ->
                                Log.e(TAG, "UPDATE")
                                adapter.setUsersList(UsersList as ArrayList<UserModel>)
                                adapter.notifyDataSetChanged()
                                Log.e(TAG, UsersList.isEmpty().toString())
                                hideNoUsersHere(UsersList)
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

        private fun hideNoUsersHere(list: List<UserModel>) {
                if (list.isEmpty()) {
                        Log.d("pio","soio")
                        empty_list_users.visibility = View.VISIBLE
                }
                else
                        empty_list_users.visibility = View.GONE
        }
}
