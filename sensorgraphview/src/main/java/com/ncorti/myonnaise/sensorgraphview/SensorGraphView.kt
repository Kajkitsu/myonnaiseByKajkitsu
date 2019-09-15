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

class SensorGraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var running = false
    var tableOfSelectedData = mutableListOf<Boolean>( false, false, false, false, false,
                                    false, false, false, false, false,
                                    false, false, false, false, false,
                                    false, false, false)
    var isEmgModeOn=true


    var channels = 0
        set(value) {
            field = value
            normalizedPoints = arrayOf()
            for (i in 0 until value) {
                normalizedPoints += FloatArray(MAX_DATA_SIZE)
            }
        }

    var maxValue = INITIAL_MAX_VALUE
    var minValue = INITIAL_MIN_VALUE

    val spread: Float
        get() = maxValue - minValue

    private val zeroLine: Float
        get() = (0 - minValue) / spread

    /** Paint brush for drawing samples  */
    private val rectPaints = arrayListOf<Paint>()
    /** Paint brush for drawing info datas  */
    private val infoPaint: Paint

    /** Matrix of points  */
    private var normalizedPoints: Array<FloatArray> = arrayOf()
    /** Current index in matrix  */
    private var currentIndexEMG = 0
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

    fun addPointEmg(points: FloatArray) {
        for (i in 0..7) {
            this.normalizedPoints[i][currentIndexEMG] = (points[i] - minValue) / spread
        }
        currentIndexEMG = (currentIndexEMG + 1) % MAX_DATA_SIZE
        invalidate()
    }

    fun addPointImu(points: FloatArray) {
        for (i in 0..9) {
            this.normalizedPoints[i+8][currentIndexIMU] = (points[i] - minValue) / spread
            Log.d("Graphh", "floatArrayData["+i+"] "+points[i].toString())
        }
        currentIndexIMU = (currentIndexIMU + 1) % MAX_DATA_SIZE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = height
        val width = width

        val zeroLine = height - height * zeroLine
        canvas.drawLine(0f, zeroLine, width.toFloat(), zeroLine, infoPaint)

        if (normalizedPoints.isEmpty()) {
            return
        }
        if (!running)
            return

        val pointSpan: Float = width.toFloat() / MAX_DATA_SIZE.toFloat()
        var previousX = -1f
        var previousY = -1f

        var fromL = 0
        var toL = 8

        if(!isEmgModeOn){
            fromL = 8
            toL = 18
        }

        for (i in fromL until toL) {

            if (tableOfSelectedData[i]){
                var currentX = pointSpan

                for (j in 0 until MAX_DATA_SIZE) {
                    val y = height - height * normalizedPoints[i][j]
                    if (previousX != -1f && previousY != -1f) {
                        canvas.drawLine(previousX, previousY, currentX, y, rectPaints[i])
                    }
                    if(isEmgModeOn)
                    {
                        if (j == (currentIndexEMG - 1) % MAX_DATA_SIZE) {
                            canvas.drawCircle(currentX, y, CIRCLE_SIZE_ACTUAL.toFloat(), infoPaint)
                            previousX = -1f
                            previousY = -1f
                        } else {
                            canvas.drawCircle(currentX, y, CIRCLE_SIZE_DEFAULT.toFloat(), rectPaints[i])
                            previousX = currentX
                            previousY = y
                        }
                    }
                    else
                    {
                        if (j == (currentIndexIMU - 1) % MAX_DATA_SIZE) {
                            canvas.drawCircle(currentX, y, CIRCLE_SIZE_ACTUAL.toFloat(), infoPaint)
                            previousX = -1f
                            previousY = -1f
                        } else {
                            canvas.drawCircle(currentX, y, CIRCLE_SIZE_DEFAULT.toFloat(), rectPaints[i])
                            previousX = currentX
                            previousY = y
                        }
                    }

                    currentX += pointSpan
                }
                previousX = -1f
                previousY = -1f
            }
        }
    }
}

/*

    fun addPointEmg(points: FloatArray) {
        for (i in 0..7) {
            for (j in MAX_DATA_SIZE-1 downTo 1){
                this.normalizedPoints[i][j] = this.normalizedPoints[i][j-1]
            }
            this.normalizedPoints[i][0] = (points[i] - minValue) / spread

        }
        //currentIndexEMG = (currentIndexEMG + 1) % MAX_DATA_SIZE
        invalidate()
    }

    fun addPointImu(points: FloatArray) {
        for (i in 0..9) {
            for (j in MAX_DATA_SIZE-1 downTo 1){
                this.normalizedPoints[i+8][j] = this.normalizedPoints[i+8][j-1]
            }
            this.normalizedPoints[i+8][0] = (points[i] - minValue) / spread
        }
        //currentIndexIMU = (currentIndexIMU + 1) % MAX_DATA_SIZE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = height
        val width = width

        val zeroLine = height - height * zeroLine
        canvas.drawLine(0f, zeroLine, width.toFloat(), zeroLine, infoPaint)

        if (normalizedPoints.isEmpty()) {
            return
        }
        if (!running)
            return

        val pointSpan: Float = width.toFloat() / MAX_DATA_SIZE.toFloat()
        var previousX = -1f
        var previousY = -1f

        for (i in 0 until channels) {

            if (tableOfSelectedData[i]){
            var currentX = pointSpan

            for (j in 0 until MAX_DATA_SIZE) {
                val y = height - height * normalizedPoints[i][j]
                if (previousX != -1f && previousY != -1f) {
                    canvas.drawLine(previousX, previousY, currentX, y, rectPaints[i])
                }
                if(isEmgModeOn)
                {
                    if (j == MAX_DATA_SIZE - 1) {
                   // if (j == (currentIndexEMG - 1) % MAX_DATA_SIZE) {
                     //   canvas.drawCircle(currentX, y, CIRCLE_SIZE_ACTUAL.toFloat(), infoPaint)
                        previousX = -1f
                        previousY = -1f
                    } else {
                        canvas.drawCircle(currentX, y, CIRCLE_SIZE_DEFAULT.toFloat(), rectPaints[i])
                        previousX = currentX
                        previousY = y
                    }
                }
                else
                {
                    //if (j == (currentIndexIMU - 1) % MAX_DATA_SIZE) {
                        if (j == MAX_DATA_SIZE - 1) {
                      //  canvas.drawCircle(currentX, y, CIRCLE_SIZE_ACTUAL.toFloat(), infoPaint)
                        previousX = -1f
                        previousY = -1f
                    } else {
                        canvas.drawCircle(currentX, y, CIRCLE_SIZE_DEFAULT.toFloat(), rectPaints[i])
                        previousX = currentX
                        previousY = y
                    }
                }

                currentX += pointSpan
            }
            previousX = -1f
            previousY = -1f
        }
        }
    }
 */