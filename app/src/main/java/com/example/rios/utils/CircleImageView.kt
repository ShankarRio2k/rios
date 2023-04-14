package com.example.rios.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val circlePath = Path()
    private val circlePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            val drawable = drawable ?: return // Add null check here

            // Calculate circle radius based on aspect ratio of image
            val aspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = if (aspectRatio > 1) {
                minOf(centerX, centerY / aspectRatio)
            } else {
                minOf(centerX * aspectRatio, centerY)
            }

            // Draw circle shape
            circlePath.reset()
            circlePath.addCircle(centerX, centerY, radius, Path.Direction.CW)

            // Apply circle clipping
            it.save()
            it.clipPath(circlePath)

            // Draw image within clipping path
            super.onDraw(canvas)

            // Restore canvas to original state
            it.restore()
        }
    }

}
