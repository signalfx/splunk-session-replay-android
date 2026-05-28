package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.webkit.WebView
import com.splunk.android.common.utils.Lock
import com.splunk.android.instrumentation.recording.wireframe.extension.WireframeView
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import com.splunk.android.instrumentation.recording.wireframe.util.FragmentConsumer
import com.splunk.android.instrumentation.recording.wireframe.util.ViewConsumer
import org.json.JSONArray

internal open class WebViewDescriptor : ViewDescriptor() {

    private val density = Resources.getSystem().displayMetrics.density

    override val intendedClass: Class<*>? = WebView::class.java

    override fun getType(view: View): Window.View.Type? {
        return Window.View.Type.WEB_VIEW
    }

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun describe(view: View, viewRect: Rect, clipRect: Rect, parentScaleX: Float, parentScaleY: Float, isParentSensitive: Boolean?, viewConsumer: ViewConsumer, fragmentConsumer: FragmentConsumer): Window.View {
        var description = super.describe(view, viewRect, clipRect, parentScaleX, parentScaleY, isParentSensitive, viewConsumer, fragmentConsumer)

        if (description.isSensitive == true || view !is WebView || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || !view.settings.javaScriptEnabled)
            return description

        val subviews = ArrayList<WireframeView>()
        val subviewsLock = Lock(true)

        description = description.copy(subviews = subviews, subviewsLock = subviewsLock)

        view.evaluateJavascript(SENSITIVE_ELEMENTS_JS) {
            try {
                val array = JSONArray(it)

                if (array.length() > 0) {
                    subviews.ensureCapacity(array.length())

                    for (i in 0 until array.length()) {
                        val json = array.getJSONObject(i)
                        val id = json.getString("id")
                        val rectArray = json.getJSONArray("rect")

                        val left = rectArray.getDouble(0).toInt()
                        val top = rectArray.getDouble(1).toInt()
                        val width = rectArray.getDouble(2).toInt()
                        val height = rectArray.getDouble(3).toInt()

                        val rect = Rect(left, top, left + width, top + height)

                        rect.scale(density)
                        rect.offset(description.rect.left, description.rect.top)

                        if (rect.intersect(description.rect))
                            subviews += createSensitiveView(id, rect, "${description.identity}_sensitivity")
                    }
                }
            } catch (_: Exception) { // Parse can failed, old versions of WebView can't execute our JS and returns unexpected results.
            }

            subviewsLock.unlock()
        }

        return description
    }

    private fun createSensitiveView(id: String, rect: Rect, identity: String): WireframeView {
        return WireframeView(
            id = id,
            name = null,
            rect = rect,
            type = null,
            typename = "WebView_SensitiveElement",
            hasFocus = false,
            offset = null,
            alpha = 1f,
            skeletons = null,
            foregroundSkeletons = null,
            subviews = null,
            identity = identity,
            isDrawDeterministic = true,
            isSensitive = true,
            subviewsLock = null
        )
    }

    private fun Rect.scale(scale: Float) {
        if (scale == 1f)
            return

        left = (left * scale).toInt()
        top = (top * scale).toInt()
        right = (right * scale).toInt()
        bottom = (bottom * scale).toInt()
    }

    private companion object { // FIXME sensitive element prefix

        const val SENSITIVE_ELEMENTS_JS = """var uniqueId = function (elem, prefix = '__smartlook_') {
  var id = elem.getAttribute('id');
  if (id) return id;
  var c = 0;
  do {
    id = prefix + c++;
  } while (document.getElementById(id) !== null);
  elem.setAttribute('id', id);
  return id;
};

var getHiddenRects = function () {
  var rectanglesToOverlay = [];
  var elementsToHide = Array.apply(null, document.querySelectorAll('input:not([type=\"button\"]):not([type=\"submit\"]):not(.smartlook-show)'));
  elementsToHide = elementsToHide.filter(function (elem) {return elem.closest('.smartlook-show') == null});
  elementsToHide = elementsToHide.concat(Array.apply(null,document.querySelectorAll('.smartlook-hide')));
  try {
    [].forEach.call(elementsToHide, function (elem, index) {
      var rect = elem.getBoundingClientRect();
      var id = uniqueId(elem);
      rectanglesToOverlay.push({
        id: id,
        rect: [rect.left, rect.top, rect.width, rect.height],
      });
    });
  } catch (e) {
    console.log(e);
  }
  return rectanglesToOverlay;
};

var rectanglesToOverlay = getHiddenRects();
rectanglesToOverlay;"""
    }
}
