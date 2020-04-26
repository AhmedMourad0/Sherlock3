package dev.ahmedmourad.sherlock.android.view

import android.content.Context
import android.util.AttributeSet
import android.widget.NumberPicker

import dev.ahmedmourad.sherlock.android.R
import timber.log.Timber
import timber.log.error

internal class ProperNumberPicker : NumberPicker {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        processXmlValues(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        processXmlValues(context, attrs, defStyleAttr)
    }

    private fun processXmlValues(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) {

        val attributes = context.theme.obtainStyledAttributes(attrs,
                R.styleable.ProperNumberPicker,
                defStyleAttr,
                0
        )

        try {

            minValue = attributes.getInt(R.styleable.ProperNumberPicker_minValue, 0)
            maxValue = attributes.getInt(R.styleable.ProperNumberPicker_maxValue, 100)
            value = attributes.getInt(R.styleable.ProperNumberPicker_value, 50)

        } catch (e: Exception) {
            Timber.error(e, e::toString)
        } finally {
            attributes.recycle()
        }
    }

    override fun getValue(): Int {
        clearFocus()
        return super.getValue()
    }
}
