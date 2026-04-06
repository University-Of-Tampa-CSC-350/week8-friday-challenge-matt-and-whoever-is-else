package com.example.fc_006

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MeteorDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meteor_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailMeteorShower)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val meteorName = intent.getStringExtra("EXTRA_METEOR_NAME") ?: "UNKNOWN TARGET"
        val hasCat = intent.getBooleanExtra("EXTRA_HAS_CAT", false)
        val avgDiameter = intent.getDoubleExtra("EXTRA_DIAMETER", 0.5)
        
        findViewById<TextView>(R.id.targetName).text = meteorName

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        val detonateButton = findViewById<Button>(R.id.detonateButton)
        val laserBeam = findViewById<ImageView>(R.id.laserBeam)
        val meteorImage = findViewById<ImageView>(R.id.meteorImage)
        val explosionImage = findViewById<ImageView>(R.id.explosionImage)
        val targetContainer = findViewById<View>(R.id.targetContainer)

        // Scale meteor based on size (min 0.5x, max 1.5x)
        val visualScale = (avgDiameter.toFloat() * 2f).coerceIn(0.5f, 1.5f)
        meteorImage.scaleX = visualScale
        meteorImage.scaleY = visualScale

        detonateButton.setOnClickListener {
            detonateButton.isEnabled = false
            detonateButton.text = "FIRING..."

            val targetLocation = IntArray(2)
            targetContainer.getLocationOnScreen(targetLocation)
            val targetY = targetLocation[1] + (targetContainer.height / 2)
            val screenHeight = resources.displayMetrics.heightPixels
            val travelDistance = screenHeight - targetY

            laserBeam.visibility = View.VISIBLE
            laserBeam.alpha = 1f
            
            val params = laserBeam.layoutParams
            params.height = travelDistance.toInt()
            laserBeam.layoutParams = params
            
            laserBeam.post {
                laserBeam.pivotX = laserBeam.width / 2f
                laserBeam.pivotY = travelDistance.toFloat()
                
                laserBeam.scaleY = 0f
                laserBeam.scaleX = 2.0f

                laserBeam.animate()
                    .scaleY(1f)
                    .scaleX(0.2f)
                    .setDuration(400)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction {
                        laserBeam.animate().alpha(0f).setDuration(50).start()
                        
                        meteorImage.animate()
                            .scaleX(0f)
                            .scaleY(0f)
                            .rotation(360f)
                            .setDuration(150)
                            .withEndAction {
                                meteorImage.visibility = View.GONE
                                
                                explosionImage.visibility = View.VISIBLE
                                explosionImage.scaleX = 0f
                                explosionImage.scaleY = 0f
                                explosionImage.alpha = 1f
                                
                                explosionImage.animate()
                                    .scaleX(10f)
                                    .scaleY(10f)
                                    .alpha(0f)
                                    .setDuration(1200)
                                    .setInterpolator(DecelerateInterpolator())
                                    .withEndAction {
                                        explosionImage.visibility = View.GONE
                                        detonateButton.text = "MISSION COMPLETE"
                                        setResult(RESULT_OK) // Notify MainActivity
                                    }
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
        }
    }
}
