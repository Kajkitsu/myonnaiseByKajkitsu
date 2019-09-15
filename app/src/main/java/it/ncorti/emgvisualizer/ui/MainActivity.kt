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
                R.id.item_graph -> {
                    view_pager.currentItem = 2

                }

                R.id.item_export -> view_pager.currentItem = 3
            }
            false
        }
    }

    fun actualize(v : View) {
        if(radioButtonEmg.isChecked) {
            sensor_graph_view_emg.visibility=View.VISIBLE
            sensor_graph_view_imu.visibility=View.INVISIBLE
        }
        else if(radioButtonImu.isChecked) {
            sensor_graph_view_emg.visibility=View.INVISIBLE
            sensor_graph_view_imu.visibility=View.VISIBLE
        }
        else if(radioButtonClass.isChecked){
            sensor_graph_view_emg.visibility=View.INVISIBLE
            sensor_graph_view_imu.visibility=View.INVISIBLE
        }

        if(radioButtonImu.isChecked){
            ch0_emg.visibility=View.INVISIBLE
            ch1_emg.visibility=View.INVISIBLE
            ch2_emg.visibility=View.INVISIBLE
            ch3_emg.visibility=View.INVISIBLE
            ch4_emg.visibility=View.INVISIBLE
            ch5_emg.visibility=View.INVISIBLE
            ch6_emg.visibility=View.INVISIBLE
            ch7_emg.visibility=View.INVISIBLE
            ch0_imu.visibility=View.VISIBLE
            ch1_imu.visibility=View.VISIBLE
            ch2_imu.visibility=View.VISIBLE
            ch3_imu.visibility=View.VISIBLE
            ch4_imu.visibility=View.VISIBLE
            ch5_imu.visibility=View.VISIBLE
            ch6_imu.visibility=View.VISIBLE
            ch7_imu.visibility=View.VISIBLE
            ch8_imu.visibility=View.VISIBLE
            ch9_imu.visibility=View.VISIBLE

            sensor_graph_view_imu.tableOfSelectedData[0]=ch0_imu.isChecked
            sensor_graph_view_imu.tableOfSelectedData[1]=ch1_imu.isChecked
            sensor_graph_view_imu.tableOfSelectedData[2]=ch2_imu.isChecked
            sensor_graph_view_imu.tableOfSelectedData[3]=ch3_imu.isChecked
            sensor_graph_view_imu.tableOfSelectedData[4]=ch4_imu.isChecked
            sensor_graph_view_imu.tableOfSelectedData[5]=ch5_imu.isChecked
            sensor_graph_view_imu.tableOfSelectedData[6]=ch6_imu.isChecked
            sensor_graph_view_imu.tableOfSelectedData[7]=ch7_imu.isChecked
            sensor_graph_view_imu.tableOfSelectedData[8]=ch8_imu.isChecked
            sensor_graph_view_imu.tableOfSelectedData[9]=ch9_imu.isChecked
        }
        else if(radioButtonEmg.isChecked){
            sensor_graph_view_emg.tableOfSelectedData[0]=ch0_emg.isChecked
            sensor_graph_view_emg.tableOfSelectedData[1]=ch1_emg.isChecked
            sensor_graph_view_emg.tableOfSelectedData[2]=ch2_emg.isChecked
            sensor_graph_view_emg.tableOfSelectedData[3]=ch3_emg.isChecked
            sensor_graph_view_emg.tableOfSelectedData[4]=ch4_emg.isChecked
            sensor_graph_view_emg.tableOfSelectedData[5]=ch5_emg.isChecked
            sensor_graph_view_emg.tableOfSelectedData[6]=ch6_emg.isChecked
            sensor_graph_view_emg.tableOfSelectedData[7]=ch7_emg.isChecked

            ch0_emg.visibility=View.VISIBLE
            ch1_emg.visibility=View.VISIBLE
            ch2_emg.visibility=View.VISIBLE
            ch3_emg.visibility=View.VISIBLE
            ch4_emg.visibility=View.VISIBLE
            ch5_emg.visibility=View.VISIBLE
            ch6_emg.visibility=View.VISIBLE
            ch7_emg.visibility=View.VISIBLE
            ch0_imu.visibility=View.INVISIBLE
            ch1_imu.visibility=View.INVISIBLE
            ch2_imu.visibility=View.INVISIBLE
            ch3_imu.visibility=View.INVISIBLE
            ch4_imu.visibility=View.INVISIBLE
            ch5_imu.visibility=View.INVISIBLE
            ch6_imu.visibility=View.INVISIBLE
            ch7_imu.visibility=View.INVISIBLE
            ch8_imu.visibility=View.INVISIBLE
            ch9_imu.visibility=View.INVISIBLE
        }
        else{
            ch0_emg.visibility=View.INVISIBLE
            ch1_emg.visibility=View.INVISIBLE
            ch2_emg.visibility=View.INVISIBLE
            ch3_emg.visibility=View.INVISIBLE
            ch4_emg.visibility=View.INVISIBLE
            ch5_emg.visibility=View.INVISIBLE
            ch6_emg.visibility=View.INVISIBLE
            ch7_emg.visibility=View.INVISIBLE
            ch0_imu.visibility=View.INVISIBLE
            ch1_imu.visibility=View.INVISIBLE
            ch2_imu.visibility=View.INVISIBLE
            ch3_imu.visibility=View.INVISIBLE
            ch4_imu.visibility=View.INVISIBLE
            ch5_imu.visibility=View.INVISIBLE
            ch6_imu.visibility=View.INVISIBLE
            ch7_imu.visibility=View.INVISIBLE
            ch8_imu.visibility=View.INVISIBLE
            ch9_imu.visibility=View.INVISIBLE
        }

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
