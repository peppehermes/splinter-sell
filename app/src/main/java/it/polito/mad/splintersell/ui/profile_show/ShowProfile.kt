package it.polito.mad.splintersell.ui.profile_show

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_show_profile.*


class ShowProfile : Fragment() {

    // GLOBAL ATTRIBUTES

    private val firestoreViewModel: FirestoreViewModel by viewModels()
    val user = Firebase.auth.currentUser
    lateinit var liveData: LiveData<UserModel>

    private val args: ShowProfileArgs by navArgs()

    // METHODS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        liveData = if(args.userID == "currUser"){
            firestoreViewModel.fetchUserFromFirestore(user!!.uid)
            firestoreViewModel.myUser
        } else{
            firestoreViewModel.fetchUserFromFirestore(args.userID)
            firestoreViewModel.myUser
        }



        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                findNavController().navigate(R.id.nav_item_list)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        liveData.observe(viewLifecycleOwner, Observer {
            // Update UI
            name.text = it.fullname
            nickname.text = it.nickname
            email.text = it.email
            location.text = it.location

            Glide.with(requireContext())
                .using(FirebaseImageLoader())
                .load(storage.child("/profileImages/${it.photoName}"))
                .into(profile_photo)
        })
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (args.userID == "currUser")
            return inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit -> {
                Navigation.findNavController(requireView()).navigate(R.id.edit)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

