package com.xenous.athenekotlin.views.adapters

import com.xenous.athenekotlin.data.Category

interface OnItemClickListener {
    fun onClick(view: android.view.View, category: Category)
}