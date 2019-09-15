package it.ncorti.emgvisualizer.ui.export

import it.ncorti.emgvisualizer.BasePresenter
import it.ncorti.emgvisualizer.BaseView

interface ExportContract {

    interface View : BaseView {
        fun enableStartCollectingButton()
        fun enableImuStartCollectingButton()

        fun disableStartCollectingButton()
        fun disableImuStartCollectingButton()

        fun showNotStreamingErrorMessage()

        fun showCollectionStarted()
        fun showImuCollectionStarted()

        fun showCollectionStopped()
        fun showImuCollectionStopped()

        fun showCollectedPoints(totalPoints: Int)
        fun showImuCollectedPoints(totalPoints: Int)

        fun enableSaveButton()
        fun enableImuSaveButton()

        fun disableSaveButton()
        fun disableImuSaveButton()

        fun saveCsvFile(content: String)

        fun sharePlainText(content: String)
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {

        abstract fun onCollectionTogglePressed()
        abstract fun onImuCollectionTogglePressed()

        abstract fun onSavePressed()
        abstract fun onImuSavePressed()
    }
}
