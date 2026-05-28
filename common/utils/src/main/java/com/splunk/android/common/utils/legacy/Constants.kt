package com.splunk.android.common.utils.legacy

internal object Constants {

    // When view doesn't have id set or service method, we are falling back to this constant
    const val UNKNOWN_VIEW_IDENTIFIER = "-"

    // TabLayout items usually don't have id so we are identifying them using complex identifier
    // defined by this template
    const val TAB_VIEW_IDENTIFIER_TEMPLATE = "%s position=[%s] tag=[%s]"
}
