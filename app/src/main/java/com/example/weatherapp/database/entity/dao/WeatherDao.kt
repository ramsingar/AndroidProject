package com.example.weatherapp.database.entity.dao
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.weatherapp.database.entity.WeatherEntity

@Dao
interface WeatherDao {

    @Insert
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather_data WHERE city = :cityName LIMIT 1")
    fun getWeatherByCity(cityName: String): LiveData<WeatherEntity>

}