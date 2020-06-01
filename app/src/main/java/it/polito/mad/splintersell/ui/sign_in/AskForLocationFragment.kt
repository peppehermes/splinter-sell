package it.polito.mad.splintersell.ui.sign_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_ask_for_location.*

class AskForLocationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ask_for_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_send.setOnClickListener {
            val action = AskForLocationFragmentDirections.globalUpdateYourLocation(true)
            findNavController().navigate(action)
        }
    }
}
