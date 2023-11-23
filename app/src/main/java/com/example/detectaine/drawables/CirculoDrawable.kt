package com.example.detectaine.drawables

import android.graphics.*
import android.graphics.drawable.Drawable

class CirculoDrawable(x: Double, y: Double): Drawable() {

    val puntoX = x.toFloat()
    val puntoY = y.toFloat()
    val radio = 10f

    private val boundingRectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = 8F
        alpha = 200
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle( puntoX, puntoY, radio, boundingRectPaint )
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