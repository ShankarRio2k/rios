package com.example.rios.views

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.rios.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth


private var mFirebaseAnalytics: FirebaseAnalytics? = null

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        var viewPager: ViewPager = findViewById(R.id.viewpager)
        var tabLayout: TabLayout = findViewById(R.id.tablayou)
        val adapter = PagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        // Set the titles for the first three tabs
        tabLayout.getTabAt(0)?.text = "Talks"
        tabLayout.getTabAt(1)?.text = "Surf"
        tabLayout.getTabAt(2)?.text = "Shots"

        // Set a custom view for the fourth tab with a smaller icon
        val tab = tabLayout.getTabAt(3)
        tab?.setCustomView(R.layout.settingstab)
        val user = FirebaseAuth.getInstance().currentUser

// Get the user's photo URI
        val photoUri = user?.photoUrl

// Load the photo into an ImageView using Glide
        val imageView = tab?.view?.findViewById<ImageView>(R.id.tab_icon)
        if (imageView != null) {
            Glide.with(this)
                .load(photoUri)
                .placeholder(R.drawable.settings)
                .into(imageView)
        }

        // Set the icon for the fourth tab
//        val tabIcon = tab?.customView?.findViewById<CircleImageView>(R.id.tab_icon)
//
//        tabIcon?.setImageResource(R.drawable.settings)
    }

    private class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int = 4

        override fun getItem(position: Int): Fragment {
            // Return a Fragment for each position
            return when (position) {
                0 -> talks.newInstance("Talks")
                1 -> surf.newInstance("Surf")
                2 -> shots.newInstance("Shots")
                else -> settings.newInstance("")
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            // Set the title for each tab
            return when (position) {
                0 -> "Tab 1"
                1 -> "Tab 2"
                2 -> "Tab 3"
                else -> null
            }
        }
    }

    override fun onStart() {
        super.onStart()
//        val id = firebaseAuth.currentUser?.uid
//        val storageRef = FirebaseStorage.getInstance().reference
//        val imageRef = storageRef.child("profiles/${id}/profilePic")
//        Glide.with(this)
//            .load(u)
//            .placeholder(R.drawable.settings) // Placeholder image while the actual image is loading
//            .error(R.drawable.senderchat) // Image to display if there is an error loading the actual image
//            .into(tab_icon)
        // Get the current user

    }
}




//        /        btnSignOut.setOnClickListener {
//            firebaseAuth.signOut()
//            startActivity(Intent(this, Createaccount::class.java))
//            toast("signed out")
//            finish()
//        }


