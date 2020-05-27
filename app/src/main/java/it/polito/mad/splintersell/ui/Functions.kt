package it.polito.mad.splintersell.ui

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.exifinterface.media.ExifInterface
import it.polito.mad.splintersell.MainActivity
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.ItemModelHolder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt

fun manageStatus(holder: ItemModelHolder, status: String) {
    val AVAILABLE = holder.itemView.context.getString(R.string.available)
    val BLOCKED = holder.itemView.context.getString(R.string.blocked)
    val SOLD = holder.itemView.context.getString(R.string.sold)

    when (status) {
        AVAILABLE -> {
            holder.button.text = holder.itemView.context.getString(R.string.edit)
            holder.button.setBackgroundColor(holder.itemView.context.getColor(R.color.colorPrimary))
            holder.card.isClickable = true
            val matrix = ColorMatrix()
            val filter = ColorMatrixColorFilter(matrix)
            holder.image.colorFilter = filter
        }
        BLOCKED -> {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)

            val filter = ColorMatrixColorFilter(matrix)

            holder.image.colorFilter = filter
            holder.button.text = BLOCKED
            holder.button.setTextColor(holder.itemView.context.getColor(R.color.colorOnPrimary))
            holder.button.isEnabled = false
            holder.button.setBackgroundColor(holder.itemView.context.getColor(R.color.colorSecondaryVariant))
            holder.card.isEnabled = false
        }
        SOLD -> {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)

            val filter = ColorMatrixColorFilter(matrix)

            holder.image.colorFilter = filter
            holder.button.text = SOLD
            holder.button.setTextColor(holder.itemView.context.getColor(R.color.colorOnPrimary))
            holder.button.isEnabled = false
            holder.button.setBackgroundColor(holder.itemView.context.getColor(R.color.colorError))
            holder.card.isEnabled = false
        }
    }
}

fun showSystemUI(activity: MainActivity) {
    activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    activity.drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    activity.supportActionBar?.show()
}

fun hideSystemUI(activity: MainActivity) {
    activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN)
    activity.drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    activity.supportActionBar?.hide()
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
    val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    img.recycle()
    return rotatedImg
}