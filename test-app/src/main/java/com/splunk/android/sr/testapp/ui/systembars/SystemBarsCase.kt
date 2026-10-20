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

package com.splunk.android.sr.testapp.ui.systembars

enum class SystemBarsCase(val title: String) {
    OPAQUE("Opaque bars"),
    EDGE_TO_EDGE("Edge-to-edge"),
    TRANSLUCENT("Translucent bars"),
    HIDDEN_STATUS_BAR("Hidden status bar"),
    HIDDEN_NAVIGATION_BAR("Hidden navigation bar"),
    HIDDEN_SYSTEM_BARS("Hidden system bars"),
    DIALOG("Dialog"),
    BOTTOM_SHEET("Bottom sheet"),
    KEYBOARD("Keyboard"),
    LANDSCAPE("Landscape"),
    DIALOG_ACTIVITY("Dialog Activity without other windows")
}
