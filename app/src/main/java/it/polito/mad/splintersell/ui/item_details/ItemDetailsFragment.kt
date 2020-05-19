package it.polito.mad.splintersell.ui.item_details

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.NotificationModel
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_item_details.*

class ItemDetailsFragment: Fragment() {
    private val firestoreViewModel: FirestoreViewModel by viewModels()
    lateinit var liveData: LiveData<ItemModel>
    var user = FirebaseAuth.getInstance().currentUser
    val action1 = ItemDetailsFragmentDirections.goToListItem()

    private val args: ItemDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    // Inflate the edit menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!args.onSale)
            inflater.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestoreViewModel.fetchSingleItemFromFirestore(args.documentName)
        liveData = firestoreViewModel.item

        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestoreViewModel.getNotifications(args.documentName)
        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        if(args.onSale) {
            firestoreViewModel.isrequested.observe(viewLifecycleOwner, Observer { requested ->
                if (requested == true) {
                    fab.setImageResource(R.drawable.ic_strikethrough_s_black_24dp)
                    fab.setBackgroundTintList(resources.getColorStateList(R.color.colorPrimaryLight))
                    fab.setOnClickListener {
                       firestoreViewModel.cancelNotifications(args.documentName)
                        Navigation.findNavController(requireView()).navigate(action1)
                    }
                }
                else{
                    fab.setOnClickListener {
                        val newNot = NotificationModel(
                            args.documentName,
                            user!!.uid,
                            liveData.value!!.ownerId
                        )
                        firestoreViewModel.saveNotificationToFirestore(newNot)
                        Navigation.findNavController(requireView()).navigate(action1)
                    }
                }
                fab.show()


            })
        }
        liveData.observe(viewLifecycleOwner, Observer {
            // Update UI
            title.text = it.title
            description.text = it.description
            price.text = it.price
            val cat = "${it.mainCategory} : ${it.secondCategory}"
            category.text = cat
            location.text = it.location
            expire_date.text = it.expireDate

            Glide.with(requireContext())
                .using(FirebaseImageLoader())
                .load(storage.child("/itemImages/${it.imgPath}"))
                .into(detail_image)
        })

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit -> {
                val action = ItemDetailsFragmentDirections.editItem(args.documentName)
                Navigation.findNavController(requireView()).navigate(action)
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


