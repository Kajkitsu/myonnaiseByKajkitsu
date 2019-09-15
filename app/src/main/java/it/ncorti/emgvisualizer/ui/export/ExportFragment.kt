package it.ncorti.emgvisualizer.ui.export

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import dagger.android.support.AndroidSupportInjection
import it.ncorti.emgvisualizer.BaseFragment
import it.ncorti.emgvisualizer.R
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlinx.android.synthetic.main.layout_export.*
import java.util.Date
import java.text.SimpleDateFormat

private const val REQUEST_WRITE_EXTERNAL_CODE = 2

class ExportFragment : BaseFragment<ExportContract.Presenter>(), ExportContract.View {

    companion object {
        fun newInstance() = ExportFragment()
    }

    @Inject
    lateinit var exportPresenter: ExportPresenter

    private var fileContentToSave: String? = null

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        attachPresenter(exportPresenter)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.layout_export, container, false)
        setHasOptionsMenu(true)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_start_collecting.setOnClickListener { exportPresenter.onCollectionTogglePressed() }
        button_save_collecting.setOnClickListener { exportPresenter.onSavePressed() }

        imu_button_start_collecting.setOnClickListener { exportPresenter.onImuCollectionTogglePressed() }
        imu_button_save_collecting.setOnClickListener { exportPresenter.onImuSavePressed() }
    }

    override fun enableStartCollectingButton() {
        button_start_collecting.isEnabled = true
    }

    override fun enableImuStartCollectingButton() {
        imu_button_start_collecting.isEnabled = true
    }

    override fun disableStartCollectingButton() {
        button_start_collecting.isEnabled = false
    }

    override fun disableImuStartCollectingButton() {
        imu_button_start_collecting.isEnabled = false
    }

    override fun showNotStreamingErrorMessage() {
        Toast.makeText(activity, "You can't collect points if Myo is not streaming!", Toast.LENGTH_SHORT).show()
    }

    override fun showCollectionStarted() {
        button_start_collecting?.text = getString(R.string.stop)
    }

    override fun showImuCollectionStarted() {
        imu_button_start_collecting?.text = getString(R.string.stop)
    }

    override fun showCollectionStopped() {
        button_start_collecting?.text = getString(R.string.start)
    }

    override fun showImuCollectionStopped() {
        imu_button_start_collecting?.text = getString(R.string.start)
    }

    override fun showCollectedPoints(totalPoints: Int) {
        points_count.text = totalPoints.toString()
    }

    override fun showImuCollectedPoints(totalPoints: Int) {
        imu_points_count.text = totalPoints.toString()
    }

    override fun enableSaveButton() {
        button_save_collecting.isEnabled = true
    }

    override fun enableImuSaveButton() {
        imu_button_save_collecting.isEnabled = true
    }

    override fun disableSaveButton() {
        button_save_collecting.isEnabled = false
    }

    override fun disableImuSaveButton() {
        imu_button_save_collecting.isEnabled = false
    }

    override fun sharePlainText(content: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, content)
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    override fun saveCsvFile(content: String) {
        context?.apply {
            val hasPermission = (
                ContextCompat.checkSelfPermission(
                    this,
                    WRITE_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED
                )
            if (hasPermission) {
                writeToFile(content)
            } else {
                fileContentToSave = content
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_EXTERNAL_CODE
                )
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun writeToFile(content: String) {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val date = sdf.format(Date())

        val storageDir =
            File("${Environment.getExternalStorageDirectory().absolutePath}/Myo_Wygenerowane_Dane")
        storageDir.mkdir()
        val outfile = File(storageDir, "MyoData $date.csv")
        val fileOutputStream = FileOutputStream(outfile)
        fileOutputStream.write(content.toByteArray())
        fileOutputStream.close()
        Toast.makeText(activity, "Saved to: ${outfile.path}", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_WRITE_EXTERNAL_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fileContentToSave?.apply { writeToFile(this) }
                } else {
                    Toast.makeText(
                        activity, getString(R.string.write_permission_denied_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
