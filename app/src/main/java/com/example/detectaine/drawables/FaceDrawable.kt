/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.camerax_mlkit

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import com.google.mlkit.vision.face.Face

/**
 * A Drawable that handles displaying a QR Code's data and a bounding box around the QR code.
 */
class FaceDrawable(face: Face, prewidth: Int = 0, preheigth: Int = 0) : Drawable() {
    private val boundingRectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.YELLOW
        strokeWidth = 5F
        alpha = 200
    }

    private val contentRectPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.YELLOW
        alpha = 255
    }

    private val contentTextPaint = Paint().apply {
        color = Color.DKGRAY
        alpha = 255
        textSize = 36F
    }

    private val face = face
    private val contentPadding = 25
    private var textWidth = contentTextPaint.measureText(face.toString()).toInt()
    private val prewidth = prewidth
    private val preheigth = preheigth
    private val realBoundingBox = Rect(
        (face.boundingBox.left+prewidth),
        (face.boundingBox.top+preheigth),
        (face.boundingBox.right+prewidth),
        (face.boundingBox.bottom+preheigth)
    )

    override fun draw(canvas: Canvas) {
        canvas.drawRect(realBoundingBox, boundingRectPaint)
        /*canvas.drawRect(
            Rect(
                face.boundingBox.left + prewidth,
                face.boundingBox.bottom + preheigth + contentPadding/2,
                face.boundingBox.left + prewidth + textWidth + contentPadding*2,
                face.boundingBox.bottom + preheigth + contentTextPaint.textSize.toInt() + contentPadding),
            contentRectPaint
        )
        canvas.drawText(
            face.toString(),
            (face.boundingBox.left + prewidth + contentPadding).toFloat(),
            (face.boundingBox.bottom + preheigth + contentPadding*2).toFloat(),
            contentTextPaint
        )*/
    }

    override fun setAlpha(alpha: Int) {
        boundingRectPaint.alpha = alpha
        contentRectPaint.alpha = alpha
        contentTextPaint.alpha = alpha
    }

    override fun setColorFilter(colorFiter: ColorFilter?) {
        boundingRectPaint.colorFilter = colorFilter
        contentRectPaint.colorFilter = colorFilter
        contentTextPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}