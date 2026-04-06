package com.example.fc_006.data

import com.google.gson.annotations.SerializedName

data class NeoResponse(
    @SerializedName("near_earth_objects")
    val asteroids: List<Asteroid>,
    val page: PageInfo
)

data class Asteroid(
    val id: String,
    val name: String,
    @SerializedName("absolute_magnitude_h")
    val absoluteMagnitude: Double,
    @SerializedName("estimated_diameter")
    val estimatedDiameter: EstimatedDiameter,
    @SerializedName("is_potentially_hazardous_asteroid")
    val isPotentiallyHazardous: Boolean,
    @SerializedName("close_approach_data")
    val closeApproachData: List<CloseApproachData>
)

data class EstimatedDiameter(
    val kilometers: DiameterRange
)

data class DiameterRange(
    @SerializedName("estimated_diameter_min")
    val min: Double,
    @SerializedName("estimated_diameter_max")
    val max: Double
)

data class CloseApproachData(
    @SerializedName("close_approach_date")
    val closeApproachDate: String,
    @SerializedName("relative_velocity")
    val relativeVelocity: RelativeVelocity,
    @SerializedName("miss_distance")
    val missDistance: MissDistance
)

data class RelativeVelocity(
    @SerializedName("kilometers_per_second")
    val kilometersPerSecond: String
)

data class MissDistance(
    val kilometers: String
)

data class PageInfo(
    val size: Int,
    @SerializedName("total_elements")
    val totalElements: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    val number: Int
)
