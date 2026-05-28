package com.splunk.android.bridge.model

/**
 * @param framework framework name, for example React Native, Flutter, ...
 * @param frameworkPluginVersion bridge implementation version, for example 2.1
 * @param frameworkVersion framework version, example for Flutter is 3.7.0
 */
class BridgeFrameworkInfo(
    val framework: String?,
    val frameworkPluginVersion: String?,
    val frameworkVersion: String?
)
