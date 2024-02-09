package gdsc.allways.allears.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private val ampList = mutableListOf<Float>()
    private val rectFList = mutableListOf<RectF>()

    val redPaint = Paint().apply {
        color = Color.RED
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (rectF in rectFList) {
            canvas?.drawRect(rectF, redPaint)
        }
    }

    fun addAmplitude(maxAmplitude: Float) {

        ampList.add(maxAmplitude)
        rectFList.clear()

        val rectFWidth = 3f
        val rectFMaxCount = (this.width / rectFWidth).toInt()

        val amps = ampList.takeLast(rectFMaxCount)

        for ((i, amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = 0f
            rectF.bottom = (amp * 0.018).toFloat()
            rectF.left = i * rectFWidth
            rectF.right = rectF.left + rectFWidth

            rectFList.add(rectF)
        }

        invalidate()    // UI 초기화
    }

    fun clearDecibel() {
        rectFList.clear()
        ampList.clear()
        invalidate()
    }
}