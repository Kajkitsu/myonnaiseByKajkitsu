package it.ncorti.emgvisualizer.ui.graph

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ncorti.myonnaise.MyoCompoments.MYO_MAX_VALUE
import com.ncorti.myonnaise.MyoCompoments.MYO_MIN_VALUE
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
        sensor_graph_view.channels = 18
                //MYO_CHANNELS
        sensor_graph_view.maxValue = MYO_MAX_VALUE
        sensor_graph_view.minValue = MYO_MIN_VALUE
    }

    override fun showDataEmg(data: FloatArray) {
        sensor_graph_view?.addPointEmg(data)
    }

    override fun showDataImu(data: FloatArray) {
        sensor_graph_view?.addPointImu(data)
    }

    override fun startGraph(running: Boolean) {
        sensor_graph_view?.apply {
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
