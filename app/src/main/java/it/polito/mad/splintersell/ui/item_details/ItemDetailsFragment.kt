package it.polito.mad.splintersell.ui.item_details

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.widget.Button
import androidx.fragment.app.Fragment
import it.polito.mad.splintersell.Item
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_item_details.*

class ItemDetailsFragment: Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item =  Item(detail_image.toString(),title.text.toString(),description.text.toString(),
            price.text.toString(),category.text.toString(),location.text.toString(),expire_date.text.toString())

/*
   val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
      with(sharedPref.edit()) {
       putString("1", item.toString())
       apply()
*/
   }

}


