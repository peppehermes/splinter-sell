package it.polito.mad.splintersell.ui.item_details

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_item_details.category
import kotlinx.android.synthetic.main.fragment_item_details.description
import kotlinx.android.synthetic.main.fragment_item_details.expire_date
import kotlinx.android.synthetic.main.fragment_item_details.location
import kotlinx.android.synthetic.main.fragment_item_details.price
import kotlinx.android.synthetic.main.fragment_item_details.title
import java.io.File
import java.io.FileInputStream

class ItemDetailsFragment: Fragment() {
    val user = Firebase.auth.currentUser
    private val firestoreViewModel: FirestoreViewModel by viewModels()
    //private val user = firestoreViewModel.getUser()
    lateinit var liveData: LiveData<ItemModel>

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
        liveData = firestoreViewModel.getItemFromFirestore(args.documentName)

        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        liveData.observe(viewLifecycleOwner, Observer {
            // Update UI
            title.text = it.title
            description.text = it.description
            price.text = it.price
            val cat = "${it.mainCategory} : ${it.secondCategory}"
            category.text = cat
            location.text = it.location
            expire_date.text = it.expireDate
        })

    }

    private fun retrieveImage(filename: String) : Bitmap? {
        val file = File(activity?.filesDir, filename)
        val fileExists = file.exists()

        return if (fileExists) {
            val fis: FileInputStream = requireActivity().openFileInput(filename)
            val bitmap = BitmapFactory.decodeStream(fis)
            fis.close()
            bitmap
        } else
            null
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


