package com.splunk.android.sr.testapp

import android.util.Log
import com.splunk.android.common.http.HttpClient
import com.splunk.android.common.http.model.Header
import com.splunk.android.common.http.model.Response
import com.splunk.android.common.http.model.part.ByteArrayContent
import com.splunk.android.common.http.model.part.StringContent
import com.splunk.android.instrumentation.recording.core.api.DataListener
import com.splunk.android.instrumentation.recording.core.api.Metadata
import com.splunk.android.instrumentation.recording.core.api.SessionReplay

object Agent {

    val httpClient = HttpClient()
    val sessionManager = SessionManager()

    fun init() {
        sessionManager.createNewSession()

        SessionReplay.instance.dataListeners += object : DataListener {
            override fun onData(data: ByteArray, metadata: Metadata): Boolean {
                val sessionId = sessionManager.loadSession(metadata.startUnixMs)
                Log.d("AGENT", "onData for sessionId: $sessionId")
                httpClient.makePostRequest(
                    url = "https://session-replay-be.alfa.smartlook.cloud/sessions/$sessionId",
                    queries = emptyList(),
                    headers = listOf(
                        Header("MIME-Version", "1.0")
                    ),
                    contents = listOf(
                        ByteArrayContent(
                            dispositionName = "sessionReplayData",
                            dispositionFileName = "$sessionId",
                            type = "application/octet-stream",
                            bytes = data
                        ),
                        StringContent(
                            dispositionName = "sessionReplayMetadata",
                            dispositionFileName = null,
                            type = "application/json",
                            string = metadata.toJSONObject().toString()
                        )
                    ),
                    callback = object : HttpClient.Callback {
                        override fun onSuccess(response: Response) {
                            Log.d("AGENT", "onSuccess code: ${response.code}, body: ${response.body.toString(Charsets.UTF_8)}")
                        }

                        override fun onFailed(e: Exception) {
                            Log.d("AGENT", "onFailed exception: $e")
                        }
                    }
                )
                return true
            }
        }

        SessionReplay.instance.preferences.frameRate = 3
        SessionReplay.instance.start()
    }
}
