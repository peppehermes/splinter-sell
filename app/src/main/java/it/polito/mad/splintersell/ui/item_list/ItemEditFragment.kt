package it.polito.mad.splintersell.ui.item_list


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_edit_item.*
import org.json.JSONObject

const val PERMISSION_CODE = 1001
/*
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
*/
/**
 * A simple [Fragment] subclass.
 * Use the [ItemEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ItemEditFragment : Fragment() {

    var index: Int?=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_edit_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.populateEditText()
        this.imageButtonMenu()
    }


    private fun populateEditText() {

        val editTitle:String ? =arguments?.getString("EDIT_TITLE")
        val editDescription:String ? =arguments?.getString("EDIT_DESCRIPTION")
        val editPrice:String ? =arguments?.getString("EDIT_PRICE")

        index=arguments?.getInt("EDIT_POSITION")

        if (editTitle != resources.getString(R.string.title)&& editTitle !=null)
            title.setText(editTitle)
        if (editDescription != resources.getString(R.string.description)&& editDescription !=null)
            description.setText(editDescription)
        if (editPrice != resources.getString(R.string.price)&& editPrice !=null)
            price.setText(editPrice)

    }

    private fun imageButtonMenu() {
        // Show menu when tapping on imagebutton

        select_photo.setOnClickListener {
            val popupMenu = PopupMenu(requireActivity().applicationContext, it)
            popupMenu.inflate(R.menu.popup_menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {



            }
                true
            }
            popupMenu.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Do something that differs the Activity's menu here
        inflater.inflate(R.menu.save_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveProfile -> {

                // Create JSON Object and fill it with data to store
                val rootObject = JSONObject()
                if (!title.text.isNullOrEmpty())
                    rootObject.accumulate("Title",title.text)

                if (!description.text.isNullOrEmpty())
                    rootObject.accumulate("Description", description.text)

                if (!price.text.isNullOrEmpty())
                    rootObject.accumulate("Price", price.text)

                if (!category.text.isNullOrEmpty())
                    rootObject.accumulate("Category", category.text)

                if (!location.text.isNullOrEmpty())
                    rootObject.accumulate("Location",location.text)


                val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString(index.toString(),  rootObject.toString())
                    apply()
                    Navigation.findNavController(requireView()).navigate(R.id.action_nav_edit_item_to_nav_item_list)

                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}







