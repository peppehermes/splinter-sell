package it.polito.mad.splintersell.ui.item_details

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_item_details.*
import kotlinx.android.synthetic.main.fragment_item_details.category
import kotlinx.android.synthetic.main.fragment_item_details.expire_date
import kotlinx.android.synthetic.main.fragment_item_details.location
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

class ItemDetailsFragment: Fragment() {

    private val args: ItemDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    // Inflate the edit menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var index: Int?

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        args.apply {
            index = this.itemId

            val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return

            if(sharedPref.contains(index.toString())) {

                val info: String? = sharedPref.getString(index.toString(), null)

                val jasonObject = JSONObject(info!!)

                val showTitle = if (jasonObject.has("Title"))
                    jasonObject.getString("Title")
                else
                    getString(R.string.title)

                val showDescription = if (jasonObject.has("Description"))
                    jasonObject.getString("Description")
                else
                    getString(R.string.description)

                val showPrice = if (jasonObject.has("Price"))
                    jasonObject.getString("Price")
                else
                    getString(R.string.price)

                val showCategory = if (jasonObject.has("Category"))
                    jasonObject.getString("Category")
                else
                    getString(R.string.category)

                val showLocation = if (jasonObject.has("Location"))
                    jasonObject.getString("Location")
                else
                    getString(R.string.location)

                val showDate = if (jasonObject.has("Expire_Date"))
                    jasonObject.getString("Expire_Date")
                else
                    getString(R.string.expire_date)

                val filename = index.toString()

                val bitmap = retrieveImage(filename)

                if (bitmap != null)
                    detail_image.setImageBitmap(bitmap)

                title.text = showTitle
                description.text = showDescription
                price.text = showPrice
                category.text = showCategory
                location.text = showLocation
                expire_date.text = showDate
            }
        }
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
                val action = ItemDetailsFragmentDirections.editItem(args.itemId)
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


