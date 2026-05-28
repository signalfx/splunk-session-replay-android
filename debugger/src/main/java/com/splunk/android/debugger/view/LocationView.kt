/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.splunk.android.debugger.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.splunk.android.debugger.R
import com.splunk.android.debugger.databinding.SldViewLocationBinding
import com.splunk.android.debugger.extension.getEnum
import com.splunk.android.debugger.model.Location

internal class LocationView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val viewBinding = SldViewLocationBinding.inflate(LayoutInflater.from(context), this, true)

    private val views = viewBinding.run { arrayOf(topLeft, topRight, bottomLeft, bottomRight) }

    var location: Location? = null
        set(value) {
            field = value

            for (i in views.indices)
                views[i].isActivated = i == value?.ordinal
        }

    var listener: Listener? = null

    init {
        val onClickListener = OnClickListener()

        for (view in views)
            view.setOnClickListener(onClickListener)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.LocationView)
            location = a.getEnum<Location>(R.styleable.LocationView_location_location, null)
            a.recycle()
        }
    }

    private inner class OnClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            for (i in views.indices)
                if (views[i].id == v.id) {
                    location = Location.values()[i]
                    break
                }

            listener?.onLocationChanged(this@LocationView, location)
        }
    }

    interface Listener {
        fun onLocationChanged(view: LocationView, location: Location?)
    }
}
