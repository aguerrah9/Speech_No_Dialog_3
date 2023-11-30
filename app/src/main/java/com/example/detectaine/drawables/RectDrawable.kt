package com.example.detectaine.drawables

import android.graphics.*
import android.graphics.drawable.Drawable

class RectDrawable(x1:Double,y1:Double,x2:Double,y2:Double): Drawable() {

    private val boundingRectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 8F
        alpha = 200
    }

    private val realBoundingBox = Rect(
        x1.toInt(),
        y1.toInt(),
        x2.toInt(),
        y2.toInt(),
    )

    override fun draw(canvas: Canvas) {
        canvas.drawRect(realBoundingBox, boundingRectPaint)
    }

    override fun setAlpha(alpha: Int) {
        boundingRectPaint.alpha = alpha
    }

    override fun setColorFilter(colorFiter: ColorFilter?) {
        boundingRectPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}