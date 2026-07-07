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

package com.splunk.android.sr.testapp.ui.compose

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.util.sessionReplay

class VariantsActivity : ComponentActivity() {

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activities = getActivitiesByPackage(this, "com.splunk.android.sr.testapp.ui.compose.variants")

        setContent {
            Content(activities = activities)
        }
    }
}

@Suppress("UNCHECKED_CAST", "SameParameterValue")
private fun getActivitiesByPackage(context: Context, packageName: String): List<Class<out Activity>> {
    val result = mutableListOf<Class<out Activity>>()

    try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
        val activities = packageInfo.activities ?: return emptyList()

        for (activityInfo in activities)
            if (activityInfo.packageName == packageName)
                result += activityInfo.name.toClass() as? Class<Activity> ?: continue
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }

    return result
}

@Composable
private fun Content(activities: List<Class<out Activity>>) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    if (activities.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = context.getString(R.string.compose_variant_no_activities),
                style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            items(activities.size) {
                val activityClass = activities[it]

                val label = remember(activityClass) {
                    try {
                        val componentName = ComponentName(context, activityClass)
                        val info = packageManager.getActivityInfo(componentName, 0)
                        info.loadLabel(packageManager).toString()
                    } catch (_: PackageManager.NameNotFoundException) {
                        activityClass.simpleName
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .heightIn(min = 35.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    colorResource(id = R.color.blue),
                                    colorResource(id = R.color.blue_light)
                                )
                            )
                        )
                        .clickable {
                            context.startActivity(Intent(context, activityClass))
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = label,
                        modifier = Modifier.sessionReplay(
                            isSensitive = false
                        ),
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}
