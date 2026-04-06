package com.example.fc_006

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class ScatteredViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scattered_view)

        findViewById<ImageButton>(R.id.scatterBackButton).setOnClickListener { finish() }

        val container = findViewById<FrameLayout>(R.id.debrisContainer)
        val random = Random()
        
        val fragments = listOf(
            R.drawable.ic_meteor_fragment,
            R.drawable.ic_meteor_fragment_2,
            R.drawable.ic_meteor // Can reuse the main one as a larger piece
        )

        for (i in 0 until 15) {
            val piece = ImageView(this).apply {
                setImageResource(fragments[random.nextInt(fragments.size)])
                val size = 50 + random.nextInt(150)
                layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    leftMargin = random.nextInt(resources.displayMetrics.widthPixels - size)
                    topMargin = random.nextInt(resources.displayMetrics.heightPixels - size)
                }
                rotation = random.nextInt(360).toFloat()
                alpha = 0.5f + random.nextFloat() * 0.5f
            }
            container.addView(piece)
        }
    }
}
