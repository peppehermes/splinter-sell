package it.polito.mad.splintersell.ui.sign_in

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import it.polito.mad.splintersell.data.storage
import it.polito.mad.splintersell.ui.profile_edit.EditProfileViewModel
import kotlinx.android.synthetic.main.fragment_information.*

class InformationFragment : Fragment() {
    private val firestoreViewModel: FirestoreViewModel by activityViewModels()
    private val userModel: EditProfileViewModel by activityViewModels()
    private val TAG = "INFORMATION_FRAGMENT"
    private var user: UserModel? = null

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.nav_sign_in, false)
        }

        // Get user information
        firestoreViewModel.myUser.observe(viewLifecycleOwner, Observer {
            user = it
            if (user == null) {
                Log.e(TAG, "Error retrieving current user")
            }

            // Check if the user already had an account
            if (user!!.photoName != "img_avatar.jpg") {
                // Load the photo of the user
                Glide.with(requireContext()).using(FirebaseImageLoader())
                    .load(storage.child("/profileImages/${user!!.photoName}")).into(profile_photo)
                userModel.user.value?.photoName = user!!.photoName
            } else {
                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                profile_photo.setImageResource(R.drawable.img_avatar)
                profile_photo.startAnimation(fadeIn)
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
                            firestoreViewModel.createdUserLiveData!!.value!!.nickname = nick
                            firestoreViewModel.createdUserLiveData!!.value!!.photoName =
                                user!!.photoName

                            // Go to location fragment
                            val action = InformationFragmentDirections.goAskForLocation()
                            findNavController().navigate(action)
                        }
                    }
            } else {
                firestoreViewModel.createdUserLiveData!!.value!!.nickname = nick
                firestoreViewModel.createdUserLiveData!!.value!!.photoName = user!!.photoName

                // Go to location fragment
                val action = InformationFragmentDirections.goAskForLocation()
                findNavController().navigate(action)
            }
        }
    }
}
