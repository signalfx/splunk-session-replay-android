-repackageclasses 'com.splunk.android.instrumentation.recording.wireframe'

# Declared Canvas override method
-keepclassmembers class com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas {
    public int saveUnclippedLayer(int, int, int, int);
    public void restoreUnclippedLayer(int, android.graphics.Paint);
    public int save(int);
}

-keepclassmembers class com.splunk.android.instrumentation.recording.wireframe.canvas.LoggingCanvas {
    public int save(int);
}

-keepclassmembers class com.splunk.android.instrumentation.recording.wireframe.canvas.LoggingSkeletonCanvas {
    public int save(int);
}

-dontwarn java.lang.invoke.StringConcatFactory
-keep class **.R  { *; }