package it.polito.mad.splintersell.ui.item_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_image_view.*

class ImageViewFragment : Fragment() {
    private val args: ImageViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext()).using(FirebaseImageLoader())
            .load(storage.child("/itemImages/${args.imgPath}")).into(full_image)
    }
}
