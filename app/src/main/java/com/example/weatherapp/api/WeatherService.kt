package com.example.weatherapp.api
import com.example.weatherapp.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("v1/weather")
    suspend fun getWeatherByCity(
        @Query("city") city: String,
        @Query("key") apiKey: String = "YOUR_API_KEY"
    ): Response<WeatherResponse>
}
