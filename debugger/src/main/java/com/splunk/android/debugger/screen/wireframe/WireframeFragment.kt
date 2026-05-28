package com.splunk.android.debugger.screen.wireframe

import androidx.fragment.app.Fragment

internal abstract class WireframeFragment : Fragment() {

    abstract var isControlsVisible: Boolean
}
