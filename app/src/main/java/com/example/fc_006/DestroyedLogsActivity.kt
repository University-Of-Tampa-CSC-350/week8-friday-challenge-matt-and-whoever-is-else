package com.example.fc_006

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DestroyedLogsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_destroyed_logs)

        findViewById<ImageButton>(R.id.logsBackButton).setOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.logsContainer)
        
        // In a real app, we'd use the same ViewModel instance (SharedViewModel/Hilt)
        // For this challenge, we'll simulate the data passed through a static list or similar.
        // But to keep it simple and functional for you, I'll use a trick.
        
        val names = intent.getStringArrayListExtra("EXTRA_NAMES") ?: arrayListOf()

        if (names.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "NO DATA RECORDED"
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                setPadding(0, 100, 0, 0)
            }
            container.addView(emptyText)
        }

        names.forEach { name ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 16, 0, 16)
                gravity = Gravity.CENTER_VERTICAL
            }

            val nameView = TextView(this).apply {
                text = name
                setTextColor(Color.WHITE)
                textSize = 18f
                typeface = android.graphics.Typeface.MONOSPACE
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val viewButton = Button(this).apply {
                text = "VIEW"
                setBackgroundColor(ContextCompat.getColor(context, R.color.star_gold))
                setTextColor(Color.BLACK)
            }
            viewButton.setOnClickListener {
                startActivity(Intent(this@DestroyedLogsActivity, ScatteredViewActivity::class.java))
            }

            row.addView(nameView)
            row.addView(viewButton)
            container.addView(row)
        }
    }
}
