package it.polito.mad.splintersell.ui.profile_edit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlin.math.roundToInt
import androidx.exifinterface.media.ExifInterface
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_edit_item.*
import kotlinx.android.synthetic.main.fragment_edit_profile.email
import kotlinx.android.synthetic.main.fragment_edit_profile.location
import kotlinx.android.synthetic.main.fragment_edit_profile.name
import kotlinx.android.synthetic.main.fragment_edit_profile.nickname
import kotlinx.android.synthetic.main.fragment_edit_profile.profile_photo
import kotlinx.android.synthetic.main.fragment_show_profile.*
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.category

const val REQUEST_TAKE_PHOTO = 2
const val GALLERY_REQUEST_CODE = 3
const val PERMISSION_CODE = 1001
const val filename = "img"
var rotatedBitmap: Bitmap? = null
var photoFile: File? = null
var photoURI: Uri? = null

const val EXTRA_NAME = "it.polito.mad.splintersell.NAME"
const val EXTRA_NICKNAME = "it.polito.mad.splintersell.NICKNAME"
const val EXTRA_EMAIL = "it.polito.mad.splintersell.EMAIL"
const val EXTRA_LOCATION = "it.polito.mad.splintersell.LOCATION"

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

        this.imageButtonMenu()

        this.retrieveImage()

        //Retrieve all the information from the local file system
        val sharedPref: SharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val profile: String? = sharedPref.getString("Profile", null)

        this.retrievePreferences(profile)

        photoURI = savedInstanceState?.getString("imgUri")?.let { Uri.parse(it) }

        photoURI?.run {
            manageBitmap()
        }


    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveProfile -> {
                //Save bitmap
                if (rotatedBitmap != null) {
                    val fos: FileOutputStream = requireActivity().openFileOutput(filename, Context.MODE_PRIVATE)
                    rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 75, fos)
                    fos.close()
                }

                // Create JSON Object and fill it with data to store
                val rootObject = JSONObject()
                if (!name.text.isNullOrEmpty()) {
                    rootObject.accumulate(EXTRA_NAME, name.text)
                }
                if (!nickname.text.isNullOrEmpty()) {
                    rootObject.accumulate(EXTRA_NICKNAME, nickname.text)
                }
                if (!email.text.isNullOrEmpty()) {
                    rootObject.accumulate(EXTRA_EMAIL, email.text)
                }
                if (!location.text.isNullOrEmpty()) {
                    rootObject.accumulate(EXTRA_LOCATION, location.text)
                }

                //Persist all the information in the local file system
                val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("Profile",  rootObject.toString())
                    apply()
                    Navigation.findNavController(requireView()).navigate(R.id.action_nav_edit_profile_to_nav_show_profile)

                }


                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

            savedName = if (jasonObject.has(it.polito.mad.splintersell.ui.profile_show.EXTRA_NAME))
                jasonObject.getString(it.polito.mad.splintersell.ui.profile_show.EXTRA_NAME)
            else
                resources.getString(R.string.fname)

            savedNickname = if (jasonObject.has(it.polito.mad.splintersell.ui.profile_show.EXTRA_NICKNAME))
                jasonObject.getString(it.polito.mad.splintersell.ui.profile_show.EXTRA_NICKNAME)
            else
                resources.getString(R.string.nick)

            savedEmail = if (jasonObject.has(it.polito.mad.splintersell.ui.profile_show.EXTRA_EMAIL))
                jasonObject.getString(it.polito.mad.splintersell.ui.profile_show.EXTRA_EMAIL)
            else
                resources.getString(R.string.mail)

            savedLocation = if (jasonObject.has(it.polito.mad.splintersell.ui.profile_show.EXTRA_LOCATION))
                jasonObject.getString(it.polito.mad.splintersell.ui.profile_show.EXTRA_LOCATION)
            else
                resources.getString(R.string.location)

            name.setText(savedName)
            nickname.setText(savedNickname)
            email.setText(savedEmail)
            location.setText(savedLocation)
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
            if (Build.VERSION.SDK_INT > 23) ExifInterface(input) else ExifInterface(selectedImage.path!!)
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
