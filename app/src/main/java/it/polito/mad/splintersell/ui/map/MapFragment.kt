package it.polito.mad.splintersell.ui.map

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import kotlinx.android.synthetic.main.fragment_map.*
import java.io.IOException


class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit var gmap: GoogleMap
    private var latlang: LatLng? = null
    private var mylatlng: LatLng? = null
    lateinit var address: Address

    val user = Firebase.auth.currentUser
    private val firestoreViewModel: FirestoreViewModel by activityViewModels()
    lateinit var liveData: LiveData<UserModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        firestoreViewModel.fetchUserFromFirestore(user!!.uid)
        liveData = firestoreViewModel.user

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

        sv_location.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE

                gmap.clear()

                val location = sv_location.query.toString()
                var list: List<Address> = emptyList()

                if (location.isNotEmpty()) {
                    val geocoder = Geocoder(requireContext())
                    try {
                        list = geocoder.getFromLocationName(location, 1)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (list.isNotEmpty()) {
                        address = list[0]
                        latlang = LatLng(address.latitude, address.longitude)
                        gmap.addMarker(
                            MarkerOptions().position(latlang!!)
                                .title(address.getAddressLine(0).toString())
                        )
                        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlang, 20F))
                    } else {
                        val dialog = AlertDialog.Builder(requireContext())
                        dialog.setMessage("This address is not valid").setCancelable(false)
                            .setPositiveButton("OK") { dialogBox, _ ->
                                dialogBox.dismiss()
                            }
                        dialog.show()
                    }
                }

                return false
            }

        })

        map.getMapAsync(this)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.edit_profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {

                if (latlang != null) {
                    val geopoint = GeoPoint(address.latitude, address.longitude)
                    firestoreViewModel.updateUserLocation(
                        geopoint,
                        address.getAddressLine(0).toString(),
                        user!!.uid
                    )
                    val dialog = AlertDialog.Builder(requireContext())
                    dialog.setMessage("New address saved!").setCancelable(false)
                        .setPositiveButton("OK") { dialogBox, _ ->
                            dialogBox.dismiss()
                            findNavController().popBackStack()
                        }
                    dialog.show()
                } else {

                    if (mylatlng != null) {
                        val dialog = AlertDialog.Builder(requireContext())
                        dialog.setMessage("This address is already saved.").setCancelable(false)
                            .setPositiveButton("OK") { dialogBox, _ ->
                                dialogBox.dismiss()
                            }
                        dialog.show()
                    } else {
                        val dialog = AlertDialog.Builder(requireContext())
                        dialog.setMessage("Please, search for an address.").setCancelable(false)
                            .setPositiveButton("OK") { dialogBox, _ ->
                                dialogBox.dismiss()
                            }
                        dialog.show()
                    }
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(p0: GoogleMap?) {

        p0?.let {
            gmap = it
        }

        liveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer { currentUser ->

            if (currentUser.address != null) {
                val mylatitude = currentUser.address!!.latitude
                val mylongitude = currentUser.address!!.longitude
                mylatlng = LatLng(mylatitude, mylongitude)
                gmap.addMarker(MarkerOptions().position(mylatlng!!).title(currentUser.location))
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(mylatlng, 20F))
            }
        })
    }

}
