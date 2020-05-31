package it.polito.mad.splintersell.ui.item_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import it.polito.mad.splintersell.MainActivity
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.storage
import it.polito.mad.splintersell.ui.hideSystemUI
import it.polito.mad.splintersell.ui.showSystemUI
import kotlinx.android.synthetic.main.fragment_image_view.*

class ImageViewFragment : Fragment() {
    private val args: ImageViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        return inflater.inflate(R.layout.fragment_image_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showSystemUI(activity as MainActivity)
            findNavController().popBackStack()
        }

        hideSystemUI(activity as MainActivity)

        Glide.with(requireContext()).using(FirebaseImageLoader())
            .load(storage.child("/itemImages/${args.imgPath}")).into(full_image)
    }
}
