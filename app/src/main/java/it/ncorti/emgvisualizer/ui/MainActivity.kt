package it.ncorti.emgvisualizer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import it.ncorti.emgvisualizer.R
import it.ncorti.emgvisualizer.ui.control.ControlDeviceFragment
import it.ncorti.emgvisualizer.ui.export.ExportFragment
import it.ncorti.emgvisualizer.ui.graph.GraphFragment
import it.ncorti.emgvisualizer.ui.scan.ScanDeviceFragment
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_graph.*

private const val PREFS_GLOBAL = "global"
private const val KEY_COMPLETED_ONBOARDING = "completed_onboarding"

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    @Suppress("MagicNumber")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        // Checking if we should on-board the user the first time.
        val prefs = getSharedPreferences(PREFS_GLOBAL, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_COMPLETED_ONBOARDING, false)) {
            finish()
            startActivity(Intent(this, IntroActivity::class.java))
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.new_toolbar))

        val fragmentList = listOf<Fragment>(
            ScanDeviceFragment.newInstance(),
            ControlDeviceFragment.newInstance(),
            GraphFragment.newInstance(),

            ExportFragment.newInstance()
        )

        view_pager.adapter = MyAdapter(supportFragmentManager, fragmentList)
        view_pager.offscreenPageLimit = 3
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            var prevMenuItem: MenuItem? = null
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem?.isChecked = false
                } else {
                    bottom_navigation.menu.getItem(0).isChecked = false
                }
                bottom_navigation.menu.getItem(position).isChecked = true
                prevMenuItem = bottom_navigation.menu.getItem(position)
            }
        })
        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_scan -> view_pager.currentItem = 0
                R.id.item_control -> view_pager.currentItem = 1
                R.id.item_graph_emg -> {
                    view_pager.currentItem = 2

                }

                R.id.item_export -> view_pager.currentItem = 3
            }
            false
        }
    }

    fun actualize(v : View) {
        sensor_graph_view.isEmgModeOn = !imuModeOn.isChecked
        if(!sensor_graph_view.isEmgModeOn)
        {
            ch0.visibility=View.INVISIBLE
            ch1.visibility=View.INVISIBLE
            ch2.visibility=View.INVISIBLE
            ch3.visibility=View.INVISIBLE
            ch4.visibility=View.INVISIBLE
            ch5.visibility=View.INVISIBLE
            ch6.visibility=View.INVISIBLE
            ch7.visibility=View.INVISIBLE

            ch8.visibility=View.VISIBLE
            ch9.visibility=View.VISIBLE
            ch10.visibility=View.VISIBLE
            ch11.visibility=View.VISIBLE
            ch12.visibility=View.VISIBLE
            ch13.visibility=View.VISIBLE
            ch14.visibility=View.VISIBLE
            ch15.visibility=View.VISIBLE
            ch16.visibility=View.VISIBLE
            ch17.visibility=View.VISIBLE
        }
        else
        {
            ch0.visibility=View.VISIBLE
            ch1.visibility=View.VISIBLE
            ch2.visibility=View.VISIBLE
            ch3.visibility=View.VISIBLE
            ch4.visibility=View.VISIBLE
            ch5.visibility=View.VISIBLE
            ch6.visibility=View.VISIBLE
            ch7.visibility=View.VISIBLE

            ch8.visibility=View.INVISIBLE
            ch9.visibility=View.INVISIBLE
            ch10.visibility=View.INVISIBLE
            ch11.visibility=View.INVISIBLE
            ch12.visibility=View.INVISIBLE
            ch13.visibility=View.INVISIBLE
            ch14.visibility=View.INVISIBLE
            ch15.visibility=View.INVISIBLE
            ch16.visibility=View.INVISIBLE
            ch17.visibility=View.INVISIBLE
        }



        sensor_graph_view.tableOfSelectedData[0]=ch0.isChecked
        sensor_graph_view.tableOfSelectedData[1]=ch1.isChecked
        sensor_graph_view.tableOfSelectedData[2]=ch2.isChecked
        sensor_graph_view.tableOfSelectedData[3]=ch3.isChecked
        sensor_graph_view.tableOfSelectedData[4]=ch4.isChecked
        sensor_graph_view.tableOfSelectedData[5]=ch5.isChecked
        sensor_graph_view.tableOfSelectedData[6]=ch6.isChecked
        sensor_graph_view.tableOfSelectedData[7]=ch7.isChecked
        sensor_graph_view.tableOfSelectedData[8]=ch8.isChecked
        sensor_graph_view.tableOfSelectedData[9]=ch9.isChecked
        sensor_graph_view.tableOfSelectedData[10]=ch10.isChecked
        sensor_graph_view.tableOfSelectedData[11]=ch11.isChecked
        sensor_graph_view.tableOfSelectedData[12]=ch12.isChecked
        sensor_graph_view.tableOfSelectedData[13]=ch13.isChecked
        sensor_graph_view.tableOfSelectedData[14]=ch14.isChecked
        sensor_graph_view.tableOfSelectedData[15]=ch15.isChecked
        sensor_graph_view.tableOfSelectedData[16]=ch16.isChecked
        sensor_graph_view.tableOfSelectedData[17]=ch17.isChecked


    }
    fun navigateToPage(pageId: Int) {
        view_pager.currentItem = pageId
    }

    class MyAdapter(fm: FragmentManager, private val fragmentList: List<Fragment>) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }
    }

}
