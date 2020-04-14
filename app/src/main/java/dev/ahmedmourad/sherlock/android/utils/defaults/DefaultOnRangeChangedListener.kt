package dev.ahmedmourad.sherlock.android.utils.defaults

import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar

internal interface DefaultOnRangeChangedListener : OnRangeChangedListener {

    override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

    }

    override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

    }
}
