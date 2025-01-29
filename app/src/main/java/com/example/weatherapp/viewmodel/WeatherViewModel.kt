package com.example.weatherapp.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.api.RetrofitClient
import com.example.weatherapp.database.entity.WeatherEntity
import com.example.weatherapp.database.entity.dao.WeatherDao
import com.example.weatherapp.database.entity.db.WeatherDatabase
import com.example.weatherapp.model.WeatherResponse
import kotlinx.coroutines.launch
import retrofit2.Response


class WeatherViewModel(private val weatherDao: WeatherDao) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherEntity>()
    val weatherData: LiveData<WeatherEntity> = _weatherData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage


    // Function to get weather from API and save to DB
    fun getWeatherFromApi(city: String) {
        viewModelScope.launch {
            try {
                Log.d("DataCity","responseCity: ${RetrofitClient.weatherService.getWeatherByCity(city)}")
                val response = RetrofitClient.weatherService.getWeatherByCity(city)
                println("responseData:${response.body()}")
                Log.d("DataCity","responseCityBody: ${response.body()}")

                if (response.isSuccessful) {
                    val weatherData = response.body()
                    weatherData?.let {
                        val weatherEntity = WeatherEntity(
                            city = city,
                            temp = it.temp,
                            description = it.description,
                            icon = it.icon
                        )
                        saveWeatherToDatabase(weatherEntity)
                    }
                } else {
                    Log.d("DataCity","responseCityBody: ${response.body()}")

                    _errorMessage.postValue("Error fetching weather data.")
                    getWeatherFromDatabase(city)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Network error: ${e.message}")
                getWeatherFromDatabase(city)
            }
        }
    }

    // Save weather to the local database
    private suspend fun saveWeatherToDatabase(weather: WeatherEntity) {
        weatherDao.insertWeather(weather)
        _weatherData.postValue(weather)
    }

    // Fetch weather from the local database
    private fun getWeatherFromDatabase(city: String) {
        viewModelScope.launch {
            weatherDao.getWeatherByCity(city).observeForever {
                it?.let {
                    _weatherData.postValue(it)
                } ?: run {
                    _errorMessage.postValue("No data available.")
                }
            }
        }
    }

    // Convert temperature based on selected unit
    fun convertTemperature(tempInCelsius: Double, isCelsius: Boolean): String {
        return if (isCelsius) {
            "$tempInCelsius°C"
        } else {
            val tempInFahrenheit = tempInCelsius * 9 / 5 + 32
            "$tempInFahrenheit°F"
        }
    }

}




