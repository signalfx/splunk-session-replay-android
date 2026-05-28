-dontwarn com.splunk.android.instrumentation.recording.screenshot.**

-keepclassmembers class io.flutter.embedding.android.FlutterSurfaceView {
    private final boolean renderTransparently;
}