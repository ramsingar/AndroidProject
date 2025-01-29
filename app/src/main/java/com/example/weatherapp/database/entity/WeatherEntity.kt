package com.example.weatherapp.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_data")
data class WeatherEntity(
    @PrimaryKey val id: Int = 0,
    val city: String,
    val temp: Double,
    val description: String,
    val icon: String
)