package it.polito.mad.splintersell.ui.sign_in

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import kotlinx.android.synthetic.main.fragment_sign_in.*

const val RC_SIGN_IN = 2013

class SignIn : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val firestoreViewModel: FirestoreViewModel by activityViewModels()
    private val viewModel: SignInViewModel by activityViewModels()
    private val TAG = "SignInTAG"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.refuseAuthentication()
            findNavController().popBackStack(R.id.nav_on_sale_list, false)
        }

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            if (authenticationState == SignInViewModel.AuthenticationState.INVALID_AUTHENTICATION)
                showErrorMessage()
        })

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        val mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        sign_in_button.setOnClickListener {
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(
                signInIntent,
                RC_SIGN_IN
            )
        }

        auth = Firebase.auth
    }

    private fun showErrorMessage() {
        Snackbar.make(requireView(), "Error during authentication", Snackbar.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                Log.w(TAG, "All right")
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                viewModel.invalidAuthentication()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        signInWithGoogleAuthCredential(credential)
    }

    private fun signInWithGoogleAuthCredential(googleAuthCredential: AuthCredential) {
        firestoreViewModel.signInWithGoogle(googleAuthCredential)
        firestoreViewModel.authenticatedUserLiveData!!.observe(
            viewLifecycleOwner,
            Observer { authenticatedUser ->
                createNewUser(authenticatedUser)
            })
    }

    private fun createNewUser(authenticateUser: UserModel) {
        firestoreViewModel.createUser(authenticateUser)
        firestoreViewModel.createdUserLiveData!!.observe(this, Observer {
            viewModel.authenticate()
            findNavController().navigate(SignInDirections.insertInformation())
        })
    }
}

