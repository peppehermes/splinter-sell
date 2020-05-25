package it.polito.mad.splintersell.ui.signin

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_information.*

class InformationFragment : Fragment() {
    private val firestoreViewModel: FirestoreViewModel by viewModels()
    private val TAG = "INFORMATION_FRAGMENT"
    private var user: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestoreViewModel.fetchMyUserFromFirestore()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user information
        firestoreViewModel.myUserNav.observe(viewLifecycleOwner, Observer {
            user = it
            if (user == null) {
                Log.e(TAG, "Error retrieving current user")
            }

            // Check if the user already had an account
            if (user!!.photoName != "img_avatar.jpg") {
                // Load the photo of the user
                Glide.with(requireContext()).using(FirebaseImageLoader())
                    .load(storage.child("/profileImages/${user!!.photoName}")).into(profile_photo)
            } else {
                TransitionManager.beginDelayedTransition(coordinator_layout, AutoTransition())
                profile_photo.setImageResource(R.drawable.img_avatar)
            }

            if (!user!!.nickname.isNullOrEmpty()) {
                // Set the previous nickname of the user
                til_login_nick.editText!!.setText(user!!.nickname)
            }
        })

        button_send.setOnClickListener {
            val nick = til_login_nick.editText!!.text.toString()

            // Check if the nickname was set
            if (login_nick.text.isNullOrEmpty()) {
                til_login_nick.error = requireContext().getString(R.string.please_fill)
                return@setOnClickListener
            }

            // If the nickname was not modified, we don't need to check for uniqueness of nickname
            if (til_login_nick.editText!!.text.toString() != user!!.nickname) {
                // Check if the nickname is unique
                firestoreViewModel.firestoreRepository.firestore.collection("users")
                    .whereEqualTo("nickname", nick)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            til_login_nick.error =
                                requireContext().getString(R.string.nick_not_available)
                            return@addOnSuccessListener
                        } else {
                            // Set user nickname
                            user!!.nickname = nick

                            // Save user into Firestore Cloud
                            firestoreViewModel.saveUserToFirestore(user!!)

                            // Navigate to home fragment
                            findNavController().navigate(InformationFragmentDirections.goToHome())
                        }
                    }
            } else {
                // Save user into Firestore Cloud
                firestoreViewModel.saveUserToFirestore(user!!)

                // Navigate to home fragment
                findNavController().navigate(InformationFragmentDirections.goToHome())
            }
        }
    }

}
