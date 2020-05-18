package it.polito.mad.splintersell.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import it.polito.mad.splintersell.R

import android.content.Intent
import android.util.Log
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.splintersell.MainActivity
import it.polito.mad.splintersell.data.UserModel
import kotlinx.android.synthetic.main.sign_in_fragment.*

const val RC_SIGN_IN = 2013


class SignIn : Fragment() {

    private lateinit var auth:FirebaseAuth


    companion object {
        fun newInstance() = SignIn()
    }

    private lateinit var viewModel: SignInViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sign_in_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val navigationView:DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        navigationView.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        val mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)


        sign_in_button.setOnClickListener {
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        val account = auth.currentUser
        checkUser(account)
    }


    private fun checkUser(account: FirebaseUser?){

        if(account != null){

            (activity as MainActivity?)?.refreshDataForDrawer()

            val navigationView:DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
            navigationView.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            Navigation.findNavController(requireView()).navigate(R.id.onSaleListFragment)

        }
        else Log.d("SignInTAG", "User not logged in")


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("SignInTAG", "firebaseAuthWithGoogle:" + account.id + " " + account.idToken + " " + account.email)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("SignInTAG", "Google sign in failed", e)
                // [START_EXCLUDE]
                checkUser(null)
                // [END_EXCLUDE]
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SignInTAG", "signInWithCredential:success")

                    val user = auth.currentUser
                    checkDBCredentials()
                    checkUser(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SignInTAG", "signInWithCredential:failure", task.exception)
                    checkUser(null)
                }


            }
    }


    private fun checkDBCredentials(){

        val db = FirebaseFirestore.getInstance()
        val user = Firebase.auth.currentUser
        val docRef = db.collection("users")
            .document(user!!.uid)

        Log.d("SignInTAG", user.toString())
        Log.d("SignInTAG", user.uid)


            docRef.get()
            .addOnSuccessListener {

                res ->
                if(!res.exists()){

                        val newUser = UserModel(
                            user.displayName!!,
                            "",
                            user.email!!,
                            "",
                            "img_avatar.jpg"
                        )

                        db.collection("users")
                            .document(user.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                Log.d("SignInTAG", "Instance succesfully created!")
                            }
                            .addOnFailureListener{
                                Log.d("SignInTAG", "Error in creating new instance")
                            }

                }
                else {
                    Log.d("SignInTAG", res.toString())
                    Log.d("SignInTAG", "Instance already created!")
                }

            }
            .addOnFailureListener{
                Log.d("SignInTAG", "Error in reading the DB")
            }


    }



}

