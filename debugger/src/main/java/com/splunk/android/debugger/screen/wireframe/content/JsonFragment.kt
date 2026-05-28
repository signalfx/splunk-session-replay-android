package com.splunk.android.debugger.screen.wireframe.content

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.splunk.android.debugger.R
import com.splunk.android.debugger.databinding.SldFragmentWireframeJsonBinding
import com.splunk.android.debugger.screen.wireframe.WireframeFragment
import com.splunk.android.instrumentation.recording.wireframe.extension.toJSONObject
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

internal class JsonFragment : WireframeFragment() {

    private var viewBinding: SldFragmentWireframeJsonBinding? = null

    var wireframeScene: Wireframe.Frame.Scene = Companion.wireframeScene ?: error("start() not called")
        set(value) {
            field = value
            updateCodeView()
        }

    override var isControlsVisible: Boolean
        get() = viewBinding?.buttonsContainer?.visibility == View.VISIBLE
        set(value) {
            viewBinding?.buttonsContainer?.visibility = if (value) View.VISIBLE else View.GONE
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = SldFragmentWireframeJsonBinding.inflate(layoutInflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewBinding = viewBinding ?: return

        viewBinding.copy.setOnClickListener(onClickListener)
        updateCodeView()
    }

    override fun onDestroyView() {
        viewBinding = null
        super.onDestroyView()
    }

    private fun updateCodeView() {
        viewBinding?.prettyCode?.string = wireframeScene.toJSONObject().toString(3)
    }

    private fun save() {
        val json = wireframeScene.toJSONObject().toString(3)

        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.sld_wireframe_structure_copy_clipboard_label), json)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), R.string.sld_wireframe_structure_copy_copied_clipboard, Toast.LENGTH_SHORT).show()
    }

    private val onClickListener = View.OnClickListener {
        val viewBinding = viewBinding ?: return@OnClickListener

        when (it.id) {
            viewBinding.copy.id ->
                save()
        }
    }

    companion object {

        private var wireframeScene: Wireframe.Frame.Scene? = null

        fun create(wireframeScene: Wireframe.Frame.Scene): JsonFragment {
            Companion.wireframeScene = wireframeScene

            return JsonFragment()
        }
    }
}
