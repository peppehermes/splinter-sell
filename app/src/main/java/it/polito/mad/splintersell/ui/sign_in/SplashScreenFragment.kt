package it.polito.mad.splintersell.ui.sign_in

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.MainActivity
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.ui.hideSystemUI
import it.polito.mad.splintersell.ui.showSystemUI

class SplashScreenFragment : Fragment() {
    private val firestoreViewModel: FirestoreViewModel by activityViewModels()
    private val viewModel: SignInViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private val TAG = "SPLASH_SCREEN_FRAGMENT"

    lateinit var logo: ImageView
    lateinit var appName: TextView
    lateinit var bottomText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Hide the status bar.
        hideSystemUI(activity as MainActivity)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logo = view.findViewById(R.id.logo_photo)
        appName = view.findViewById(R.id.app_name)
        bottomText = view.findViewById(R.id.breadsticksText)

        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)

        logo.startAnimation(fadeIn)
        appName.startAnimation(fadeIn)
        bottomText.startAnimation(fadeIn)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        auth = Firebase.auth
        val account = auth.currentUser
        checkUser(account)
    }

    private fun checkUser(account: FirebaseUser?) {
        if (account != null) {
            firestoreViewModel.getCurrentUser()
            firestoreViewModel.createdUserLiveData!!.observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "User logged in")
                viewModel.authenticate()
                val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)

                logo.startAnimation(fadeOut)
                appName.startAnimation(fadeOut)
                bottomText.startAnimation(fadeOut)
                showSystemUI(activity as MainActivity)
                findNavController().popBackStack()
            })
        } else {
            Log.d(TAG, "User not logged in")
            findNavController().navigate(R.id.nav_sign_in)
        }
    }
}
