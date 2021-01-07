package app.java.sentiextract.ui.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log

import app.java.sentiextract.ui.view.GraphicOverlay.Graphic
import com.google.mlkit.vision.text.Text


class TextGraphic internal constructor(overlay: GraphicOverlay?, element: Text.Element?) :
    Graphic(overlay!!) {
    private val rectPaint: Paint
    private val textPaint: Paint
    private val element: Text.Element?

    override fun draw(canvas: Canvas?) {
        Log.d(TAG, "on draw text graphic")
        checkNotNull(element) { "Attempting to draw a null text." }

        // Draws the bounding box around the TextBlock.
        val rect = RectF(element.getBoundingBox())
        canvas?.drawRect(rect, rectPaint)

        // Renders the text at the bottom of the box.
        canvas?.drawText(element.getText(), rect.left, rect.bottom, textPaint)
    }

    companion object {
        private const val TAG = "TextGraphic"
        private val TEXT_COLOR: Int = Color.RED
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
    }

    init {
        this.element = element
        rectPaint = Paint()
        rectPaint.setColor(TEXT_COLOR)
        rectPaint.setStyle(Paint.Style.STROKE)
        rectPaint.setStrokeWidth(STROKE_WIDTH)
        textPaint = Paint()
        textPaint.setColor(TEXT_COLOR)
        textPaint.setTextSize(TEXT_SIZE)
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }
}