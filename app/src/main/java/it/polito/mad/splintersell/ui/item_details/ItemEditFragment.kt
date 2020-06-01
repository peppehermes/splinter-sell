package it.polito.mad.splintersell.ui.item_details

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputFilter
import android.util.Log
import android.view.*
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.ItemModel
import it.polito.mad.splintersell.data.storage
import kotlinx.android.synthetic.main.fragment_edit_item.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

const val REQUEST_TAKE_PHOTO = 2
const val GALLERY_REQUEST_CODE = 3


class ItemEditFragment : Fragment() {
    private val TAG = "ITEM_EDIT"
    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private var path: String = ""
    private var oldPath: String = ""
    private var randomString: String = ""
    private var isImage: Boolean = false
    private var rotatedBitmap: Bitmap? = null
    private var photoFile: File? = null
    private var photoURI: Uri? = null

    private var existingLocation: String? = null
    private var existingAddress: GeoPoint? = null

    private val firestoreViewModel: FirestoreViewModel by viewModels()

    private val args: ItemEditFragmentArgs by navArgs()
    private lateinit var currentPhotoPath: String
    private val user = Firebase.auth.currentUser
    private lateinit var liveData: LiveData<ItemModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        firestoreViewModel.fetchSingleItemFromFirestore(args.documentName)
        liveData = firestoreViewModel.item

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_item, container, false)
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var savedTitle: String? = null
        var savedDescription: String? = null
        var savedPrice: String? = null
        var savedDate: String? = null
        var savedImg: String? = null

        savedInstanceState?.run {
            savedTitle = savedInstanceState.get(getString(R.string.title)).toString()
            savedDescription = savedInstanceState.get(getString(R.string.description)).toString()
            savedPrice = savedInstanceState.get(getString(R.string.price)).toString()
            savedDate = savedInstanceState.get(getString(R.string.expire_date)).toString()
            oldPath = savedInstanceState.get("oldPath").toString()
            Log.d("onsave",oldPath)
            // If an image has been taken , retrieve Uri
            if (savedInstanceState.get("imgUri").toString() != "null")
                savedImg = savedInstanceState.get("imgUri").toString()
        }

        this.setInputLimits()
        this.showDate()
        this.imageButtonMenu()

        if (liveData.value == null) {
            if (savedImg != null)
                this.restoreImage(savedImg)
        }

        liveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            if (savedTitle == null)
                til_title.editText!!.setText(it.title)
            else
                til_title.editText!!.setText(savedTitle)

            if (savedDescription == null)
                til_description.editText!!.setText(it.description)
            else
                til_description.editText!!.setText(savedDescription)

            if (savedPrice == null)
                til_price.editText!!.setText(it.price)
            else
                til_price.editText!!.setText(savedPrice)

            // Check if a location already exists
            if(it.location != null){
                existingLocation = it.location
                existingAddress = it.address
            }

            if (savedDate == null)
                til_expire_date.editText!!.setText(it.expireDate)
            else
                til_expire_date.editText!!.setText(savedDate)

            if (savedImg == null) {

                path = it.imgPath
                isImage = if (path == "") {
                    image.setImageDrawable(
                        requireContext().getDrawable(R.drawable.image_vectorized_lower)
                    )

                    // Set the check for the successive from validation
                    false
                } else {
                    Glide.with(requireContext()).using(FirebaseImageLoader())
                        .load(storage.child("/itemImages/$path")).into(image)

                    // Add on click listener to see full size image

                    detail_image.isClickable = true
                    detail_image.isFocusable = true
                    detail_image.setOnClickListener {
                        val action = ItemEditFragmentDirections.fullScreenImage(path)
                        findNavController().navigate(action)
                    }

                    // Set the check for the successive from validation
                    true
                }
            } else {
                this.restoreImage(savedImg)
            }
        })

        manageSpinner()

    }

    private fun manageSpinner() {
        val adapter = ArrayAdapter(
            this.requireContext(),
            R.layout.spinner_text,
            resources.getStringArray(R.array.macroCategories)
        )

        dropdown_main_category!!.setAdapter(adapter)
        dropdown_main_category.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                when (position) {

                    0 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.arts)
                        )
                    )
                    1 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.sports)
                        )
                    )
                    2 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.baby)
                        )
                    )
                    3 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.women)
                        )
                    )
                    4 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.men)
                        )
                    )
                    5 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.electronics)
                        )
                    )
                    6 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.games)
                        )
                    )
                    7 -> dropdown_sub_category!!.setAdapter(
                        ArrayAdapter(
                            this.requireContext(),
                            R.layout.spinner_text,
                            resources.getStringArray(R.array.automotive)
                        )
                    )

                }
            }
    }


    //Limits the lenght of the input of the EditText fields
    private fun setInputLimits() {

        til_title.editText!!.filters = arrayOf(InputFilter.LengthFilter(30))
        til_description.editText!!.filters = arrayOf(InputFilter.LengthFilter(100))

    }

    private fun restoreImage(savedImg: String?) {

        photoURI = savedImg?.let { Uri.parse(it) }
        photoURI?.run {
            manageBitmap()
        }
    }


    private fun showDate() {
        //expire_date.setText(SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis()))
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var datevalid: Boolean

        expire_date.setOnClickListener {
            DatePickerDialog(
                requireActivity().window.context,
                DatePickerDialog.OnDateSetListener { _, years, monthOfYear, dayOfMonth ->
                    // Display Selected date in TextView
                    datevalid = validateDate(years, monthOfYear, dayOfMonth)
                    if (datevalid) {
                        var monthConverted = "" + (monthOfYear + 1).toString()
                        var dayConverted = "" + dayOfMonth.toString()
                        if (monthOfYear < 10) monthConverted = "0$monthConverted"
                        if (dayOfMonth < 10) dayConverted = "0$dayConverted"

                        val date = "$dayConverted/$monthConverted/$years"
                        til_expire_date.editText!!.setText(date)
                    } else Snackbar.make(
                        this.requireView(), getString(R.string.wrong_date), Snackbar.LENGTH_SHORT
                    ).show()
                },
                year,
                month,
                day
            ).show()
        }
    }

    private fun imageButtonMenu() {
        // Show show_profile_menu when tapping on imagebutton

        select_photo.setOnClickListener {
            val popupMenu = PopupMenu(requireActivity().applicationContext, it)
            popupMenu.inflate(R.menu.popup_menu)
            popupMenu.show()
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
            imageIntent, GALLERY_REQUEST_CODE
        )
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
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT, photoURI
                    )
                    startActivityForResult(
                        takePictureIntent, REQUEST_TAKE_PHOTO
                    )
                }
            }
        }
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

    private fun manageBitmap() {
        var bitmap = handleSamplingAndRotationBitmap(
            requireContext(), photoURI
        )
        bitmap = CropSquareTransformation().transform(bitmap!!)
        image.setImageBitmap(bitmap)
        rotatedBitmap = bitmap
        oldPath = path
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
        //Log.e("photo_orientation", ei.getAttribute(ExifInterface.TAG_ORIENTATION).toString())
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Do something that differs the Activity's show_profile_menu here
        inflater.inflate(R.menu.edit_item_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("WrongThread")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {

                hideKeyboardFrom(requireContext(), requireView())

                val checkError: Boolean = formValidation()

                if (!checkError) {  // All fields are declared


                    if (rotatedBitmap != null) {    // An image has been taken

                        randomString =
                            (1..20).map { _ -> kotlin.random.Random.nextInt(0, charPool.size) }
                                .map(charPool::get).joinToString("")

                        randomString = "$randomString.jpg"

                        setNewItem()

                        uploadImageOnStorage()


                    } else {     // No image taken

                        if (isImage) {    //There is an Image in the Storage

                            randomString = path

                            setNewItem()
                            val dialog = AlertDialog.Builder(requireContext())
                            dialog.setMessage("Done!").setCancelable(false)
                                .setPositiveButton("Great!") { dialogBox, _ ->
                                    dialogBox.dismiss()
                                    navigateMyItemDetails()
                                }
                            dialog.show()
                        } else {       // Apply Constraint
                            Snackbar.make(
                                this.requireView(),
                                "An image must be uploaded",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                    }
                } else {
                    Snackbar
                        .make(requireView(), R.string.please_fill_all, Snackbar.LENGTH_SHORT)
                        .show()
                }


                true
            }
            R.id.delete -> {

                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Are you sure you want to Delete?").setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        // Delete selected note from database

                        firestoreViewModel.updateStatus(
                            requireContext().getString(R.string.blocked), args.documentName
                        )

                        firestoreViewModel.cancelAllNotifications(args.documentName)

                        dialog.dismiss()

                        findNavController().popBackStack(R.id.nav_item_list, false)
                    }.setNegativeButton("No") { dialog, _ ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }

                val alert = builder.create()
                alert.show()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun formValidation(): Boolean {

        var result = false
        if (til_title.editText!!.text.isEmpty()) {
            til_title.editText!!.error = getString(R.string.please_fill)
            result = true
        }
        if (til_description.editText!!.text.isEmpty()) {
            til_description.editText!!.error = getString(R.string.please_fill)
            result = true
        }
        if (til_price.editText!!.text.isEmpty()) {
            til_price.editText!!.error = getString(R.string.please_fill)
            result = true
        }
        if (til_expire_date.editText!!.text.isEmpty()) {
            til_expire_date.editText!!.error = getString(R.string.please_fill)
            result = true
        }
        if (spinner_1.editText!!.text.isEmpty()) {
            spinner_1.editText!!.error = getString(R.string.please_select)
            result = true
        }
        if (spinner_2.editText!!.text.isEmpty()) {
            spinner_2.editText!!.error = getString(R.string.please_select)
            result = true
        }

        return result

    }

    private fun validateDate(years: Int, monthOfYear: Int, dayOfMonth: Int): Boolean {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return !(years < year || ((years == year) && (monthOfYear < month)) || ((years == year) && (monthOfYear == month) && dayOfMonth < day))
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (rotatedBitmap != null)

            photoURI?.run {
                outState.putString("imgUri", this.toString())
            }
        Log.d("oldpath",path)
        outState.putString("oldPath",path)
        outState.putString(getString(R.string.title), til_title.editText!!.text.toString())
        outState.putString(
            getString(R.string.description),
            til_description.editText!!.text.toString()
        )
        outState.putString(getString(R.string.price), til_price.editText!!.text.toString())
        outState.putString(
            getString(R.string.expire_date),
            til_expire_date.editText!!.text.toString()
        )
    }

    private fun uploadImageOnStorage() {

        val dialog1 = AlertDialog.Builder(requireContext()).create()
        val dialog2 = AlertDialog.Builder(requireContext())
        dialog1.setMessage("Uploading Your Item")
        dialog1.setCancelable(false)
        dialog1.show()

        val profileImageRefs = storage.child("itemImages/$randomString")

        val baos = ByteArrayOutputStream()
        rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val rotBytes = baos.toByteArray()
        profileImageRefs.putBytes(rotBytes).addOnCompleteListener {
            dialog1.cancel()
            dialog2.setMessage("Done!").setCancelable(false)
            dialog2.setPositiveButton("Great!") { dialog, _ ->
                rotatedBitmap = null
                Log.d("deleteOfFile", "old path:  $oldPath")

                if (oldPath != "") {
                    val refToDelete = storage.child("itemImages/$oldPath")
                    refToDelete.delete().addOnSuccessListener {
                        Log.d("deleteOfFile", "Delete complete on item $oldPath")
                    }.addOnFailureListener {
                        Log.d("deleteOfFile", "Delete failed")
                    }
                }
                dialog.dismiss()
                navigateMyItemDetails()
            }
            dialog2.show()


        }.addOnFailureListener {
            Log.d("ItemEditTAG", "Error in saving image to the Cloud Storage")
        }.addOnSuccessListener {
            Log.d("ItemEditTAG", "Success in saving image to the Cloud Storage")
        }


    }

    private fun setNewItem() {

        oldPath = path

        val newItem : ItemModel

        if(existingLocation == null){
            newItem = ItemModel(
                til_title.editText!!.text.toString(),
                til_description.editText!!.text.toString(),
                til_price.editText!!.text.toString(),
                dropdown_main_category.text.toString(),
                dropdown_sub_category.text.toString(),
                "Null Location",
                GeoPoint(0.0, 0.0),
                til_expire_date.editText!!.text.toString(),
                args.documentName,
                user!!.uid,
                randomString,
                requireContext().getString(R.string.available)
            )
        }
        else{
            newItem = ItemModel(
                til_title.editText!!.text.toString(),
                til_description.editText!!.text.toString(),
                til_price.editText!!.text.toString(),
                dropdown_main_category.text.toString(),
                dropdown_sub_category.text.toString(),
                existingLocation!!,
                existingAddress!!,
                til_expire_date.editText!!.text.toString(),
                args.documentName,
                user!!.uid,
                randomString,
                requireContext().getString(R.string.available)
            )

        }




        firestoreViewModel.saveItemToFirestore(newItem)

    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun navigateMyItemDetails() {
        val action =
            ItemEditFragmentDirections.returnToItemDetails(args.documentName, false)
        findNavController().navigate(action)

    }
}