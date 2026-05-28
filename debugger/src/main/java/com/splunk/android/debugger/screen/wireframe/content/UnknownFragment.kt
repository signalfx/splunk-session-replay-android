package com.splunk.android.debugger.screen.wireframe.content

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.splunk.android.debugger.R
import com.splunk.android.debugger.databinding.SldFragmentWireframeUnknownBinding
import com.splunk.android.debugger.databinding.SldItemUnknownClassBinding
import com.splunk.android.instrumentation.recording.wireframe.model.ClassDefinition

internal class UnknownFragment : Fragment() {

    private var viewBinding: SldFragmentWireframeUnknownBinding? = null

    private val unknownClasses: List<ClassDefinition> by lazy { arguments?.getParcelableArrayList(ARG_UNKNOWN_CLASSES)!! }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = SldFragmentWireframeUnknownBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewBinding = viewBinding ?: return
        viewBinding.list.adapter = ClassesAdapter(unknownClasses, requireContext())
        viewBinding.list.onItemClickListener = onItemClickListener

        if (unknownClasses.isEmpty())
            viewBinding.noUnknown.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        viewBinding = null
        super.onDestroyView()
    }

    private fun copy(unknownClass: ClassDefinition) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.sld_wireframe_unknown_classes_clipboard_label), unknownClass.className)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), R.string.sld_wireframe_unknown_classes_clipboard_copied, Toast.LENGTH_SHORT).show()
    }

    private val onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        copy(unknownClasses[position])
    }

    private class ClassesAdapter(
        private val items: List<ClassDefinition>,
        context: Context
    ) : ArrayAdapter<List<String>>(context, R.layout.sld_item_unknown_class) {

        private val inflater = LayoutInflater.from(context)

        override fun getCount(): Int {
            return items.size
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val item = items[position]

            val viewBinding = SldItemUnknownClassBinding.inflate(inflater, parent, false)
            viewBinding.className.text = item.className
            viewBinding.ancestors.text = item.ancestors.joinToString("\n") { it }
            viewBinding.root.alpha = if (item.isInternal) 0.3f else 1f

            return viewBinding.root
        }
    }

    companion object {

        private const val ARG_UNKNOWN_CLASSES = "ARG_UNKNOWN_CLASSES"

        fun create(unknownClasses: List<ClassDefinition>): UnknownFragment {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ARG_UNKNOWN_CLASSES, ArrayList(unknownClasses))

            val fragment = UnknownFragment()
            fragment.arguments = bundle

            return fragment
        }
    }
}
