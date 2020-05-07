package dev.ahmedmourad.sherlock.android.pickers.colors

import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import dev.ahmedmourad.sherlock.android.R

internal class ColorSelector<T : Enum<T>>(vararg items: Item<T>, default: T = items[0].id) {

    private val items = arrayOf(*items)

    private var selectedItemId = default
        set(value) {
            field = value
            onSelectionChangeListeners.forEach { it(selectedItemId) }
        }

    val onSelectionChangeListeners = mutableListOf<(T) -> Unit>()

    init {

        require(items.isNotEmpty()) {
            "The items list cannot be empty!"
        }

        requireNotNull(items.find { it.id == default }?.setSelected(true)) {
            "$default was not found in the items list!"
        }

        onSelectionChangeListeners.forEach { it(selectedItemId) }
    }

    fun select(itemId: T) {

        if (itemId == selectedItemId)
            return

        items.forEach {
            when (it.id) {
                selectedItemId -> it.setSelected(false)
                itemId -> it.setSelected(true)
            }
        }

        selectedItemId = itemId
    }

    class Item<T : Enum<T>> private constructor(internal val id: T, private val view: View, @ColorRes private val color: Int) {

        private val drawable = generateColoredCircleDrawable(color).also { view.background = it }

        fun setSelected(selected: Boolean) {
            drawable.setStroke(STROKE_WIDTH, ContextCompat.getColor(view.context,
                    if (selected)
                        STROKE_COLOR
                    else
                        android.R.color.transparent
            ))
        }

        private fun generateColoredCircleDrawable(@ColorRes color: Int) = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(ContextCompat.getColor(view.context, color))
            setStroke(STROKE_WIDTH, ContextCompat.getColor(view.context, android.R.color.transparent))
        }

        companion object {
            internal fun <T : Enum<T>> create(id: T, view: View, @ColorRes color: Int) = Item(id, view, color)
        }
    }

    companion object {

        private const val STROKE_WIDTH = 5
        private const val STROKE_COLOR = R.color.colorAccent

        fun <T : Enum<T>> newItem(id: T, view: View, @ColorRes color: Int): Item<T> {
            return Item.create(id, view, color)
        }
    }
}
