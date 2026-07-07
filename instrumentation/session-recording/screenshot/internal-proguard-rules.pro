# Internal API. See internal-api module for more details.

# PreferencesExt.videoBitrate
-keepnames class com.splunk.android.common.encoder.Encoder

-keepclassmembers class com.splunk.android.common.encoder.Encoder {
    java.lang.Integer bitrateOverride;
}

-dontwarn java.lang.invoke.StringConcatFactory