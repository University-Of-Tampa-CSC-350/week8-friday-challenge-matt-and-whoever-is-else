package com.example.fc_006

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fc_006.data.Asteroid
import com.example.fc_006.data.RetrofitInstance
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

class AsteroidViewModel : ViewModel() {

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
                updateState()
            } catch (e: Exception) {
                _uiState.value = AsteroidUiState.Error("Mission Control lost asteroid tracking data.")
            }
        }
    }

    fun nextAsteroid() {
        if (currentIndex < asteroidList.size - 1) {
            currentIndex++
            updateState()
        }
    }

    fun previousAsteroid() {
        if (currentIndex > 0) {
            currentIndex--
            updateState()
        }
    }

    fun markCurrentAsDestroyed() {
        if (asteroidList.isNotEmpty()) {
            destroyedAsteroidIds.add(asteroidList[currentIndex].id)
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
