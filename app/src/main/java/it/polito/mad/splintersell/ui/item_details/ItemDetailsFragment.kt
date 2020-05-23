package it.polito.mad.splintersell.ui.item_details

import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.*
import kotlinx.android.synthetic.main.fragment_item_details.*

class ItemDetailsFragment : Fragment() {
    private val firestoreViewModel: FirestoreViewModel by viewModels()
    private lateinit var liveData: LiveData<ItemModel>
    private lateinit var userLiveData: LiveData<UserModel>
    private var user = FirebaseAuth.getInstance().currentUser
    private lateinit var coordinator: CoordinatorLayout
    private var isRotate: Boolean = false
    private lateinit var imgPath: String

    private val args: ItemDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    // Inflate the edit show_profile_menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!args.onSale) inflater.inflate(R.menu.detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        firestoreViewModel.fetchSingleItemFromFirestore(args.documentName)
        liveData = firestoreViewModel.item

        if (args.userID != "currUser") {
            firestoreViewModel.fetchUserFromFirestore(args.userID)
            userLiveData = firestoreViewModel.myUser
        }

        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coordinator = view.findViewById(R.id.coordinator_layout)

        firestoreViewModel.firestoreRepository.getItemNotification(args.documentName)
        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        if (args.userID == "currUser") {
            owner_label.visibility = View.GONE
            owner.visibility = View.GONE
        } else {

            owner.setTextColor(owner.context.getColor(R.color.colorPrimary))

            owner.setOnClickListener {
                val action = ItemDetailsFragmentDirections.showProfile(args.userID)
                findNavController().navigate(action)

            }

            userLiveData.observe(viewLifecycleOwner, Observer {
                owner.text = it.nickname
            })
        }

        if (args.onSale) {
            firestoreViewModel.isRequested.observe(viewLifecycleOwner, Observer { requested ->
                isRotate = requested

                if (requested) {
                    rotateFab(fab, isRotate)
                    fab.backgroundTintList =
                        fab.context.getColorStateList(R.color.colorPrimaryLight)
                } else {
                    fab.backgroundTintList = fab.context.getColorStateList(R.color.colorSecondary)
                }

                fab.setOnClickListener { floatingButton ->
                    toggleItem(isRotate, floatingButton, requested)
                }

                fab.show()

            })
        }

        liveData.observe(viewLifecycleOwner, Observer {
            // Update UI
            title.text = it.title
            description.text = it.description
            val priceText = "${it.price} $"
            price.text = priceText
            val cat = "${it.mainCategory} : ${it.secondCategory}"
            category.text = cat
            location.text = it.location
            expire_date.text = it.expireDate
            imgPath = it.imgPath

            Glide.with(requireContext()).using(FirebaseImageLoader())
                .load(storage.child("/itemImages/${it.imgPath}")).into(detail_image)
        })

        detail_image.setOnClickListener {
            val action = ItemDetailsFragmentDirections.fullScreenImage(imgPath)
            findNavController().navigate(action)
        }

    }

    private fun toggleItem(isRotated: Boolean, fab: View, isRequested: Boolean) {
        isRotate = rotateFab(fab, !isRotated)

        if (isRequested) {
            firestoreViewModel.firestoreRepository.removeNotification(args.documentName)
            FirebaseMessaging.getInstance().unsubscribeFromTopic(args.documentName)
            Snackbar.make(
                coordinator, "Item removed from Wishlist", Snackbar.LENGTH_SHORT
            ).show()
            firestoreViewModel.isRequested.value = false
        } else {
            val newNot = NotificationModel(
                args.documentName, user!!.uid, liveData.value!!.ownerId!!
            )
            firestoreViewModel.saveNotificationToFirestore(newNot)
            FirebaseMessaging.getInstance().subscribeToTopic(args.documentName)
            Snackbar.make(
                coordinator, "Item added to Wishlist", Snackbar.LENGTH_SHORT
            ).show()
            firestoreViewModel.isRequested.value = true
        }
    }

    private fun rotateFab(v: View, rotate: Boolean): Boolean {
        v.animate().setDuration(200).setListener(object : AnimatorListenerAdapter() {})
            .rotation(if (rotate) 135f else 0f)
        return rotate
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit -> {
                val action = ItemDetailsFragmentDirections.editItem(args.documentName)
                findNavController().navigate(action)
                true
            }
            R.id.show -> {
                val action = ItemDetailsFragmentDirections.goToInterestedUsers(args.documentName)
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}


