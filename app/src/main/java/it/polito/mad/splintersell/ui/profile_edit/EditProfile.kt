package it.polito.mad.splintersell.ui.profile_edit

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_edit_profile.email
import kotlinx.android.synthetic.main.fragment_edit_profile.location
import kotlinx.android.synthetic.main.fragment_edit_profile.name
import kotlinx.android.synthetic.main.fragment_edit_profile.nickname
import kotlinx.android.synthetic.main.fragment_edit_profile.profile_photo
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

const val REQUEST_TAKE_PHOTO = 2
const val GALLERY_REQUEST_CODE = 3

val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

class EditProfile : Fragment() {

    val user = Firebase.auth.currentUser
    private val firestoreViewModel: FirestoreViewModel by viewModels()
    lateinit var liveData: LiveData<UserModel>

    lateinit var currentPhotoPath: String
    lateinit var path:String
    var randomString:String = ""


    var rotatedBitmap: Bitmap? = null
    var photoFile: File? = null
    var photoURI: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestoreViewModel.fetchUserFromFirestore(user!!.uid)
        liveData = firestoreViewModel.myUser

    }


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


        photoURI = savedInstanceState?.getString("imgUri")?.let { Uri.parse(it) }

        photoURI?.run {
            manageBitmap()
        }


        liveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            name.setText(it.fullname)
            nickname.setText(it.nickname)
            email.setText(it.email)
            location.setText(it.location)


            path = it.photoName
            if(path == "")
                profile_photo.setImageDrawable(requireContext().getDrawable(R.drawable.image_vectorized_lower))
            else{

                Glide.with(requireContext())
                    .using(FirebaseImageLoader())
                    .load(storage.child("/profileImages/$path"))
                    .into(profile_photo)
            }

        })


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.edit_profile_menu, menu)
    }

    @SuppressLint("WrongThread")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveProfile -> {

                //Form validation

                val checkError = formValidation()

                if (!checkError){

                    Log.d("EditItemTAG", "Error in Item Form Validation")


                    //Save image on Cloud Storage

                    if(rotatedBitmap!=null){

                        randomString = (1..20)
                            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                            .map(charPool::get)
                            .joinToString("")

                        randomString = "$randomString.jpg"

                        setNewUser()

                        uploadImageOnStorage()


                    }else {
                        randomString = path

                        setNewUser()
                        navigateMyProfile()
                    }


                }
                else
                    Log.d("EditProfileTAG", "Error in Form Validation")


                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun formValidation(): Boolean{

        var result = false

        if (name.text.isEmpty()){
            name.error = getString(R.string.please_fill)
            result = true
        }
        if (nickname.text.isEmpty()){
            nickname.error = getString(R.string.please_fill)
            result = true
        }
        if (email.text.isEmpty()){
            email.error = getString(R.string.please_fill)
            result = true
        }
        if (location.text.isEmpty()){
            location.error = getString(R.string.please_fill)
            result = true
        }

        return result

    }




    //Limits the lenght of the input of the EditText fields
    private fun setInputLimits(){

        name.filters = arrayOf(InputFilter.LengthFilter(20))
        nickname.filters = arrayOf(InputFilter.LengthFilter(20))
        email.filters = arrayOf(InputFilter.LengthFilter(30))
        location.filters = arrayOf(InputFilter.LengthFilter(30))

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
        Log.d("photoURI", photoURI.toString())
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

    private fun uploadImageOnStorage(){

        val dialog1 = AlertDialog.Builder(requireContext()).create()
        val dialog2 = AlertDialog.Builder(requireContext())
        dialog1.setMessage("Uploading Your Profile")
        dialog1.setCancelable(false)
        dialog1.show()

        val profileImageRefs= storage.child("profileImages/$randomString")
        Log.d("ItemEditTAG", "Name of the file to be stored: $profileImageRefs")

        val baos = ByteArrayOutputStream()
        rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val rotBytes = baos.toByteArray()
        val uploadTask = profileImageRefs.putBytes(rotBytes).addOnCompleteListener{
            dialog1.cancel()
            dialog2.setMessage("Done!")
                .setCancelable(false)
            dialog2.setPositiveButton("Great!") { dialog, _ ->
                dialog.dismiss()
                navigateMyProfile()
            }
            dialog2.show()


        }

        uploadTask.addOnFailureListener {
            Log.d("ItemEditTAG", "Error in saving image to the Cloud Storage")
        }.addOnSuccessListener {
            Log.d("ItemEditTAG", "Success in saving image to the Cloud Storage")
        }

        if(path != "img_avatar.jpg") {
            val refToDelete = storage.child("itemImages/$path")
            refToDelete.delete().addOnSuccessListener {
                Log.d("deleteOfFile", "Delete complete on item $path")
            }.addOnFailureListener {
                Log.d("deleteOfFile", "Delete failed")
            }
        }


    }

    private fun setNewUser(){

        val newUser = UserModel(
            name.text.toString(), nickname.text.toString(),
            email.text.toString(), location.text.toString(), randomString
        )
        firestoreViewModel.saveUserToFirestore(newUser)

    }

    private fun navigateMyProfile(){
        val action = EditProfileDirections.editToShow()
        Navigation.findNavController(requireView()).navigate(action)
    }


}
