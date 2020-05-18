package it.polito.mad.splintersell.ui.profile_show

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.MainActivity
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.User
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_show_profile.*

const val EXTRA_NAME = "it.polito.mad.splintersell.NAME"
const val EXTRA_NICKNAME = "it.polito.mad.splintersell.NICKNAME"
const val EXTRA_EMAIL = "it.polito.mad.splintersell.EMAIL"
const val EXTRA_LOCATION = "it.polito.mad.splintersell.LOCATION"
const val filename = "proPic"

val db = FirebaseFirestore.getInstance()
val user = Firebase.auth.currentUser

class ShowProfile : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        this.retrieveData()

        (activity as MainActivity?)?.refreshDataForDrawer()
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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


    //TODO Fix retrieving image of User from Storage (ShowProfile)
    private fun retrieveImage(photoName: String) {

        Log.d("profileImage","found this image : $photoName")

        Glide.with(requireContext())
            .using(FirebaseImageLoader())
            .load(storage.child("/profileImages/$photoName"))
            //.diskCacheStrategy(DiskCacheStrategy.NONE)
            //.skipMemoryCache(true)
            .into(profile_photo)

    }


    private fun retrieveData(){

        db.collection("users")
            .document(user!!.uid)
            .get()
            .addOnSuccessListener {

                res ->
                if(res.exists()){
                    val userData: User? = res.toObject(
                        User::class.java)
                    Log.d("ShowProfileTAG", "Success in retrieving data: "+ res.toString())

                    Log.d("ShowProfileTAG", userData.toString())
                    Log.d("profileImage","image retrieved ${userData!!.photoName}")

                    if(userData?.fullname != "")
                        name.text = userData!!.fullname
                    if(userData.nickname != "")
                        nickname.text = userData.nickname
                    if(userData.email != "")
                        email.text = userData.email
                    if(userData.location != "")
                        location.text = userData.location

                        retrieveImage(userData.photoName)

                }
                else
                    Log.d("ShowProfileTAG", "No document retrieved")


            }
            .addOnFailureListener{
                Log.d("ShowProfileTAG", "Error in retrieving data")
            }

    }



}

