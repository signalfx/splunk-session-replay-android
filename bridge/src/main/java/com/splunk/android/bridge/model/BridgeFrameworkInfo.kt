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
