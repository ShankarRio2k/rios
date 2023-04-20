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
import com.example.rios.databinding.ActivityMainBinding
import com.example.rios.tabs.settings
import com.example.rios.tabs.shots
import com.example.rios.tabs.surf
import com.example.rios.tabs.talks
import com.example.rios.utils.SharedPrefernceHelper
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


private var mFirebaseAnalytics: FirebaseAnalytics? = null

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var pagerAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        pagerAdapter = PagerAdapter(supportFragmentManager)
        binding.viewpager.adapter = pagerAdapter
        binding.tablayou.setupWithViewPager(binding.viewpager)

        // Set the titles for the first three tabs
        binding.tablayou.getTabAt(0)?.text = "Talks"
        binding.tablayou.getTabAt(1)?.text = "Surf"
        binding.tablayou.getTabAt(2)?.text = "Shots"

        // Set a custom view for the fourth tab with a smaller icon
        val tab = binding.tablayou.getTabAt(3)
        tab?.setCustomView(R.layout.settingstab)

        val imageView = tab?.view?.findViewById<ImageView>(R.id.tab_icon)
        val user = FirebaseAuth.getInstance().currentUser

        val (username, bio, profileImageUrl) = SharedPrefernceHelper(this).getUserDetails()

        if (profileImageUrl != null){
            imageView?.let {
                Picasso.get().load(profileImageUrl).into(it)
            }
        }else {
            // Get the user's photo URI
            val photoUri = user?.photoUrl
            // Load the photo into an ImageView using Picasso
            imageView?.let {
                Picasso.get().load(photoUri).into(it)
            }
        }
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
}

//        /        btnSignOut.setOnClickListener {
//            firebaseAuth.signOut()
//            startActivity(Intent(this, Createaccount::class.java))
//            toast("signed out")
//            finish()
//        }


