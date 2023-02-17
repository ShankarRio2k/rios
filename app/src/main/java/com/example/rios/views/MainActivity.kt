package com.example.rios.views

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.rios.R
import com.example.rios.utils.Extensions.toast
import com.example.rios.utils.FirebaseUtils.firebaseAuth
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main.*

private var mFirebaseAnalytics: FirebaseAnalytics? = null

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        var viewPager: ViewPager = findViewById(R.id.viewpager)
        var tabLayout: TabLayout = findViewById(R.id.tablayou)

            val adapter = MyPagerAdapter(supportFragmentManager)
            viewPager.adapter = adapter
            tabLayout.setupWithViewPager(viewPager)

        }

        class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

            private val pages = listOf(
                Page(R.drawable.ic_baseline_chat_bubble_outline_24,"Talks", talks()),
                Page(R.drawable.ic_baseline_surfing_24,"surf", surf()),
                Page(R.drawable.ic_baseline_play_arrow_24,"Shots", shots()),
//                Page(R.drawable.setting,"", shots()),
            )

            override fun getItem(position: Int): Fragment {
                val page = pages[position]
                page.fragment.arguments = Bundle().apply {
                    putInt("imageId", page.imageId)
                }
                return page.fragment
            }

            override fun getPageTitle(position: Int): CharSequence? = pages[position].title

            override fun getCount(): Int = pages.size
        }

        data class Page(val imageId: Int,val title: String, val fragment: Fragment)
    }


//        /        btnSignOut.setOnClickListener {
//            firebaseAuth.signOut()
//            startActivity(Intent(this, Createaccount::class.java))
//            toast("signed out")
//            finish()
//        }


