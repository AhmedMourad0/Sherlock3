package dev.ahmedmourad.sherlock.android.adapters

import androidx.recyclerview.widget.RecyclerView

abstract class DynamicRecyclerAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    abstract fun update(items: T)
}
