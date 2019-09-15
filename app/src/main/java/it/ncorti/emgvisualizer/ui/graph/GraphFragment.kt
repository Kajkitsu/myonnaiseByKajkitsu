package it.ncorti.emgvisualizer.ui.graph

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ncorti.myonnaise.myoCompoments.*
import dagger.android.support.AndroidSupportInjection
import it.ncorti.emgvisualizer.BaseFragment
import it.ncorti.emgvisualizer.R
import javax.inject.Inject
import kotlinx.android.synthetic.main.layout_graph.*

open class GraphFragment : BaseFragment<GraphContract.Presenter>(), GraphContract.View {

    companion object {
        fun newInstance() = GraphFragment()
    }

    @Inject
    lateinit var graphPresenter: GraphPresenter

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        attachPresenter(graphPresenter)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.layout_graph, container, false)
        setHasOptionsMenu(true)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensor_graph_view_emg.channels = MYO_EMG_CHANNELS
        sensor_graph_view_imu.channels = MYO_IMU_CHANNELS
                //MYO_CHANNELS
        sensor_graph_view_emg.maxValue = MYO_MAX_EMG_VALUE
        sensor_graph_view_emg.minValue = MYO_MIN_EMG_VALUE

        sensor_graph_view_imu.maxValueOri = MYO_MAX_ORI_VALUE
        sensor_graph_view_imu.minValueOri = MYO_MIN_ORI_VALUE
        sensor_graph_view_imu.maxValueAcc = MYO_MAX_ACC_VALUE
        sensor_graph_view_imu.minValueAcc = MYO_MIN_ACC_VALUE
        sensor_graph_view_imu.maxValueGyr = MYO_MAX_GYR_VALUE
        sensor_graph_view_imu.minValueGyr = MYO_MIN_GYR_VALUE
    }

    override fun showDataEmg(data: FloatArray) {
        sensor_graph_view_emg?.addPoint(data)
    }

    override fun showDataImu(data: FloatArray) {
        sensor_graph_view_imu?.addPoint(data)
    }

    override fun startGraph(running: Boolean) {
        sensor_graph_view_emg?.apply {
            this.running = running
        }
        sensor_graph_view_imu?.apply {
            this.running = running
        }
    }

    override fun showNoStreamingMessage() {
        text_empty_graph.visibility = View.VISIBLE
    }

    override fun hideNoStreamingMessage() {
        text_empty_graph.visibility = View.INVISIBLE
    }
}
