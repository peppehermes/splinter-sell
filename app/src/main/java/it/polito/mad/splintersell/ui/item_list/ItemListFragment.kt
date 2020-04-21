package it.polito.mad.splintersell.ui.item_list

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.splintersell.Item
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_item_list.*
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ItemListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // TODO: Replace these with information about ItemCards
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        val profile: MutableMap<String,*>? = sharedPref.all

        // Take the data of the items
        val items = arrayOf(
            Item("Title 1", "Description 1", "10$","none"),
            Item("Title 2", "Description 2", "10$","none"),
            Item("Title 3", "Description 3", "10$","none")
        )

        var title: String
        var description: String
        var price: String


        if (profile != null) {
            for (item in profile) {
                if (item.key.toString() != "Profile") {

                    val info: String? = item.value.toString()
                    val jasonObject = JSONObject(info);

                    title = if (jasonObject.has("Title"))
                        jasonObject.getString("Title")
                    else
                        resources.getString(R.string.title)

                    description = if (jasonObject.has("Description"))
                        jasonObject.getString("Description")
                    else
                        resources.getString(R.string.description)

                    price = if (jasonObject.has("Price"))
                        jasonObject.getString("Price")
                    else
                        resources.getString(R.string.price)


                    val element = Item(title, description, price, " ")
                    items[item.key.toString().toInt()] = element
                }
            }
        }
        item_list.layoutManager = LinearLayoutManager(context)
        item_list.adapter = ItemCardAdapter(items)
    }
}
