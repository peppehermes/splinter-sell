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
import android.view.animation.Transformation
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import it.polito.mad.splintersell.R
import kotlin.math.roundToInt
import androidx.exifinterface.media.ExifInterface
import androidx.navigation.Navigation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import it.polito.mad.splintersell.User
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
const val filename = "proPic"
const val localfile_name = "imageProfile"
var rotatedBitmap: Bitmap? = null
var photoFile: File? = null
var photoURI: Uri? = null

val db = FirebaseFirestore.getInstance()
val storage = FirebaseStorage.getInstance().reference
val user = Firebase.auth.currentUser


class EditProfile : Fragment() {

    lateinit var currentPhotoPath: String


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

        this.retrieveImage()

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
                //Save bitmap
                if (rotatedBitmap != null) {
                    val fos: FileOutputStream = requireActivity().openFileOutput(filename, Context.MODE_PRIVATE)
                    rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 75, fos)
                    fos.close()
                }

                //Save image on Cloud Storage

                var profileRefs = storage.child("profileImages")
                val profileImageName = storage.child(user!!.uid+".jpg")
                val profileImageRefs= storage.child("profileImages/"+user.uid+".jpg")
                Log.d("EditProfileTAG", "Name of the file to be stored: $profileImageRefs")

                if(rotatedBitmap!=null){
                    val baos = ByteArrayOutputStream()
                    rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                    val rotBytes = baos.toByteArray()

                    val uploadTask = profileImageRefs.putBytes(rotBytes)
                    uploadTask.addOnFailureListener {
                        Log.d("EditProfileTAG", "Error in saving image to the Cloud Storage")
                    }.addOnSuccessListener {
                        Log.d("EditProfileTAG", "Success in saving image to the Cloud Storage")
                    }

                }

                val newUser = User(name.text.toString(), nickname.text.toString(),
                                    email.text.toString(), location.text.toString())

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


        //TODO Fix retrieving image of User from Storage (EditProfile)
/*
        val profileImageRefs= storage.child("profileImages/"+user!!.uid+".jpg")

        val localFile = File(activity?.filesDir, localfile_name)


        Log.d("EditProfileTAG", "Path of the image:"+localFile.absolutePath)
        Log.d("EditProfileTAG", "Name of the image:"+localFile.name)
        profileImageRefs.getFile(localFile)
            .addOnSuccessListener {
                Log.d("EditProfileTAG", "File created")
            }
            .addOnFailureListener{
                Log.d("EditProfileTAG", "Error in file creation")
            }


        val fileExists = localFile.exists()
        if (fileExists) {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            val roundDrawable: RoundedBitmapDrawable =
                RoundedBitmapDrawableFactory.create(resources, bitmap)
            roundDrawable.isCircular = true
            profile_photo.setImageDrawable(roundDrawable)
        }


 */

    }


    private fun retrieveData(){

        db.collection("users")
            .document(user!!.uid)
            .get()
            .addOnSuccessListener {

                    res ->
                if(res.exists()){
                    val userData: User? = res.toObject(User::class.java)
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
        bitmap = CropSquareTransformation().transform(bitmap!!)
        val roundDrawable: RoundedBitmapDrawable =
            RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundDrawable.isCircular = true
        profile_photo.setImageDrawable(roundDrawable)
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

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        imageStream = context.contentResolver.openInputStream(selectedImage)
        var img = BitmapFactory.decodeStream(imageStream, null, options)
        img = rotateImageIfRequired(context, img!!, selectedImage)
        return img
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            val heightRatio =
                (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio =
                (width.toFloat() / reqWidth.toFloat()).roundToInt()

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).
            val totalPixels = width * height.toFloat()

            // Anything more than 2x the requested pixels we'll sample down further
            val totalReqPixelsCap = reqWidth * reqHeight * 2.toFloat()
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++
            }
        }
        return inSampleSize
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

    class CropSquareTransformation : Transformation() {
        fun transform(source: Bitmap): Bitmap {
            val size = source.width.coerceAtMost(source.height)
            val x = (source.width - size) / 2
            val y = (source.height - size) / 2
            val result = Bitmap.createBitmap(source, x, y, size, size)
            if (result != source) {
                source.recycle()
            }
            return result
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        photoURI?.run {
            outState.putString("imgUri", this.toString())
        }
    }





}
