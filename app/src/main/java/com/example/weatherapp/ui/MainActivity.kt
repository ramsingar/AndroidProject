package com.example.weatherapp.ui

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.database.entity.db.WeatherDatabase
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.factory.WeatherViewModelFactory
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.util.concurrent.ListenableFuture
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {
    var fullAddress: String? = ""
    var cityName: String? = ""
    var countryName: String? = ""
    var stateName: String? = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: WeatherViewModel
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()
        // Initialize the Room Database
        val weatherDao = WeatherDatabase.getDatabase(application).weatherDao()

        // Initialize the ViewModel with the WeatherDao
        val factory = WeatherViewModelFactory(weatherDao)
        viewModel = ViewModelProvider(this, factory).get(WeatherViewModel::class.java)

        // Observe weather data and update the UI
        viewModel.weatherData.observe(this) { weather ->
            binding.tvWeather.text = "Temperature: ${weather.temp}°C\nDescription: ${weather.description}"
            // You can also show weather icons here using the icon code
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }

        binding.btnSearch.setOnClickListener {
            val city = binding.etCity.text.toString()
            viewModel.getWeatherFromApi(city)
        }

        binding.switchUnit.setOnCheckedChangeListener { _, isChecked ->
            val unit = if (isChecked) "Fahrenheit" else "Celsius"
            val tempInCelsius = viewModel.weatherData.value?.temp ?: 0.0
            val convertedTemp = viewModel.convertTemperature(tempInCelsius, isChecked)
            binding.tvWeather.text = "Temperature: $convertedTemp\nDescription: ${viewModel.weatherData.value?.description}"
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude

                val geocoder = Geocoder(this, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        fullAddress = addresses!![0].getAddressLine(0)
                        cityName = addresses!![0].locality
                        countryName = addresses!![0].countryName
                        stateName = addresses!![0].adminArea
                    }
                } else {
                    val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        fullAddress = addresses!![0].getAddressLine(0)
                        cityName = addresses!![0].locality
                        countryName = addresses!![0].countryName
                        stateName = addresses!![0].adminArea
                    }
                }
                Toast.makeText(this, fullAddress, Toast.LENGTH_SHORT).show()
                Toast.makeText(this, cityName, Toast.LENGTH_SHORT).show()
                Toast.makeText(this, countryName, Toast.LENGTH_SHORT).show()
                Toast.makeText(this, stateName, Toast.LENGTH_SHORT).show()

                // Use latitude and longitude
                    viewModel.getWeatherFromApi(cityName!!)
            }
        }
    }
}


