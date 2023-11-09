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

package com.example.detectaine.drawables

import android.graphics.*
import android.graphics.drawable.Drawable
import com.google.mlkit.vision.objects.DetectedObject

/**
 * A Drawable that handles displaying a QR Code's data and a bounding box around the QR code.
 */
class ObjectDetectedDrawable(objectViewModel: DetectedObject) : Drawable() {
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

    private val objectViewModel = objectViewModel
    private val contentPadding = 25
    private var textWidth = contentTextPaint.measureText(objectViewModel.labels[0].text).toInt()

    override fun draw(canvas: Canvas) {
        canvas.drawRect(objectViewModel.boundingBox, boundingRectPaint)
        canvas.drawRect(
            Rect(
                objectViewModel.boundingBox.left,
                objectViewModel.boundingBox.bottom + contentPadding/2,
                objectViewModel.boundingBox.left + textWidth + contentPadding*2,
                objectViewModel.boundingBox.bottom + contentTextPaint.textSize.toInt() + contentPadding),
            contentRectPaint
        )
        canvas.drawText(
            objectViewModel.labels[0].text,
            (objectViewModel.boundingBox.left + contentPadding).toFloat(),
            (objectViewModel.boundingBox.bottom + contentPadding*2).toFloat(),
            contentTextPaint
        )
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