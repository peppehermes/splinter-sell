package it.polito.mad.splintersell.ui.profile_show

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.navigation.Navigation
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_show_profile.*
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

const val EXTRA_NAME = "it.polito.mad.splintersell.NAME"
const val EXTRA_NICKNAME = "it.polito.mad.splintersell.NICKNAME"
const val EXTRA_EMAIL = "it.polito.mad.splintersell.EMAIL"
const val EXTRA_LOCATION = "it.polito.mad.splintersell.LOCATION"
const val filename = "proPic"

class ShowProfile : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        //Retrieve all the information from the local file system
        val sharedPref: SharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val profile: String? = sharedPref.getString("Profile", null)

        this.retrievePreferences(profile)

        this.retrieveImage()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editProfile -> {
                editProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // This have to be invoked when the pencil button is pressed
    private fun editProfile() {
        Navigation.findNavController(requireView()).navigate(R.id.editProfile)
    }

    private fun retrieveImage() {
        val file = File(activity?.filesDir, filename)
        val fileExists = file.exists()
        if (fileExists) {
            val fis: FileInputStream = requireActivity().openFileInput(filename)
            val bitmap = BitmapFactory.decodeStream(fis)
            val roundDrawable: RoundedBitmapDrawable =
                RoundedBitmapDrawableFactory.create(resources, bitmap)
            roundDrawable.isCircular = true
            fis.close()
            profile_photo.setImageDrawable(roundDrawable)

        }
    }

    private fun retrievePreferences(profile: String?) {
        if (profile != null) {

            val jasonObject = JSONObject(profile)
            val savedName: String
            val savedNickname: String
            val savedEmail: String
            val savedLocation: String

            savedName = if (jasonObject.has(EXTRA_NAME))
                jasonObject.getString(EXTRA_NAME)
            else
                resources.getString(R.string.fname)

            savedNickname = if (jasonObject.has(EXTRA_NICKNAME))
                jasonObject.getString(EXTRA_NICKNAME)
            else
                resources.getString(R.string.nick)

            savedEmail = if (jasonObject.has(EXTRA_EMAIL))
                jasonObject.getString(EXTRA_EMAIL)
            else
                resources.getString(R.string.mail)

            savedLocation = if (jasonObject.has(EXTRA_LOCATION))
                jasonObject.getString(EXTRA_LOCATION)
            else
                resources.getString(R.string.location)

            name.text = savedName
            nickname.text = savedNickname
            email.text = savedEmail
            location.text = savedLocation
        }
    }
}
