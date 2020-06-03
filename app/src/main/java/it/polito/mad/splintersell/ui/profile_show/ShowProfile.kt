package it.polito.mad.splintersell.ui.profile_show

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

    private val firestoreViewModel: FirestoreViewModel by activityViewModels()
    val user = Firebase.auth.currentUser
    lateinit var liveData: LiveData<UserModel>
    private val TAG = "SHOW_PROFILE"

    private val args: ShowProfileArgs by navArgs()

    // METHODS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        liveData = if (args.userID == "currUser") {
            firestoreViewModel.fetchMyUserFromFirestore()
            firestoreViewModel.myUser
        } else {
            firestoreViewModel.fetchUserFromFirestore(args.userID)
            firestoreViewModel.user
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        liveData.observe(viewLifecycleOwner, Observer {
            // Update UI

            if (args.userID == "currUser") {
                name.text = it.fullname
                nickname.text = it.nickname
                email.text = it.email
                location.text = it.location
                rating.rating = it.rating
                Log.e(TAG, "${rating.rating} ${it.rating}")

                location.setOnClickListener{
                    val action = ShowProfileDirections.fromProfileToShowMap(true, "", true, "currUser")
                    findNavController().navigate(action)
                }


            } else {
                nickname.text = it.nickname
                email.text = it.email
                location.text = it.location
                rating.rating = it.rating
                Log.e(TAG, "${rating.rating} ${it.rating}")

                name.visibility = View.GONE
                label_name.visibility = View.GONE

                location.setOnClickListener{
                    val action = ShowProfileDirections.fromProfileToShowMap(true, "", true, args.userID)
                    findNavController().navigate(action)
                }

            }




            Glide.with(requireContext()).using(FirebaseImageLoader())
                .load(storage.child("/profileImages/${it.photoName}")).into(profile_photo)
        })
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (args.userID == "currUser") return inflater.inflate(R.menu.show_profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
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

