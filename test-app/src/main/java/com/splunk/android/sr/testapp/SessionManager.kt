package com.splunk.android.sr.testapp

import com.splunk.android.common.id.NanoId
import kotlin.collections.get

class SessionManager {

    val sessions = mutableMapOf<Long, String>()

    fun createNewSession() {
        sessions[System.currentTimeMillis()] = "Android-" + NanoId.generate()
    }

    fun loadSession(time: Long): String? {
        val key = sessions.keys.sorted().firstOrNull { it <= time }
        return sessions[key]
    }
}
