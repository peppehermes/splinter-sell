package it.polito.mad.splintersell.ui.item_list

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.splintersell.Item
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_item_list.*
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

class ItemListFragment : Fragment() {
    private lateinit var items: ArrayList<Item>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Close the soft Keyboard, if open
        hideKeyboardFrom(requireContext(), view)

        // Load the item list (database?) from the shared preferences
        items = loadSharedPreferences()

        // If the list is empty, show a message
        if (!items.isNullOrEmpty())
            empty_list.visibility = View.GONE

        fab.setOnClickListener {
            val action = ItemListFragmentDirections.newItem(items.size)
            it.findNavController().navigate(action)
        }

        // Pass the layout manager and the item list to the adapter
        item_list.layoutManager = LinearLayoutManager(context)
        item_list.adapter = ItemCardAdapter(items)
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loadSharedPreferences(): ArrayList<Item> {
        // Read data from SharedPreferences
        val items = arrayListOf<Item>()
        val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return items
        val profile: MutableMap<String,*>? = sharedPref.all

        if (profile != null) {
            for (item in profile) {
                if (item.key != "Profile") {
                    val filename = item.key

                    val bitmap = retrieveImage(filename)

                    val info: String = item.value.toString()
                    val jasonObject = JSONObject(info)

                    val title = if (jasonObject.has("Title"))
                        jasonObject.getString("Title")
                    else
                        resources.getString(R.string.title)

                    val description = if (jasonObject.has("Description"))
                        jasonObject.getString("Description")
                    else
                        resources.getString(R.string.description)

                    val price = if (jasonObject.has("Price"))
                        jasonObject.getString("Price")
                    else
                        resources.getString(R.string.price)

                    items.add(item.key.toInt(), Item(bitmap, title, description, price, " "))
                }
            }
        }

        return items
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
