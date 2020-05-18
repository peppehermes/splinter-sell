package it.polito.mad.splintersell.ui.profile_edit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputFilter
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import it.polito.mad.splintersell.R
import androidx.exifinterface.media.ExifInterface
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.data.User
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_edit_profile.email
import kotlinx.android.synthetic.main.fragment_edit_profile.location
import kotlinx.android.synthetic.main.fragment_edit_profile.name
import kotlinx.android.synthetic.main.fragment_edit_profile.nickname
import kotlinx.android.synthetic.main.fragment_edit_profile.profile_photo
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_TAKE_PHOTO = 2
const val GALLERY_REQUEST_CODE = 3
var rotatedBitmap: Bitmap? = null
var photoFile: File? = null
var photoURI: Uri? = null

val db = FirebaseFirestore.getInstance()
val user = Firebase.auth.currentUser
val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

class EditProfile : Fragment() {

    lateinit var currentPhotoPath: String
    lateinit var path:String
    var randomString:String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setHasOptionsMenu(true)

        this.setInputLimits()

        this.imageButtonMenu()

        this.retrieveData()


        photoURI = savedInstanceState?.getString("imgUri")?.let { Uri.parse(it) }

        photoURI?.run {
            manageBitmap()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.save_menu, menu)
    }

    @SuppressLint("WrongThread")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveProfile -> {

                //save img on Storage
                if(rotatedBitmap!=null) {
                    Log.d("rotated", rotatedBitmap.toString())
                    //computing a random name for the file
                    randomString = (1..20)
                        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                        .map(charPool::get)
                        .joinToString("")

                    randomString = "$randomString.jpg"

                    //taking the reference of Storage path
                    val profileImageRefs = storage.child("profileImages/$randomString")
                    Log.d("EditProfileTAG", "Name of the file to be stored: $profileImageRefs")

                    val baos = ByteArrayOutputStream()
                    rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    val rotBytes = baos.toByteArray()

                    val uploadTask = profileImageRefs.putBytes(rotBytes)

                    uploadTask.addOnFailureListener {
                        Log.d("EditProfileTAG", "Error in saving image to the Cloud Storage")
                    }.addOnSuccessListener {
                        Log.d("EditProfileTAG", "Success in saving image to the Cloud Storage")
                    }

                    if(path != "img_avatar.jpg") {
                        val refToDelete = storage.child("profileImages/$path")
                        refToDelete.delete().addOnSuccessListener {
                            Log.d("deleteOfFile", "Delete complete")
                        }.addOnFailureListener {
                            Log.d("deleteOfFile", "Delete failed")
                        }
                    }
                }else
                    randomString = path

                val newUser = User(
                    name.text.toString(), nickname.text.toString(),
                    email.text.toString(), location.text.toString(), randomString
                )

                // Update document on the DB
                db.collection("users")
                    .document(user!!.uid)
                    .set(newUser)
                    .addOnSuccessListener {
                        Log.d("EditProfileTAG", "Instance succesfully updated!")
                    }
                    .addOnFailureListener{
                        Log.d("EditProfileTAG", "Error in updating instance")
                    }

                Navigation.findNavController(requireView()).navigate(R.id.nav_show_profile)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Limits the lenght of the input of the EditText fields
    private fun setInputLimits(){

        name.filters = arrayOf(InputFilter.LengthFilter(20))
        nickname.filters = arrayOf(InputFilter.LengthFilter(20))
        email.filters = arrayOf(InputFilter.LengthFilter(30))
        location.filters = arrayOf(InputFilter.LengthFilter(30))

    }

    private fun retrieveImage(path: String) {

        Glide.with(requireContext())
            .using(FirebaseImageLoader())
            .load(storage.child("/profileImages/$path"))
            .into(profile_photo)

    }


    private fun retrieveData(){

        db.collection("users")
            .document(user!!.uid)
            .get()
            .addOnSuccessListener {

                    res ->
                if(res.exists()){
                    val userData: User? = res.toObject(
                        User::class.java)
                    Log.d("ShowProfileTAG", "Success in retrieving data: "+ res.toString())

                    Log.d("ShowProfileTAG", userData.toString())

                    if(userData?.fullname != "")
                        name.setText(userData!!.fullname)
                    if(userData.nickname != "")
                        nickname.setText(userData.nickname)
                    if(userData.email != "")
                        email.setText(userData.email)
                    if(userData.location != "")
                        location.setText(userData.location)

                    path = userData.photoName

                    retrieveImage(path)

                }
                else
                    Log.d("ShowProfileTAG", "No document retrieved")


            }
            .addOnFailureListener{
                Log.d("ShowProfileTAG", "Error in retrieving data")
            }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun imageButtonMenu() {
        // Show menu when tapping on imagebutton
        val button = requireView().findViewById<ImageButton>(R.id.select_photo)
        button.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), button)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.open_camera -> this.dispatchTakePictureIntent()

                    R.id.select_image -> this.pickPhotoFromGallery()

                }
                true
            }
            popupMenu.show()
        }
    }

    // Method to pick photo from gallery
    private fun pickPhotoFromGallery() {
        // Create an intent with action ACTION_PICK
        val imageIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        if (imageIntent.resolveActivity(requireActivity().packageManager) != null)
            startActivityForResult(imageIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            REQUEST_TAKE_PHOTO -> {
                Log.e("LOG", "$data")
                Log.e("photo", "path: ${photoFile?.absolutePath}")

                val bmOptions = BitmapFactory.Options()
                BitmapFactory.decodeFile(
                    photoFile?.absolutePath, bmOptions)?.run {
                    photoURI?.run {
                        Log.e("photo", "uri: $photoURI")
                        manageBitmap()
                    }
                }
            }

            GALLERY_REQUEST_CODE -> {
                Log.e("LOG", "$data")
                //data.data return the content URI for the selected Image
                photoURI = data?.data

                photoURI?.run {
                    Log.e("gallery", "$photoURI")
                    manageBitmap()
                }
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                photoFile = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.e("photo_error", "ERROR IN CREATING UNIQUE NAME")
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        requireContext(),
                        "it.polito.mad.splintersell",
                        it
                    )
                    //currentPhotoUri = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    //Method that returns a unique file name for a new photo using a date-time stamp
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            Log.d("MY_TEST", currentPhotoPath)
        }
    }

    private fun manageBitmap() {
        var bitmap = handleSamplingAndRotationBitmap(requireContext(), photoURI)
        profile_photo.setImageBitmap(bitmap)
        rotatedBitmap = bitmap
    }

    @Throws(IOException::class)
    fun handleSamplingAndRotationBitmap(
        context: Context,
        selectedImage: Uri?
    ): Bitmap? {
        val MAX_HEIGHT = 1024
        val MAX_WIDTH = 1024

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var imageStream = context.contentResolver.openInputStream(selectedImage!!)
        BitmapFactory.decodeStream(imageStream, null, options)
        imageStream!!.close()


        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        imageStream = context.contentResolver.openInputStream(selectedImage)
        var img = BitmapFactory.decodeStream(imageStream, null, options)
        img = rotateImageIfRequired(context, img!!, selectedImage)
        return img
    }

    @Throws(IOException::class)
    private fun rotateImageIfRequired(
        context: Context,
        img: Bitmap,
        selectedImage: Uri
    ): Bitmap? {
        val input: InputStream = context.contentResolver.openInputStream(selectedImage)!!
        val ei: ExifInterface
        ei =
            ExifInterface(input)
        Log.e("photo_orientation", ei.getAttribute(ExifInterface.TAG_ORIENTATION).toString())
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        Log.e("photo_orientation", "$orientation")
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg =
            Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        photoURI?.run {
            outState.putString("imgUri", this.toString())
        }
    }





}
