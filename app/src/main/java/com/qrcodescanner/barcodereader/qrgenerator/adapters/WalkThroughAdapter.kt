package com.qrcodescanner.barcodereader.qrgenerator.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.qrcodescanner.barcodereader.qrgenerator.fragments.WalkThroughFullScreenAdFragment
import com.qrcodescanner.barcodereader.qrgenerator.fragments.WalkThroughOneFragment
import com.qrcodescanner.barcodereader.qrgenerator.fragments.WalkThroughThreeFragment
import com.qrcodescanner.barcodereader.qrgenerator.fragments.WalkThroughTwoFragment
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper

class WalkThroughAdapter(fragmentActivity: FragmentActivity, val prefHelper: PrefHelper, val noOfFragments: Int) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return noOfFragments
    }

    override fun createFragment(position: Int): Fragment {
        val myReturnFragment: Fragment
        if (noOfFragments == 4) {
            myReturnFragment = when (position) {
                0 -> WalkThroughOneFragment(prefHelper)
                1 -> WalkThroughTwoFragment(prefHelper)
                2 -> WalkThroughFullScreenAdFragment(prefHelper)
                3 -> WalkThroughThreeFragment(prefHelper)
                else -> WalkThroughOneFragment(prefHelper)
            }
        } else {
            myReturnFragment = when (position) {
                0 -> WalkThroughOneFragment(prefHelper)
                1 -> WalkThroughTwoFragment(prefHelper)
                2 -> WalkThroughThreeFragment(prefHelper)
                else -> WalkThroughOneFragment(prefHelper)
            }
        }
        return myReturnFragment
    }
}
