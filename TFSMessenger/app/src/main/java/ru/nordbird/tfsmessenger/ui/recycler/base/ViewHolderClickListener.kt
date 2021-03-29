package ru.nordbird.tfsmessenger.ui.recycler.base

import android.view.View

interface ViewHolderClickType

interface ViewHolderClickListener {

    fun onViewHolderClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType? = null)

    fun onViewHolderLongClick(holder: BaseViewHolder<*>, view: View): Boolean
}