package com.splunk.android.sr.testapp.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.splunk.android.sr.testapp.ui.SampleFragment
import com.splunk.android.sr.testapp.util.randomColor

class ViewPagerAdapter2(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 10
    }

    override fun createFragment(position: Int): Fragment {
        return SampleFragment.create("Title $position", "Description $position", randomColor())
    }
}
