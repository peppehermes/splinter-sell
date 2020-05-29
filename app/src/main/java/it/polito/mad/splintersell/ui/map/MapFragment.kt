package it.polito.mad.splintersell.ui.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback

import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_map.*


class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit var gmap:GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        map.onCreate(savedInstanceState)
        map.onResume()
        map.getMapAsync(this)

    }

    override fun onMapReady(p0: GoogleMap?) {

        p0?.let {
            gmap = it
        }
    }


}
