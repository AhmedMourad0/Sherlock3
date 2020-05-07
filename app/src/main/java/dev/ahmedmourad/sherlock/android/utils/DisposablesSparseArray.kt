package dev.ahmedmourad.sherlock.android.utils

import android.util.SparseArray
import androidx.core.util.forEach

import io.reactivex.disposables.Disposable

internal class DisposablesSparseArray : SparseArray<Disposable>() {

    fun dispose() {
        this.forEach { _, disposable -> disposable.dispose() }
        clear()
    }

    fun dispose(keys: IntArray) {
        keys.forEach {
            dispose(it)
            remove(it)
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
