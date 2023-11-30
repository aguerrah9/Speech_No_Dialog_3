package com.example.detectaine.drawables

import android.graphics.*
import android.graphics.drawable.Drawable

class LineaDrawable(x1:Double,y1:Double,x2:Double,y2:Double): Drawable() {

    val x1 = x1.toFloat()
    val y1 = y1.toFloat()
    val x2 = x2.toFloat()
    val y2 = y2.toFloat()

    private val boundingRectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GREEN
        strokeWidth = 8F
        alpha = 200
    }

    override fun draw(canvas: Canvas) {
        canvas.drawLine( x1, y1, x2, y2, boundingRectPaint)
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