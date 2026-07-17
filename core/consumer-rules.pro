-dontwarn com.splunk.android.instrumentation.recording.core.**

-keep public class com.splunk.android.instrumentation.recording.core.SessionReplayInstaller

# Keep Android media classes used for video encoding
-keep class android.media.** { *; }