package com.example.fc_006

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fc_006.data.Asteroid
import com.example.fc_006.data.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class AsteroidUiState {
    object Idle : AsteroidUiState()
    object Loading : AsteroidUiState()
    data class Success(
        val asteroid: Asteroid,
        val currentIndex: Int,
        val totalCount: Int,
        val isDestroyed: Boolean,
        val hasCat: Boolean
    ) : AsteroidUiState()
    data class Error(val message: String) : AsteroidUiState()
}

class AsteroidViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("asteroid_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _uiState = MutableLiveData<AsteroidUiState>(AsteroidUiState.Idle)
    val uiState: LiveData<AsteroidUiState> = _uiState

    private var asteroidList: List<Asteroid> = emptyList()
    private var currentIndex: Int = 0
    private val destroyedAsteroidIds = mutableSetOf<String>()
    private var catAsteroidId: String? = null

    val destroyedAsteroids: List<Asteroid>
        get() = asteroidList.filter { destroyedAsteroidIds.contains(it.id) }

    fun scanForAsteroids() {
        _uiState.value = AsteroidUiState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.getAsteroids(RetrofitInstance.API_KEY)
                asteroidList = response.asteroids
                currentIndex = 0
                if (asteroidList.isNotEmpty()) {
                    catAsteroidId = asteroidList[Random.nextInt(asteroidList.size)].id
                }
                saveScanData()
                updateState()
            } catch (e: Exception) {
                _uiState.value = AsteroidUiState.Error("Mission Control lost asteroid tracking data.")
            }
        }
    }

    fun restoreLastScan() {
        val json = prefs.getString("last_scan", null)
        if (json != null) {
            val type = object : TypeToken<List<Asteroid>>() {}.type
            asteroidList = gson.fromJson(json, type)
            currentIndex = prefs.getInt("current_index", 0)
            catAsteroidId = prefs.getString("cat_id", null)
            val destroyedJson = prefs.getString("destroyed_ids", "[]")
            val destroyedType = object : TypeToken<Set<String>>() {}.type
            destroyedAsteroidIds.clear()
            destroyedAsteroidIds.addAll(gson.fromJson(destroyedJson, destroyedType))

            if (asteroidList.isNotEmpty()) {
                updateState()
            }
        }
    }

    private fun saveScanData() {
        prefs.edit().apply {
            putString("last_scan", gson.toJson(asteroidList))
            putInt("current_index", currentIndex)
            putString("cat_id", catAsteroidId)
            putString("destroyed_ids", gson.toJson(destroyedAsteroidIds))
            apply()
        }
    }

    fun nextAsteroid() {
        if (currentIndex < asteroidList.size - 1) {
            currentIndex++
            saveScanData()
            updateState()
        }
    }

    fun previousAsteroid() {
        if (currentIndex > 0) {
            currentIndex--
            saveScanData()
            updateState()
        }
    }

    fun markCurrentAsDestroyed() {
        if (asteroidList.isNotEmpty()) {
            destroyedAsteroidIds.add(asteroidList[currentIndex].id)
            saveScanData()
            updateState()
        }
    }

    private fun updateState() {
        if (asteroidList.isNotEmpty()) {
            val current = asteroidList[currentIndex]
            _uiState.value = AsteroidUiState.Success(
                current,
                currentIndex,
                asteroidList.size,
                destroyedAsteroidIds.contains(current.id),
                current.id == catAsteroidId
            )
        } else {
            _uiState.value = AsteroidUiState.Error("No asteroids detected in this sector.")
        }
    }
}
