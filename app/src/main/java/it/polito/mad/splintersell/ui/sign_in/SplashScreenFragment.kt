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
import androidx.navigation.fragment.FragmentNavigatorExtras
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
import kotlinx.android.synthetic.main.fragment_sign_in.*

class SplashScreenFragment : Fragment() {
    private val firestoreViewModel: FirestoreViewModel by activityViewModels()
    private val viewModel: SignInViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private val TAG = "SPLASH_SCREEN_FRAGMENT"

    lateinit var logo: ImageView
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        auth = Firebase.auth
        val account = auth.currentUser
        checkUser(account)
    }

    private fun checkUser(account: FirebaseUser?) {
        if (account != null) {
            logo = requireView().findViewById(R.id.logo_photo)
            bottomText = requireView().findViewById(R.id.breadsticksText)
            firestoreViewModel.getCurrentUser()
            firestoreViewModel.createdUserLiveData!!.observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "User logged in")
                viewModel.authenticate()
                Thread.sleep(300)
                val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)

                logo.startAnimation(fadeOut)
                app_name.startAnimation(fadeOut)
                bottomText.startAnimation(fadeOut)
                showSystemUI(activity as MainActivity)
                findNavController().popBackStack()
            })
        } else {
            Log.d(TAG, "User not logged in")
            //Thread.sleep(5000)
            val extras = FragmentNavigatorExtras(
                logo_photo to "logo_image",
                app_name to "logo_text"
            )
            val action = SplashScreenFragmentDirections.toSignIn()
            findNavController().navigate(action, extras)
        }
    }
}
