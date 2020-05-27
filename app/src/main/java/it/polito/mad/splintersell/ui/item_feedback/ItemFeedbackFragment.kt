package it.polito.mad.splintersell.ui.item_feedback

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FeedbackModel
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_rating.*


class ItemFeedbackFragment : Fragment() {
    private val firestoreViewModel: FirestoreViewModel by viewModels()
    private lateinit var userLiveData: LiveData<UserModel>
    private var user = FirebaseAuth.getInstance().currentUser


    private val args: ItemFeedbackFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        firestoreViewModel.fetchUserFromFirestore(args.ownerid)
        userLiveData = firestoreViewModel.myUser

        return inflater.inflate(R.layout.fragment_rating, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideKeyboardFrom(requireContext(), view)

        this.setInputLimits()

        userLiveData.observe(viewLifecycleOwner, Observer {
            nickname.text = it.nickname

            Glide.with(requireContext()).using(FirebaseImageLoader())
                .load(storage.child("/profileImages/${it.photoName}")).into(profile_photo)
        })

        itemname.text = args.itemtitle

        fab.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Do you want to leave this feedback?").setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    // Insert feedback into DB
                    val newFeed = FeedbackModel(
                        args.itemid,
                        user!!.uid,
                        args.ownerid,
                        rating.rating,
                        comment.text.toString()
                    )
                    firestoreViewModel.saveFeedbackToFirestore(newFeed)
                    firestoreViewModel.updateRating(args.ownerid, rating.rating)
                    firestoreViewModel.updateStatusFeed(args.itemid)
                    dialog.dismiss()

                    findNavController().popBackStack()
                }.setNegativeButton("No") { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }

            val alert = builder.create()
            alert.show()
        }
    }

    private fun setInputLimits() {
        til_comment.editText!!.filters = arrayOf(InputFilter.LengthFilter(80))
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}



