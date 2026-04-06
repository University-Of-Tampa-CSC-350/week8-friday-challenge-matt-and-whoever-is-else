package com.example.fc_006

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Random

class MeteorShowerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val meteors = mutableListOf<Meteor>()
    private val paint = Paint().apply {
        strokeWidth = 4f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val random = Random()
    private var isAnimating = true

    private class Meteor(
        var x: Float,
        var y: Float,
        var speed: Float,
        var length: Float,
        var alpha: Int,
        var width: Float
    )

    init {
        for (i in 0 until 15) {
            meteors.add(createRandomMeteor(true))
        }
    }

    private fun createRandomMeteor(randomY: Boolean): Meteor {
        val x = random.nextInt(width.coerceAtLeast(1)).toFloat()
        val y = if (randomY) random.nextInt(height.coerceAtLeast(1)).toFloat() else -100f
        return Meteor(
            x = x,
            y = y,
            speed = 15f + random.nextFloat() * 20f,
            length = 50f + random.nextFloat() * 100f,
            alpha = 50 + random.nextInt(200),
            width = 2f + random.nextFloat() * 6f
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        for (i in meteors.indices) {
            val m = meteors[i]
            paint.color = Color.WHITE
            paint.alpha = m.alpha
            paint.strokeWidth = m.width

            canvas.drawLine(m.x, m.y, m.x - m.length * 0.5f, m.y - m.length, paint)

            m.x += m.speed * 0.5f
            m.y += m.speed

            if (m.y > height || m.x > width) {
                meteors[i] = createRandomMeteor(false)
            }
        }

        if (isAnimating) {
            postInvalidateOnAnimation()
        }
    }

    fun startAnimation() {
        isAnimating = true
        invalidate()
    }

    fun stopAnimation() {
        isAnimating = false
    }
}
