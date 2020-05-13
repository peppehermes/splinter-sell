package it.polito.mad.splintersell.ui.item_details

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.media.ExifInterface
import android.widget.Spinner
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputFilter
import android.view.animation.Transformation
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.PopupMenu
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import it.polito.mad.splintersell.ItemDB
import it.polito.mad.splintersell.R
import kotlinx.android.synthetic.main.fragment_edit_item.*
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

const val REQUEST_TAKE_PHOTO = 2
const val GALLERY_REQUEST_CODE = 3
var rotatedBitmap: Bitmap? = null
var photoFile: File? = null
var photoURI: Uri? = null
var filename: String? = null

val db = FirebaseFirestore.getInstance()
val storage = FirebaseStorage.getInstance().reference
val user = Firebase.auth.currentUser

class ItemEditFragment : Fragment() {
    private val args: ItemEditFragmentArgs by navArgs()
    private lateinit var currentPhotoPath: String
    private var index: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_item, container, false)
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.setInputLimits()

        index = args.itemId
        //Log.e("ID", index.toString())
        this.showDate()
        this.restoreImage(savedInstanceState)
        this.imageButtonMenu()
        if (index != -1) {
            this.populateEditText()
            this.retrieveImage()
        }
        val adapter = ArrayAdapter(
            activity?.applicationContext!!,R.layout.spinner_text,
            resources.getStringArray(R.array.macroCategories))

        dropdow_main_category!!.setAdapter(adapter)
        dropdow_main_category.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            when(position) {

                0 -> dropdow_sub_category!!.setAdapter(ArrayAdapter(
                    activity?.applicationContext!!, R.layout.spinner_text,
                    resources.getStringArray(R.array.arts))
                )
                1 -> dropdow_sub_category!!.setAdapter(ArrayAdapter(
                    activity?.applicationContext!!, R.layout.spinner_text,
                    resources.getStringArray(R.array.sports))
                )
                2 -> dropdow_sub_category!!.setAdapter(ArrayAdapter(
                    activity?.applicationContext!!, R.layout.spinner_text,
                    resources.getStringArray(R.array.baby))
                )
                3 -> dropdow_sub_category!!.setAdapter(ArrayAdapter(
                    activity?.applicationContext!!, R.layout.spinner_text,
                    resources.getStringArray(R.array.women))
                )
                4 -> dropdow_sub_category!!.setAdapter(ArrayAdapter(
                    activity?.applicationContext!!, R.layout.spinner_text,
                    resources.getStringArray(R.array.men))
                )
                5 -> dropdow_sub_category!!.setAdapter(ArrayAdapter(
                    activity?.applicationContext!!, R.layout.spinner_text,
                    resources.getStringArray(R.array.electronics))
                )
                6 -> dropdow_sub_category!!.setAdapter(ArrayAdapter(
                    activity?.applicationContext!!, R.layout.spinner_text,
                    resources.getStringArray(R.array.games))
                )
                7 -> dropdow_sub_category!!.setAdapter(ArrayAdapter(
                    activity?.applicationContext!!, R.layout.spinner_text,
                    resources.getStringArray(R.array.automotive))
                )

            }
        }
    }


    //Limits the lenght of the input of the EditText fields
    private fun setInputLimits(){

        title.filters = arrayOf(InputFilter.LengthFilter(30))
        description.filters = arrayOf(InputFilter.LengthFilter(50))
        location.filters = arrayOf(InputFilter.LengthFilter(30))

    }

    private fun restoreImage(savedInstanceState: Bundle?) {

        photoURI = savedInstanceState?.getString("imgUri")?.let { Uri.parse(it) }
        photoURI?.run {
            manageBitmap()
        }
    }

    private fun retrieveImage() {
        val file = File(activity?.filesDir, filename!!)
        val fileExists = file.exists()

        if (fileExists) {
            val fis: FileInputStream = requireActivity().openFileInput(filename)
            val bitmap = BitmapFactory.decodeStream(fis)
            fis.close()
            detail_image.setImageBitmap(bitmap)
        }
    }

    private fun showDate() {
        //expire_date.setText(SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis()))
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        expire_date.setOnClickListener {
            DatePickerDialog(
                requireActivity().window.context,
                DatePickerDialog.OnDateSetListener { it, year, monthOfYear, dayOfMonth ->
                    // Display Selected date in TextView
                    var monthConverted = "" + (monthOfYear + 1).toString()
                    var dayConverted = "" + dayOfMonth.toString()
                    if (monthOfYear < 10) monthConverted = "0$monthConverted"
                    if (dayOfMonth < 10) dayConverted = "0$dayConverted"

                    val date = "$dayConverted/$monthConverted/$year"
                    expire_date.setText(date)
                },
                year,
                month,
                day
            ).show()
        }
    }

    //TODO: fix retrieving data of the Item from DB (ItemEdit)
    private fun populateEditText() {
        filename = index.toString()

        val itemName: String = user!!.uid+"_"+index.toString()

        val docRef = db.collection("users")
            .document(user.uid)
            .collection("items")
            .document(itemName)

        docRef.get()
            .addOnSuccessListener {

                    res ->
                if(res.exists()){
                    val itemData: ItemDB? = res.toObject(ItemDB::class.java)
                    Log.d("ItemEditTAG", "Success in retrieving data: "+ res.toString())

                    Log.d("ItemEditTAG", itemData.toString())

                    if(itemData!!.title != "")
                        title.setText(itemData!!.title)
                    if(itemData.description != "")
                        description.setText(itemData.description)
                    if(itemData!!.price != "")
                        price.setText(itemData!!.price)
                    if(itemData!!.location != "")
                        location.setText(itemData!!.location)
                    if(itemData!!.expire_date != "")
                        expire_date.setText(itemData!!.expire_date)

                }
                else
                    Log.d("ItemEditTAG", "No document retrieved")


            }
            .addOnFailureListener{
                Log.d("ItemEditTAG", "Error in retrieving data")
            }




        /*

        val sharedPref: SharedPreferences =
            requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return

        if (sharedPref.contains(index.toString())) {

            val info: String? = sharedPref.getString(index.toString(), null)

            val jasonObject = JSONObject(info!!)
            val editTitle: String
            val editDescription: String
            val editPrice: String
            val editLocation: String
            val editDate: String

            editTitle = if (jasonObject.has("Title"))
                jasonObject.getString("Title")
            else
                ""

            editDescription = if (jasonObject.has("Description"))
                jasonObject.getString("Description")
            else
                ""
            editPrice = if (jasonObject.has("Price"))
                jasonObject.getString("Price")
            else
                ""

            editLocation = if (jasonObject.has("Location"))
                jasonObject.getString("Location")
            else
                ""

            editDate = if (jasonObject.has("Expire_Date"))
                jasonObject.getString("Expire_Date")
            else
                ""

            title.setText(editTitle)
            description.setText(editDescription)
            price.setText(editPrice)
            location.setText(editLocation)
            expire_date.setText(editDate)
        }

         */
    }

    private fun imageButtonMenu() {
        // Show menu when tapping on imagebutton

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
        if (imageIntent.resolveActivity(requireActivity().packageManager) != null)
            startActivityForResult(
                imageIntent,
                GALLERY_REQUEST_CODE
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
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        photoURI
                    )
                    startActivityForResult(
                        takePictureIntent,
                        REQUEST_TAKE_PHOTO
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            REQUEST_TAKE_PHOTO -> {
                Log.e("LOG", "$data")
                Log.e("photo", "path: ${photoFile?.absolutePath}")

                val bmOptions = BitmapFactory.Options()
                BitmapFactory.decodeFile(
                    photoFile?.absolutePath, bmOptions
                )?.run {
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

    private fun manageBitmap() {
        var bitmap = handleSamplingAndRotationBitmap(
            requireContext(),
            photoURI
        )
        bitmap = CropSquareTransformation()
            .transform(bitmap!!)
        detail_image.setImageBitmap(bitmap)
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
            Log.d("MY_TEST", currentPhotoPath)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Do something that differs the Activity's menu here
        inflater.inflate(R.menu.save_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("WrongThread")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveProfile -> {
                //Save bitmap
                if (rotatedBitmap != null) {
                    val fos: FileOutputStream =
                        requireActivity().openFileOutput(filename, Context.MODE_PRIVATE)
                    rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 75, fos)
                    fos.close()
                }

                //Save image on Cloud Storage

                var profileRefs = storage.child("itemImages")
                val profileImageName = storage.child(user!!.uid+"_"+index+".jpg")
                val profileImageRefs= storage.child("itemImages/"+user!!.uid+"_"+index+".jpg")
                Log.d("ItemEditTAG", "Name of the file to be stored: $profileImageRefs")

                if(rotatedBitmap!=null){
                    val baos = ByteArrayOutputStream()
                    rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                    val rotBytes = baos.toByteArray()

                    val uploadTask = profileImageRefs.putBytes(rotBytes)
                    uploadTask.addOnFailureListener {
                        Log.d("ItemEditTAG", "Error in saving image to the Cloud Storage")
                    }.addOnSuccessListener {
                        Log.d("ItemEditTAG", "Success in saving image to the Cloud Storage")
                    }

                }

                rotatedBitmap = null

                insertIntoDB()



                // Create JSON Object and fill it with data to store
                val rootObject = JSONObject()

                if (!title.text.isNullOrEmpty())
                    rootObject.accumulate("Title", title.text)

                if (!description.text.isNullOrEmpty())
                    rootObject.accumulate("Description", description.text)

                if (!price.text.isNullOrEmpty())
                    rootObject.accumulate("Price", price.text)

                if (!dropdow_sub_category.text.isNullOrEmpty())
                    rootObject.accumulate("Category", dropdow_main_category.text.toString() + ": " + dropdow_sub_category.text.toString())

                if (!location.text.isNullOrEmpty())
                    rootObject.accumulate("Location", location.text)

                if (!expire_date.text.isNullOrEmpty())
                    rootObject.accumulate("Expire_Date", expire_date.text.toString())

                val sharedPref: SharedPreferences =
                    requireActivity().getPreferences(Context.MODE_PRIVATE)

                with(sharedPref.edit()) {
                    putString(index.toString(), rootObject.toString())
                    apply()

                    val action = ItemEditFragmentDirections.goToDetails(args.itemId)
                    Navigation.findNavController(requireView()).navigate(action)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun insertIntoDB() {

        val itemName: String = user!!.uid.toString() + "_" + index.toString()

        val newItem = ItemDB(
            index!!, title.text.toString(),
            description.text.toString(), price.text.toString(),
            dropdow_main_category.text.toString(), dropdow_sub_category.text.toString(),
            location.text.toString(), expire_date.text.toString()
        )

        val docRef = db.collection("users")
            .document(user.uid)
            .collection("items")
            .document(itemName)

        Log.d("SignInTAG", docRef.toString())

        docRef.get()
            .addOnSuccessListener {

                    res ->
                if (!res.exists()) {  //new item created

                    db.collection("users")
                        .document(user.uid)
                        .collection("items")
                        .document(itemName)
                        .set(newItem)
                        .addOnSuccessListener {
                            Log.d("ItemEditTAG", "Instance succesfully created!")
                        }
                        .addOnFailureListener {
                            Log.d("ItemEditTAG", "Error in creating new instance")
                        }

                } else {  //update item


                    db.collection("users")
                        .document(user.uid)
                        .collection("items")
                        .document(itemName)
                        .set(newItem)
                        .addOnSuccessListener {
                            Log.d("ItemEditTAG", "Instance succesfully updated!")
                        }
                        .addOnFailureListener {
                            Log.d("ItemEditTAG", "Error in updating instance")
                        }


                }

            }
            .addOnFailureListener {
                Log.d("ItemEditTAG", "Error in reading the DB")
            }

    }




    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        photoURI?.run {
            outState.putString("imgUri", this.toString())
        }
    }
}







