package com.eduramza.cameratextconversor.utils

import android.content.Context
import androidx.annotation.StringRes

interface StringProvider {
    fun getString(@StringRes resId: Int): String
    fun getStringWithArgs(@StringRes resId: Int, vararg args: Any): String
}

class StringProviderImpl(private val context: Context): StringProvider {
    override fun getString(resId: Int): String {
        return context.getString(resId)
    }

    override fun getStringWithArgs(resId: Int, vararg args: Any): String {
        return context.getString(resId, args)
    }
}