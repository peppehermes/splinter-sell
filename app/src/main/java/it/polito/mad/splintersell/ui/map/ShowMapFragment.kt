package it.polito.mad.splintersell.ui.map

import android.location.Address
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.data.UserModel
import kotlinx.android.synthetic.main.fragment_show_map.*


class ShowMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    lateinit var gmap: GoogleMap
    private var itemLatlang: LatLng? = null
    private var userLatlng: LatLng? = null

    private val args: ShowMapFragmentArgs by navArgs()

    val user = Firebase.auth.currentUser
    private val firestoreViewModel: FirestoreViewModel by activityViewModels()
    lateinit var itemLiveData: LiveData<ItemModel>
    lateinit var userLiveData: LiveData<UserModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(args.isUserLocation == false){

            firestoreViewModel.fetchSingleItemFromFirestore(args.itemID)
            itemLiveData = firestoreViewModel.item

            if(args.notMyItem){
                firestoreViewModel.fetchUserFromFirestore(user!!.uid)
                userLiveData = firestoreViewModel.user
            }

        }
        else{

            if(args.userID == "currUser")
                firestoreViewModel.fetchUserFromFirestore(user!!.uid)
            else
                firestoreViewModel.fetchUserFromFirestore(args.userID!!)

            userLiveData = firestoreViewModel.user

        }







    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_map, container, false)
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


        if(args.isUserLocation == false){

            itemLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer { currentItem ->

                val itemLatitude = currentItem.address!!.latitude
                val itemLongitude = currentItem.address!!.longitude
                itemLatlang = LatLng(itemLatitude, itemLongitude)
                gmap.addMarker(MarkerOptions().position(itemLatlang!!).title(currentItem.location))
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(itemLatlang, 15F))

                if (args.notMyItem) {

                    userLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer { currentUser ->

                        val userLatitude = currentUser.address!!.latitude
                        val userLongitude = currentUser.address!!.longitude
                        userLatlng = LatLng(userLatitude, userLongitude)
                        gmap.addMarker(MarkerOptions().position(userLatlng!!).title(currentUser.location))

                        val myPolylineOptions = PolylineOptions()

                        gmap.addPolyline(
                            myPolylineOptions.add(itemLatlang, userLatlng)
                        )


                        gmap.setOnPolylineClickListener(this)




                    })



                }

            })



        }
        else{

            userLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer { currentUser ->

                val userLatitude = currentUser.address!!.latitude
                val userLongitude = currentUser.address!!.longitude
                userLatlng = LatLng(userLatitude, userLongitude)
                gmap.addMarker(MarkerOptions().position(userLatlng!!).title(currentUser.location))
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatlng, 20F))


            })



        }





    }

    override fun onPolylineClick(p0: Polyline?) {
        TODO("Not yet implemented")
    }


}
