package com.example.bookhub.UI.Authentication.Activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.Animation
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.bookhub.R
import com.example.bookhub.UI.Authentication.Fragment.Login_fragment
import com.example.bookhub.UI.Authentication.Fragment.signup_fragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.core.view.View

class Loginhome : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setTheme(R.style.NoActionbarAndStatusbar)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility=android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.activity_loginhome)
        val topAnimation: Animation? = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val bottomanim: Animation? = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)
        val titletxt:TextView=findViewById(R.id.title_text)
        titletxt.animation=topAnimation
        val details:MaterialCardView=findViewById(R.id.logindetails)
        details.animation=bottomanim
        val tabLayout:TabLayout=findViewById(R.id.tab_layout)
        val viewpager:ViewPager=findViewById(R.id.view_pager)
        tabLayout.addTab(tabLayout.newTab().setText("Login"))
        tabLayout.addTab(tabLayout.newTab().setText("signup"))
        tabLayout.tabGravity=TabLayout.GRAVITY_FILL
        setupViewPager(viewpager)
        tabLayout.setupWithViewPager(viewpager)

    }
    companion object{
        private const val RC_SIGN_IN=120
    }
    private fun setupViewPager(viewpager: ViewPager) {
        var adapter: ViewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        adapter.addFragment(Login_fragment(), "Login")
        adapter.addFragment(signup_fragment(), "Signup")

        // setting adapter to view pager.
        viewpager.setAdapter(adapter)
    }
    class ViewPagerAdapter : FragmentPagerAdapter {

        // objects of arraylist. One is of Fragment type and
        // another one is of String type.*/
        private final var fragmentList1: ArrayList<Fragment> = ArrayList()
        private final var fragmentTitleList1: ArrayList<String> = ArrayList()

        // this is a secondary constructor of ViewPagerAdapter class.
        public constructor(supportFragmentManager: FragmentManager)
                : super(supportFragmentManager)

        // returns which item is selected from arraylist of fragments.
        override fun getItem(position: Int): Fragment {
            return fragmentList1.get(position)
        }

        // returns which item is selected from arraylist of titles.
        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList1.get(position)
        }

        // returns the number of items present in arraylist.
        override fun getCount(): Int {
            return fragmentList1.size
        }

        // this function adds the fragment and title in 2 separate  arraylist.
        fun addFragment(fragment: Fragment, title: String) {
            fragmentList1.add(fragment)
            fragmentTitleList1.add(title)
        }


    }
}