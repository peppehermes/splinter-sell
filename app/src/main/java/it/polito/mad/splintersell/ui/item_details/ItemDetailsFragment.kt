package it.polito.mad.splintersell.ui.item_details

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
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

        args.apply {
            index = this.itemId

            val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return

            if(sharedPref.contains(index.toString())) {

                val info: String? = sharedPref.getString(index.toString(), null)

                val jasonObject = JSONObject(info!!)
                val showTitle: String
                val showDescription: String
                val showPrice: String
                val showCategory: String
                val showLocation: String
                val showDate: String

                showTitle = if (jasonObject.has("Title"))
                    jasonObject.getString("Title")
                else
                    getString(R.string.title)

                showDescription = if (jasonObject.has("Description"))
                    jasonObject.getString("Description")
                else
                    getString(R.string.description)

                showPrice = if (jasonObject.has("Price"))
                    jasonObject.getString("Price")
                else
                    getString(R.string.price)

                showCategory = if (jasonObject.has("Category"))
                    jasonObject.getString("Category")
                else
                    getString(R.string.category)

                showLocation = if (jasonObject.has("Location"))
                    jasonObject.getString("Location")
                else
                    getString(R.string.location)

                showDate = if (jasonObject.has("Expire_Date"))
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
}


