package inc.ahmedmourad.sherlock.utils

import android.util.SparseArray

import io.reactivex.disposables.Disposable

internal class DisposablesSparseArray : SparseArray<Disposable>() {

    fun dispose() {
        for (i in 0 until size())
            valueAt(i).dispose()
        clear()
    }

    fun dispose(keys: IntArray) {
        for (key in keys) {
            dispose(key)
            remove(key)
        }
    }

    override fun put(key: Int, value: Disposable) {
        dispose(key)
        super.put(key, value)
    }

    private fun dispose(key: Int) {
        get(key)?.dispose()
    }
}
