package it.polito.mad.splintersell

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import kotlinx.android.synthetic.main.fragment_show_profile.*
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

const val EXTRA_NAME = "it.polito.mad.splintersell.NAME"
const val EXTRA_NICKNAME = "it.polito.mad.splintersell.NICKNAME"
const val EXTRA_EMAIL = "it.polito.mad.splintersell.EMAIL"
const val EXTRA_LOCATION = "it.polito.mad.splintersell.LOCATION"
const val EDIT_CODE = 1
const val COLOR = "white"


class ShowProfile : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }




    override fun onStart() {
        super.onStart()
        //Retrieve all the information from the local file system
        val sharedPref: SharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val profile: String? = sharedPref.getString("Profile", null)

        this.retrievePreferences(profile)

        this.retrieveImage()

    }


    private fun retrieveImage() {
        val filename = "img"
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_CODE && resultCode == Activity.RESULT_OK) {
            // Get the Intent from the edit activity and extract the strings
            val editName = data?.getStringExtra(EXTRA_NAME)
            val editNickname = data?.getStringExtra(EXTRA_NICKNAME)
            val editEmail = data?.getStringExtra(EXTRA_EMAIL)
            val editLocation = data?.getStringExtra(EXTRA_LOCATION)

            // Create JSON Object and fill it with data to store
            val rootObject = JSONObject()
            if (!editName.isNullOrEmpty()) {
                rootObject.accumulate(EXTRA_NAME, editName)
                name.text = editName
            }
            if (!editNickname.isNullOrEmpty()) {
                rootObject.accumulate(EXTRA_NICKNAME, editNickname)
                nickname.text = editNickname
            }
            if (!editEmail.isNullOrEmpty()) {
                rootObject.accumulate(EXTRA_EMAIL, editEmail)
                email.text = editEmail
            }
            if (!editLocation.isNullOrEmpty()) {
                rootObject.accumulate(EXTRA_LOCATION, editLocation)
                location.text = editLocation
            }

            //Persist all the information in the local file system
            val sharedPref: SharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putString("Profile", rootObject.toString())
                apply()
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_NAME, name.text.toString())
        outState.putString(EXTRA_NICKNAME, nickname.text.toString())
        outState.putString(EXTRA_EMAIL, email.text.toString())
        outState.putString(EXTRA_LOCATION, location.text.toString())
        super.onSaveInstanceState(outState)
    }

}
