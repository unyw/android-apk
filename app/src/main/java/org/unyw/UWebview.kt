package org.unyw

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebView
import android.view.inputmethod.BaseInputConnection

import android.view.inputmethod.EditorInfo

import android.view.inputmethod.InputConnection




class UWebview @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : WebView(context, attrs, defStyleAttr) {
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Log.d("UNYW_KEYW", event?.toString() ?: "null")
        return super.dispatchKeyEvent(event)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection? {
        return BaseInputConnection(this, false)
    }

}