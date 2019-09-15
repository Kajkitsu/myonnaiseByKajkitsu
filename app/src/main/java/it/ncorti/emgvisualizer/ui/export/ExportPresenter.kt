package it.ncorti.emgvisualizer.ui.export

import androidx.annotation.VisibleForTesting
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import it.ncorti.emgvisualizer.dagger.DeviceManager
import java.util.concurrent.atomic.AtomicInteger

class ExportPresenter(
    override val view: ExportContract.View,
    private val deviceManager: DeviceManager
) : ExportContract.Presenter(view) {

    private val counter: AtomicInteger = AtomicInteger()
    private val imuCounter: AtomicInteger = AtomicInteger()
    private val buffer: ArrayList<FloatArray> = arrayListOf()
    private val imuBuffer: ArrayList<FloatArray> = arrayListOf()

    internal var dataSubscription: Disposable? = null

    override fun create() {}

    override fun start() {
        view.showCollectedPoints(counter.get())
        view.showImuCollectedPoints(imuCounter.get())
        deviceManager.myo?.apply {
            if (this.isStreaming()) {
                view.enableStartCollectingButton()
                view.enableImuStartCollectingButton()
            } else {
                view.disableStartCollectingButton()
                view.disableImuStartCollectingButton()
            }
        }
    }

    override fun stop() {
        dataSubscription?.dispose()
        view.showCollectionStopped()
        view.showImuCollectionStopped()
    }

    override fun onCollectionTogglePressed() {
        deviceManager.myo?.apply {
            if (this.isStreaming()) {
                if (dataSubscription == null || dataSubscription?.isDisposed == true) {
                    dataSubscription = this.emg.dataFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            view.showCollectionStarted()
                            view.disableSaveButton()
                        }
                        .subscribe {
                            buffer.add(it)
                            view.showCollectedPoints(counter.incrementAndGet())
                        }
                } else {
                    dataSubscription?.dispose()
                    view.enableSaveButton()
                    view.showCollectionStopped()
                }
            } else {
                view.showNotStreamingErrorMessage()
            }
        }
    }

    override fun onImuCollectionTogglePressed() {
        deviceManager.myo?.apply {
            if (this.isStreaming()) {
                if (dataSubscription == null || dataSubscription?.isDisposed == true) {
                    dataSubscription = this.imu.dataFlowable()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe {
                                view.showImuCollectionStarted()
                                view.disableImuSaveButton()
                            }
                            .subscribe {
                                imuBuffer.add(it)
                                view.showImuCollectedPoints(imuCounter.incrementAndGet())
                            }
                } else {
                    dataSubscription?.dispose()
                    view.enableImuSaveButton()
                    view.showImuCollectionStopped()
                }
            } else {
                view.showNotStreamingErrorMessage()
            }
        }
    }

    override fun onSavePressed() {
        counter.set(0)
        view.saveCsvFile(createCsv(buffer))
        buffer.clear()
        view.showCollectedPoints(0)
        dataSubscription?.dispose()
        view.disableSaveButton()
    }

    override fun onImuSavePressed() {
        imuCounter.set(0)
        view.saveCsvFile(createCsv(imuBuffer))
        imuBuffer.clear()
        view.showImuCollectedPoints(0)
        dataSubscription?.dispose()
        view.disableImuSaveButton()
    }

    @VisibleForTesting
    internal fun createCsv(buffer: ArrayList<FloatArray>): String {
        val stringBuilder = StringBuilder()
        buffer.forEach {
            it.forEach {
                stringBuilder.append(it)
                stringBuilder.append(",")
            }
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }
}
