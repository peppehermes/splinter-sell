package it.polito.mad.splintersell.ui

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.ItemModelHolder

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
