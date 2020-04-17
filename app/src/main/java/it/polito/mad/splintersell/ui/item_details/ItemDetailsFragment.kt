package it.polito.mad.splintersell.ui.item_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_item_details.*

class ItemDetailsFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("PIPPO", "JERRY")
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myDataset = arrayOf(
            Detail("Title", "title"),
            Detail("Description","description"),
            Detail("Price","price"),
            Detail("Category","category"),
            Detail("Location","location"),
            Detail("Expiration date","date")
        )

        Log.e("PIPPO", "PLUTO")
        item_details_view.layoutManager = LinearLayoutManager(context)
        item_details_view.adapter = ItemDetailsAdapter(myDataset)
        Log.e("PIPPO", "PAPERINO")
    }
}