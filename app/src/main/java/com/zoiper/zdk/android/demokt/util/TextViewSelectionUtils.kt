package com.zoiper.zdk.android.demokt.util

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.TypedValue
import android.widget.TextView
import com.zoiper.zdk.android.demokt.R

/**
 * TextViewSelectionUtils
 *
 * @since 4.2.2019 г.
 */
object TextViewSelectionUtils {

    /**
     * Selects textview by changing it icon and text colors to accent/white.
     *
     * @param view
     * The selected textview
     * @param selected
     * True if selected, false otherwise.
     */
    fun setTextViewSelected(view: TextView, selected: Boolean) {
        view.isSelected = selected
        val color: Int = if (selected) {
            getThemeAccentColor(view.context)
        } else {
            Color.WHITE
        }
        val drawables = view.compoundDrawables
        if (drawables[1] != null) {  // left drawable
            drawables[1].setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }
        view.setTextColor(color)
    }

    /**
     * Returns the current theme Accent color.
     * @param context
     * @return The current Accent color.
     */
    private fun getThemeAccentColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.color.colorAccent, value, true)
        return value.data
    }

}
