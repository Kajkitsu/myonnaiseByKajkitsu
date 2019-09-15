package it.ncorti.emgvisualizer.ui.graph

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import it.ncorti.emgvisualizer.dagger.DeviceManager

class GraphPresenter(
    override val view: GraphContract.View,
    private val deviceManager: DeviceManager
) : GraphContract.Presenter(view){

    private var dataSubscriptionEmg: Disposable? = null
    private var dataSubscriptionImu: Disposable? = null
//    private var dataSubscriptionImuAce: Disposable? = null
//    private var dataSubscriptionImuGyr: Disposable? = null

    override fun create() {}

    override fun start() {
        deviceManager.myo?.apply {
            if (this.isStreaming()) {
                view.hideNoStreamingMessage()
                dataSubscriptionEmg?.apply {
                    if (!this.isDisposed) this.dispose()
                }
                dataSubscriptionEmg = this.emg.dataFlowableEmg()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        view.startGraph(true)
                    }
                    .subscribe {
                        view.showDataEmg(it)
                    }


                dataSubscriptionImu?.apply {
                    if (!this.isDisposed) this.dispose()
                }
                dataSubscriptionImu = this.dataFlowableImuGyro()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            view.startGraph(true)
                        }
                        .subscribe {
                            view.showDataImu(it)
                        }
//                dataSubscriptionImuAce?.apply {
//                    if (!this.isDisposed) this.dispose()
//                }
//                dataSubscriptionImuAce = this.dataFlowableImuAccelerometer()
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doOnSubscribe {
//                            view.startGraph(true)
//                        }
//                        .subscribe {
//                            view.showDataEmg(it)
//                        }
//                dataSubscriptionImuGyr?.apply {
//                    if (!this.isDisposed) this.dispose()
//                }
//                dataSubscriptionImuGyr = this.dataFlowableImuGyro()
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doOnSubscribe {
//                            view.startGraph(true)
//                        }
//                        .subscribe {
//                            view.showDataEmg(it)
//                        }
            } else {
                view.showNoStreamingMessage()
            }
        }
    }

    override fun stop() {
        view.startGraph(false)
        dataSubscriptionImu?.dispose()
 //       dataSubscriptionImuAce?.dispose()
//        dataSubscriptionImuGyr?.dispose()
        dataSubscriptionEmg?.dispose()
    }
}
