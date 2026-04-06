package com.example.fc_006

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fc_006.data.Asteroid

class MainActivity : AppCompatActivity() {

    private lateinit var scanButton: Button
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var asteroidDataText: TextView
    private lateinit var navigationLayout: LinearLayout
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private lateinit var indexText: TextView
    private lateinit var viewMeteorButton: Button
    private lateinit var destroyedMeteorsButton: Button
    
    private val viewModel: AsteroidViewModel by viewModels()
    private var wasLoading = false
    private val notifiedHazardIds = mutableSetOf<String>()
    private val handler = Handler(Looper.getMainLooper())

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Mission Control link established. Alerts enabled.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Alerts disabled. Manual monitoring required.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scanButton = findViewById(R.id.scanButton)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        asteroidDataText = findViewById(R.id.asteroidDataText)
        navigationLayout = findViewById(R.id.navigationLayout)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)
        indexText = findViewById(R.id.indexText)
        viewMeteorButton = findViewById(R.id.viewMeteorButton)
        destroyedMeteorsButton = findViewById(R.id.destroyedMeteorsButton)

        createNotificationChannel()
        requestNotificationPermission()

        scanButton.setOnClickListener {
            viewModel.scanForAsteroids()
        }

        prevButton.setOnClickListener {
            viewModel.previousAsteroid()
        }

        nextButton.setOnClickListener {
            viewModel.nextAsteroid()
        }

        viewMeteorButton.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is AsteroidUiState.Success) {
                val intent = Intent(this, MeteorDetailActivity::class.java).apply {
                    putExtra("EXTRA_METEOR_NAME", state.asteroid.name)
                    putExtra("EXTRA_HAS_CAT", state.hasCat)
                    val diameter = state.asteroid.estimatedDiameter.kilometers
                    val avgDiameter = (diameter.min + diameter.max) / 2.0
                    putExtra("EXTRA_DIAMETER", avgDiameter)
                }
                startActivityForResult(intent, 100)
            }
        }

        destroyedMeteorsButton.setOnClickListener {
            val names = ArrayList(viewModel.destroyedAsteroids.map { it.name })
            val intent = Intent(this, DestroyedLogsActivity::class.java).apply {
                putStringArrayListExtra("EXTRA_NAMES", names)
            }
            startActivity(intent)
        }

        observeViewModel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mission Control Alerts"
            val descriptionText = "High-priority notifications for orbital monitoring"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun sendNotification(id: Int, title: String, message: String, icon: Int, targetIntent: Intent? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return // Respect user's choice, do not crash
            }
        }

        val intent = targetIntent ?: Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent: PendingIntent = if (targetIntent != null) {
            TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(intent)
                getPendingIntent(id, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }!!
        } else {
            PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(id, builder.build())
        } catch (e: Exception) {
            // Log or handle unexpected notification failures
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            viewModel.markCurrentAsDestroyed()
            val state = viewModel.uiState.value
            if (state is AsteroidUiState.Success) {
                val names = ArrayList(viewModel.destroyedAsteroids.map { it.name })
                val logIntent = Intent(this, DestroyedLogsActivity::class.java).apply {
                    putStringArrayListExtra("EXTRA_NAMES", names)
                }
                // Condition-triggered notification
                sendNotification(
                    NEUTRALIZED_NOTIFICATION_ID,
                    "TARGET NEUTRALIZED",
                    "Asteroid ${state.asteroid.name} has been successfully intercepted and destroyed.",
                    R.drawable.ic_laser_beam,
                    logIntent
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is AsteroidUiState.Idle -> {
                    loadingOverlay.visibility = View.GONE
                    navigationLayout.visibility = View.GONE
                    viewMeteorButton.visibility = View.GONE
                    destroyedMeteorsButton.visibility = View.GONE
                    asteroidDataText.text = "READY FOR SCAN..."
                }
                is AsteroidUiState.Loading -> {
                    loadingOverlay.visibility = View.VISIBLE
                    navigationLayout.visibility = View.GONE
                    viewMeteorButton.visibility = View.GONE
                    destroyedMeteorsButton.visibility = View.GONE
                    scanButton.isEnabled = false
                    wasLoading = true
                }
                is AsteroidUiState.Success -> {
                    loadingOverlay.visibility = View.GONE
                    navigationLayout.visibility = View.VISIBLE
                    
                    viewMeteorButton.visibility = if (state.isDestroyed) View.GONE else View.VISIBLE
                    destroyedMeteorsButton.visibility = if (viewModel.destroyedAsteroids.isNotEmpty()) View.VISIBLE else View.GONE
                    
                    scanButton.isEnabled = true
                    displaySingleAsteroid(state.asteroid, state.currentIndex, state.totalCount, state.isDestroyed)

                    if (wasLoading) {
                        // Intentional delay: 3 seconds after scan finishes to simulate data processing
                        handler.postDelayed({
                            sendNotification(
                                SCAN_NOTIFICATION_ID, 
                                "MISSION CONTROL: SCAN COMPLETE", 
                                "Deep space telemetry processed. ${state.totalCount} objects identified in sector.",
                                R.drawable.ic_meteor
                            )
                        }, 3000)
                        wasLoading = false
                    }

                    // Hazard notification logic: Delayed and intentional
                    if (state.asteroid.isPotentiallyHazardous && !state.isDestroyed && !notifiedHazardIds.contains(state.asteroid.id)) {
                        notifiedHazardIds.add(state.asteroid.id)
                        
                        // Delayed notification: 7 seconds to "calculate impact probability"
                        handler.postDelayed({
                            val diameter = (state.asteroid.estimatedDiameter.kilometers.min + state.asteroid.estimatedDiameter.kilometers.max) / 2.0
                            sendNotification(
                                HAZARD_NOTIFICATION_ID, 
                                "🚨 RED ALERT: HAZARD DETECTED", 
                                "Object ${state.asteroid.name} (~${String.format("%.2f", diameter)} km) on terminal trajectory. Action required!",
                                R.drawable.ic_explosion
                            )
                        }, 7000)
                    }
                }
                is AsteroidUiState.Error -> {
                    loadingOverlay.visibility = View.GONE
                    navigationLayout.visibility = View.GONE
                    viewMeteorButton.visibility = View.GONE
                    destroyedMeteorsButton.visibility = View.GONE
                    scanButton.isEnabled = true
                    showErrorMessage(state.message)
                    wasLoading = false
                }
            }
        }
    }

    private fun showErrorMessage(message: String) {
        val builder = SpannableStringBuilder()
        builder.append("⚠️ ALERT ⚠️\n\n", ForegroundColorSpan(ContextCompat.getColor(this, R.color.hazard_red)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(message)
        asteroidDataText.text = builder
    }

    private fun displaySingleAsteroid(asteroid: Asteroid, index: Int, total: Int, isDestroyed: Boolean) {
        val builder = SpannableStringBuilder()
        
        val startName = builder.length
        builder.append("NAME: ${asteroid.name}\n\n")
        builder.setSpan(StyleSpan(Typeface.BOLD), startName, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.star_gold)), startName, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        builder.append("STATUS: ")
        val statusText = if (isDestroyed) "NEUTRALIZED" else "ACTIVE"
        val statusColor = if (isDestroyed) R.color.safe_green else R.color.hazard_red
        val statusStart = builder.length
        builder.append("$statusText\n")
        builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, statusColor)), statusStart, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(StyleSpan(Typeface.BOLD), statusStart, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        builder.append("THREAT LEVEL: ")
        val hazardText = if (asteroid.isPotentiallyHazardous) "⚠️ CRITICAL" else "SAFE"
        val hazardColor = if (asteroid.isPotentiallyHazardous) R.color.hazard_red else R.color.safe_green
        val hazardStart = builder.length
        builder.append("$hazardText\n")
        builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, hazardColor)), hazardStart, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(StyleSpan(Typeface.BOLD), hazardStart, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (asteroid.isPotentiallyHazardous && !isDestroyed) {
            val recStart = builder.length
            builder.append("\nRECOMMENDATION: IMMEDIATE DESTRUCTION\n")
            builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.hazard_red)), recStart, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(StyleSpan(Typeface.BOLD_ITALIC), recStart, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            
            val anim = AlphaAnimation(0.2f, 1.0f).apply {
                duration = 500
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            }
            asteroidDataText.startAnimation(anim)
        } else {
            asteroidDataText.clearAnimation()
        }

        builder.append("\nABS MAGNITUDE: ${asteroid.absoluteMagnitude}\n")
        val diameter = asteroid.estimatedDiameter.kilometers
        builder.append("EST. DIAMETER: ${String.format("%.2f", diameter.min)} - ${String.format("%.2f", diameter.max)} km\n")
        val approach = asteroid.closeApproachData.firstOrNull()
        val missDistance = approach?.missDistance?.kilometers ?: "UNKNOWN"
        val velocity = approach?.relativeVelocity?.kilometersPerSecond ?: "UNKNOWN"
        builder.append("MISS DISTANCE: $missDistance km\n")
        builder.append("VELOCITY: $velocity km/s\n")
        
        asteroidDataText.text = builder
        
        indexText.text = "${index + 1} / $total"
        prevButton.isEnabled = index > 0
        nextButton.isEnabled = index < total - 1
        
        if (!asteroid.isPotentiallyHazardous || isDestroyed) {
            asteroidDataText.alpha = 0f
            asteroidDataText.animate().alpha(1f).setDuration(300).start()
        }
    }

    private fun SpannableStringBuilder.append(text: CharSequence, span: Any, flags: Int): SpannableStringBuilder {
        val start = length
        append(text)
        setSpan(span, start, length, flags)
        return this
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Prevent memory leaks and orphaned notifications
    }

    companion object {
        const val CHANNEL_ID = "asteroid_alerts"
        const val SCAN_NOTIFICATION_ID = 1
        const val HAZARD_NOTIFICATION_ID = 2
        const val NEUTRALIZED_NOTIFICATION_ID = 3
    }
}
