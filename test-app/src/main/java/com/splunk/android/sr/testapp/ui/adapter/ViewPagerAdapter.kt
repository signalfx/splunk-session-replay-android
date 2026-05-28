package com.splunk.android.sr.testapp.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.splunk.android.sr.testapp.ui.SampleFragment
import com.splunk.android.sr.testapp.util.randomColor

class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return 10
    }

    override fun getItem(position: Int): Fragment {
        return SampleFragment.create("Title $position", "Description $position", randomColor())
    }
}
