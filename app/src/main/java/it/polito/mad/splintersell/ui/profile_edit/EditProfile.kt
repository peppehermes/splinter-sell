package it.polito.mad.splintersell.ui.profile_edit

import android.annotation.SuppressLint
import android.app.Activity
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
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

const val REQUEST_TAKE_PHOTO = 2
const val GALLERY_REQUEST_CODE = 3

val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

class EditProfile : Fragment() {
    private val TAG = "EDIT_PROFILE"

    private val user = Firebase.auth.currentUser
    private val firestoreViewModel: FirestoreViewModel by viewModels()
    private val userModel: EditProfileViewModel by activityViewModels()
    lateinit var liveData: LiveData<UserModel>

    lateinit var currentPhotoPath: String
    lateinit var path: String
    lateinit var oldPath: String
    var counterFeedback: Int = 0
    var rating: Float = 0F
    var token: String = ""
    var randomString: String = ""

    var rotatedBitmap: Bitmap? = null
    var photoFile: File? = null
    var photoURI: Uri? = null

    private var existingLocation: String? = null
    private var existingAddress: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (firestoreViewModel.user.value == null)
            firestoreViewModel.fetchUserFromFirestore(user!!.uid)
        liveData = firestoreViewModel.user

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Empty the photo field in ViewModel
                userModel.photo = MutableLiveData()

                // Empty the location and address field in ViewModel
                userModel.user.value = UserModel()

                // Handle the back button event
                navigateMyProfile()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setHasOptionsMenu(true)

        hideKeyboardFrom(requireContext(), requireView())

        this.setInputLimits()

        this.imageButtonMenu()

        var savedName: String? = null
        var savedNickname: String? = null
        var savedEmail: String? = null
        var savedLocation: String? = null

//        savedInstanceState?.run {
//            // If an image has been taken , retrieve Uri
//            if (savedInstanceState.get("imgUri").toString() != "null")
//                savedImg = savedInstanceState.get("imgUri").toString()
//        }

        val savedImg: String? = userModel.photo?.value

        userModel.user.observe(viewLifecycleOwner, androidx.lifecycle.Observer { userData ->
            userData.fullname?.apply {
                savedName = this
            }

            userData.nickname?.apply {
                savedNickname = this
            }

            userData.email?.apply {
                savedEmail = this
            }

            userData.location?.apply {
                savedLocation = this
            }
        })

        liveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer { currentUser ->
            if (savedName == null)
                name.setText(currentUser.fullname)
            else
                name.setText(savedName)

            if (savedNickname == null) {
                nickname.setText(currentUser.nickname)
                userModel.oldNick.value = currentUser.nickname
            } else {
                nickname.setText(savedNickname)
            }

            if (savedEmail == null)
                email.setText(currentUser.email)
            else
                email.setText(savedEmail)

            if (savedLocation == null)
                location.setText(currentUser.location)
            else
                location.setText(savedLocation)

            // Check if a location already exists
            if (currentUser.location != null) {
                existingLocation = currentUser.location
                existingAddress = currentUser.address
            }

            if (userModel.user.value?.location != null &&
                userModel.user.value?.address != null
            ) {
                existingLocation = userModel.user.value?.location
                existingAddress = userModel.user.value?.address
            }

            // Save current image path
            userModel.user.value?.photoName = currentUser.photoName

            //email.setText(currentUser.email)
            if (savedImg == null) {
                path = currentUser.photoName
                if (path == "") profile_photo.setImageDrawable(
                    requireContext()
                        .getDrawable(R.drawable.image_vectorized_lower)
                )
                else {

                    Glide.with(requireContext()).using(FirebaseImageLoader())
                        .load(storage.child("/profileImages/$path")).into(profile_photo)
                }
            } else {
                this.restoreImage(savedImg)
            }

            // Store token, counterFeedback and rating for next updates
            token = currentUser.token
            counterFeedback = currentUser.counterfeed
            rating = currentUser.rating
        })

        location.setOnClickListener {
            this.saveOnViewModel()
            val action = EditProfileDirections.fromProfileToEditMap()
            findNavController().navigate(action)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.edit_profile_menu, menu)
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("WrongThread")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {

                //Form validation
                if (nickname.text!!.toString() != userModel.oldNick.value) {
                    // Check if the nickname is unique
                    firestoreViewModel.firestoreRepository.firestore.collection("users")
                        .whereEqualTo("nickname", nickname.text!!.toString())
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                til_nickname.error =
                                    requireContext().getString(R.string.nick_not_available)
                            } else {
                                hideKeyboardFrom(requireContext(), requireView())

                                val checkError = formValidation()

                                if (!checkError) {
                                    //Save image on Cloud Storage

                                    if (rotatedBitmap != null) {

                                        randomString =
                                            (1..20).map { _ ->
                                                kotlin.random.Random.nextInt(
                                                    0,
                                                    charPool.size
                                                )
                                            }
                                                .map(charPool::get).joinToString("")

                                        randomString = "$randomString.jpg"

                                        setNewUser()

                                        uploadImageOnStorage()


                                    } else {
                                        randomString = path

                                        setNewUser()
                                        val dialog = AlertDialog.Builder(requireContext())
                                        dialog.setMessage("Done!").setCancelable(false)
                                            .setPositiveButton("Great!") { dialogBox, _ ->
                                                dialogBox.dismiss()
                                                navigateMyProfile()
                                            }
                                        dialog.show()
                                    }


                                } else {
                                    Snackbar.make(
                                        requireView(),
                                        R.string.please_fill_all,
                                        Snackbar.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        }
                } else {
                    hideKeyboardFrom(requireContext(), requireView())

                    val checkError = formValidation()

                    if (!checkError) {
                        //Save image on Cloud Storage

                        if (rotatedBitmap != null) {

                            randomString =
                                (1..20).map { _ -> kotlin.random.Random.nextInt(0, charPool.size) }
                                    .map(charPool::get).joinToString("")

                            randomString = "$randomString.jpg"

                            setNewUser()

                            uploadImageOnStorage()


                        } else {
                            randomString = path

                            setNewUser()
                            val dialog = AlertDialog.Builder(requireContext())
                            dialog.setMessage("Done!").setCancelable(false)
                                .setPositiveButton("Great!") { dialogBox, _ ->
                                    dialogBox.dismiss()
                                    navigateMyProfile()
                                }
                            dialog.show()
                        }


                    } else {
                        Snackbar.make(
                            requireView(),
                            R.string.please_fill_all,
                            Snackbar.LENGTH_SHORT
                        )
                            .show()
                    }
                }

                true
            }
            android.R.id.home -> {
                // Empty the photo field in ViewModel
                userModel.photo = MutableLiveData()
                // Empty the location and address field in ViewModel
                userModel.user.value = UserModel()
                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun restoreImage(savedImg: String?) {
        photoURI = savedImg?.let { Uri.parse(it) }
        photoURI?.run {
            manageBitmap()
        }
    }

    private fun formValidation(): Boolean {
        var result = false

        if (name.text!!.isEmpty()) {
            name.error = getString(R.string.please_fill)
            result = true
        }
        if (nickname.text!!.isEmpty()) {
            nickname.error = getString(R.string.please_fill)
            result = true
        }
        if (email.text!!.isEmpty()) {
            email.error = getString(R.string.please_fill)
            result = true
        }
        if (location.text!!.isEmpty()) {
            location.error = getString(R.string.please_fill)
            result = true
        }

        return result

    }


    //Limits the length of the input of the EditText fields
    private fun setInputLimits() {

        name.filters = arrayOf(InputFilter.LengthFilter(20))
        nickname.filters = arrayOf(InputFilter.LengthFilter(20))
        email.filters = arrayOf(InputFilter.LengthFilter(30))
        location.filters = arrayOf(InputFilter.LengthFilter(60))

    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun imageButtonMenu() {
        // Show show_profile_menu when tapping on imagebutton
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
        if (imageIntent.resolveActivity(requireActivity().packageManager) != null) startActivityForResult(
            imageIntent,
            GALLERY_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            REQUEST_TAKE_PHOTO -> {
                val bmOptions = BitmapFactory.Options()
                BitmapFactory.decodeFile(
                    photoFile?.absolutePath, bmOptions
                )?.run {
                    photoURI?.run {
                        manageBitmap()
                    }
                }
            }

            GALLERY_REQUEST_CODE -> {
                //data.data return the content URI for the selected Image
                photoURI = data?.data

                photoURI?.run {
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
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        requireContext(), "it.polito.mad.splintersell", it
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
        val storageDir: File? =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun manageBitmap() {
        val bitmap = handleSamplingAndRotationBitmap(requireContext(), photoURI)
        profile_photo.setImageBitmap(bitmap)
        rotatedBitmap = bitmap
    }

    @Throws(IOException::class)
    fun handleSamplingAndRotationBitmap(
        context: Context, selectedImage: Uri?
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
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()

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
        context: Context, img: Bitmap, selectedImage: Uri
    ): Bitmap? {
        val input: InputStream = context.contentResolver.openInputStream(selectedImage)!!
        val ei: ExifInterface
        ei = ExifInterface(input)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
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
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        this.saveOnViewModel()
    }

    private fun saveOnViewModel() {
        userModel.user.value?.fullname = name.text.toString()
        userModel.user.value?.nickname = nickname.text.toString()
        userModel.user.value?.email = email.text.toString()
        userModel.user.value?.location = location.text.toString()

        if (rotatedBitmap != null)
            photoURI?.run {
                userModel.photo?.value = photoURI.toString()
            }
    }

    private fun uploadImageOnStorage() {

        val dialog1 = AlertDialog.Builder(requireContext()).create()
        val dialog2 = AlertDialog.Builder(requireContext())
        dialog1.setMessage("Uploading Your Profile")
        dialog1.setCancelable(false)
        dialog1.show()

        val profileImageRefs = storage.child("profileImages/$randomString")

        val baos = ByteArrayOutputStream()
        rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val rotBytes = baos.toByteArray()
        val uploadTask = profileImageRefs.putBytes(rotBytes).addOnCompleteListener {
            dialog1.cancel()
            dialog2.setMessage("Done!").setCancelable(false)
            dialog2.setPositiveButton("Great!") { dialog, _ ->
                if (oldPath != "img_avatar.jpg") {
                    val refToDelete = storage.child("profileImages/$oldPath")
                    refToDelete.delete()
                }
                dialog.dismiss()
                navigateMyProfile()
            }
            dialog2.show()
        }

        uploadTask
    }

    private fun setNewUser() {
        oldPath = userModel.user.value?.photoName.toString()
        if (userModel.user.value?.location != null)
            existingLocation = userModel.user.value?.location
        if (userModel.user.value?.address != null)
            existingAddress = userModel.user.value?.address

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val newUser = UserModel(
            name.text.toString(),
            nickname.text.toString(),
            email.text.toString(),
            existingLocation!!,
            randomString,
            userId,
            token,
            counterFeedback,
            rating,
            existingAddress!!
        )
        userModel.oldNick.value = nickname.text.toString()
        this.saveOnViewModel()
        // Reset the old path, we don't need it anymore
        userModel.photo = MutableLiveData()
        firestoreViewModel.saveUserToFirestore(newUser)
        val user: MutableLiveData<UserModel> = MutableLiveData(newUser)
        firestoreViewModel.createdUserLiveData = user
    }

    private fun navigateMyProfile() {
        // Return back to profile page
        findNavController().popBackStack()
    }

}
