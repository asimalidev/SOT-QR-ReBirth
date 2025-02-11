package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.graphics.Matrix
import android.graphics.PointF
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView

class ZoomableImageViewTouchListener(private val imageView: ImageView) : View.OnTouchListener {

    private val matrix = Matrix()
    private val savedMatrix = Matrix()

    // Different states for interaction
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2

    private var mode = NONE

    // Points for dragging
    private val startPoint = PointF()
    private val lastPoint = PointF()

    // For pinch-zooming
    private var scale = 1f
    private val minScale = 1f
    private val maxScale = 4f

    private val scaleGestureDetector = ScaleGestureDetector(imageView.context, ScaleListener())

    init {
        imageView.imageMatrix = matrix
        imageView.scaleType = ImageView.ScaleType.MATRIX
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (event != null) {
            scaleGestureDetector.onTouchEvent(event)
        }

        val action = event?.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                lastPoint.set(event.x, event.y)
                startPoint.set(lastPoint)
                mode = DRAG
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    // Calculate the distance dragged
                    val dx = event.x - lastPoint.x
                    val dy = event.y - lastPoint.y
                    // Move the image
                    matrix.postTranslate(dx, dy)
                    lastPoint.set(event.x, event.y)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = ZOOM
                savedMatrix.set(matrix)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
            }

            MotionEvent.ACTION_UP -> {
                mode = NONE
            }
        }

        // Set the image matrix
        imageView.imageMatrix = matrix
        return true
    }

    // Inner class to handle scaling using ScaleGestureDetector
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor

            // Limiting the scale factor within min and max bounds
            scale = Math.max(minScale, Math.min(scale * scaleFactor, maxScale))
            scaleFactor = scale / detector.scaleFactor

            // Apply scaling
            matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            return true
        }
    }
}
