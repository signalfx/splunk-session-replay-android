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

package com.splunk.android.instrumentation.recording.core.data

internal enum class RenderingDataSource(internal val code: String) {
    NATIVE("NATIVE"),

    WIREFRAME("WIREFRAME");

    internal companion object {

        @JvmStatic
        fun fromString(code: String): RenderingDataSource {
            return when (code) {
                NATIVE.code -> NATIVE
                WIREFRAME.code -> WIREFRAME
                else -> NATIVE
            }
        }
    }
}

internal fun List<RenderingDataSource>.containsNative(): Boolean =
    any { it == RenderingDataSource.NATIVE }

internal fun List<RenderingDataSource>.containsWireframe(): Boolean =
    any { it == RenderingDataSource.WIREFRAME }
