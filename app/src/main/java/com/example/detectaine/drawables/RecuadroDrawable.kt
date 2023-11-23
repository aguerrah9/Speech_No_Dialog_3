package com.example.detectaine.drawables

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.camera.view.PreviewView

class RecuadroDrawable(previewView: PreviewView): Drawable() {
    private val boundingRectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 8F
        alpha = 200
    }

    private val realBoundingBox = Rect(
        previewView.width/2 - (previewView.width * .425).toInt(),
        previewView.height/2 - (previewView.width * .275).toInt(),
        previewView.width/2 + (previewView.width * .425).toInt(),
        previewView.height/2 + (previewView.width * .275).toInt(),
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