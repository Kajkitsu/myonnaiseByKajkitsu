package com.ncorti.myonnaise.sensorgraphview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat

/** Default point circle size  */
private const val CIRCLE_SIZE_DEFAULT = 3
/** Now drawing point circle size  */
private const val CIRCLE_SIZE_ACTUAL = 20
/** Graph size  */
private const val MAX_DATA_SIZE = 150

private const val INITIAL_MAX_VALUE = 1.0f
private const val INITIAL_MIN_VALUE = -1.0f

class SensorGraphViewForImu(context: Context, attrs: AttributeSet) : View(context, attrs) {

//    enum class ImuGraphMode() {
//        GYR,
//        ACC,
//        ORI
//    }

    var running = false
    var tableOfSelectedData = mutableListOf<Boolean>( false, false, false, false, false,
                                    false, false, false, false, false)
    var channels = 0
        set(value) {
            field = value
            normalizedPoints = arrayOf()
            for (i in 0 until value) {
                normalizedPoints += FloatArray(MAX_DATA_SIZE)
            }
        }

    var maxValueOri = INITIAL_MAX_VALUE
    var minValueOri = INITIAL_MIN_VALUE
    var maxValueAcc = INITIAL_MAX_VALUE
    var minValueAcc = INITIAL_MIN_VALUE
    var maxValueGyr = INITIAL_MAX_VALUE
    var minValueGyr = INITIAL_MIN_VALUE

    val spreadOri: Float
        get() = maxValueOri - minValueOri
    val spreadAcc: Float
        get() = maxValueAcc - minValueAcc
    val spreadGyr: Float
        get() = maxValueGyr - minValueGyr

    private val zeroLineGyr: Float
        get() = (0 - minValueGyr) / spreadGyr
    private val zeroLineAcc: Float
        get() = (0 - minValueAcc) / spreadAcc
    private val zeroLineOri: Float
        get() = (0 - minValueOri) / spreadOri

    /** Paint brush for drawing samples  */
    private val rectPaints = arrayListOf<Paint>()
    /** Paint brush for drawing info datas  */
    private val infoPaint: Paint

    /** Matrix of points  */
    private var normalizedPoints: Array<FloatArray> = arrayOf()
    /** Current index in matrix  */
    private var currentIndexIMU = 0

    init {

        val colors = context.resources.getIntArray(R.array.graph_colors)
        for (i in 0 until colors.size) {
            val paint = Paint()
            paint.color = Color.parseColor("#${Integer.toHexString(colors[i])}")
            rectPaints += paint
        }

        infoPaint = Paint()
        infoPaint.color = ContextCompat.getColor(context, R.color.graph_info)

        infoPaint.textSize = context.resources
            .getDimensionPixelSize(R.dimen.text_size).toFloat()
        infoPaint.isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> Math.min(desiredWidth, widthSize)
            MeasureSpec.UNSPECIFIED -> desiredWidth
            else -> desiredWidth
        }
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> Math.min(desiredHeight, heightSize)
            MeasureSpec.UNSPECIFIED -> desiredHeight
            else -> desiredHeight
        }
        setMeasuredDimension(width, height)
    }

    fun addPoint(points: FloatArray) {
        for (i in 0..9) {
            when (i) {
                in 0..3 -> this.normalizedPoints[i][currentIndexIMU] = (points[i] - minValueOri) / spreadOri
                in 4..6 -> this.normalizedPoints[i][currentIndexIMU] = (points[i] - minValueAcc) / spreadAcc
                in 7..9 -> this.normalizedPoints[i][currentIndexIMU] = (points[i] - minValueGyr) / spreadGyr
            }
//
//            if(i==0){
//                if(maxValueOri<points[i]){
//                    maxValueOri=points[i]
//                    Log.d("maxValueOri", "maxValueOri: ${maxValueOri.toString()}")
//                }
//                if(minValueOri>points[i]){
//                    minValueOri=points[i]
//                    Log.d("minValueOri", "minValueOri: ${minValueOri.toString()}")
//                }
//
//            }
//            if(i==4){
//                if(maxValueAcc<points[i]){
//                    maxValueAcc=points[i]
//                    Log.d("maxValueAcc", "maxValueAcc: ${maxValueAcc.toString()}")
//                }
//                if(minValueAcc>points[i]){
//                    minValueAcc=points[i]
//                    Log.d("minValueAcc", "minValueAcc: ${minValueAcc.toString()}")
//                }
//            }
//            if(i==7){
//                if(maxValueGyr<points[i]){
//                    maxValueGyr=points[i]
//                    Log.d("maxValueGyr", "maxValueGyr: ${maxValueGyr.toString()}")
//                }
//                if(minValueGyr>points[i]){
//                    minValueGyr=points[i]
//                    Log.d("minValueGyr", "minValueGyr: ${minValueGyr.toString()}")
//                }
//
//            }
           // Log.d("Graphh", "floatArrayData["+i+"] "+points[i].toString())
        }
        currentIndexIMU = (currentIndexIMU + 1) % MAX_DATA_SIZE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = height
        val width = width

        val zeroLineOri = height - height * zeroLineOri
        val zeroLineAcc = height - height * zeroLineAcc
        val zeroLineGyr = height - height * zeroLineGyr
        if(tableOfSelectedData[0] || tableOfSelectedData[1] || tableOfSelectedData[2] || tableOfSelectedData[3] )
            canvas.drawLine(0f, zeroLineOri, width.toFloat(), zeroLineOri, infoPaint)
        if(tableOfSelectedData[4] || tableOfSelectedData[5] || tableOfSelectedData[6])
            canvas.drawLine(0f, zeroLineAcc, width.toFloat(), zeroLineAcc, infoPaint)
        if(tableOfSelectedData[7] || tableOfSelectedData[8] || tableOfSelectedData[9])
            canvas.drawLine(0f, zeroLineGyr, width.toFloat(), zeroLineGyr, infoPaint)


        if (normalizedPoints.isEmpty()) {
            return
        }
        if (!running)
            return

        val pointSpan: Float = width.toFloat() / MAX_DATA_SIZE.toFloat()
        var previousX = -1f
        var previousY = -1f



        for (i in 0 until 10) {

            if (tableOfSelectedData[i]){
                var currentX = pointSpan

                for (j in 0 until MAX_DATA_SIZE) {
                    val y = height - height * normalizedPoints[i][j]
                    if (previousX != -1f && previousY != -1f) {
                        canvas.drawLine(previousX, previousY, currentX, y, rectPaints[i])
                    }
                    if (j == (currentIndexIMU - 1) % MAX_DATA_SIZE) {
                        canvas.drawCircle(currentX, y, CIRCLE_SIZE_ACTUAL.toFloat(), infoPaint)
                        previousX = -1f
                        previousY = -1f
                    } else {
                        canvas.drawCircle(currentX, y, CIRCLE_SIZE_DEFAULT.toFloat(), rectPaints[i])
                        previousX = currentX
                        previousY = y
                    }


                    currentX += pointSpan
                }
                previousX = -1f
                previousY = -1f
            }
        }
    }
}
