package it.polito.mad.splintersell.ui.item_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.splintersell.Item
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_item_list.*

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

        // Take the data of the items
        val items = arrayOf(
            Item("Title 1", "Description 1", 10F),
            Item("Title 2", "Description 2", 20F),
            Item("Title 3", "Description 3", 30F)
        )

        item_list.layoutManager = LinearLayoutManager(context)
        item_list.adapter = ItemCardAdapter(items)
    }
}
