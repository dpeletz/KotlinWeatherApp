package com.example.kotlinweatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kotlinweatherapp.data.WeatherResult
import com.example.kotlinweatherapp.network.WeatherAPI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

@Suppress("KotlinDeprecation")
class DetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        setUpAPIRetrofitAndMapFragment()
    }

    private fun setUpAPIRetrofitAndMapFragment() {
        val cityName = intent.getStringExtra(getString(R.string.city_name))
        val retrofit = Retrofit.Builder().baseUrl(getString(R.string.base_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val weatherAPI = retrofit.create(WeatherAPI::class.java)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setUpAPICall(weatherAPI, cityName)
    }

    private fun setUpAPICall(weatherAPI: WeatherAPI, cityName: String) {
        val call = weatherAPI
            .getWeatherDetails(cityName, getString(R.string.imperial), getString(R.string.app_id))
        enqueueCall(call, cityName)
    }

    private fun enqueueCall(call: Call<WeatherResult>, cityName: String) {
        call.enqueue(object : Callback<WeatherResult> {
            override fun onResponse(call: Call<WeatherResult>, response: Response<WeatherResult>) {
                val weatherResult = response.body()

                setUpGlideIconAndCityName(cityName, response)
                setTemperatureTexts(weatherResult)
                setHumidityDescriptionTexts(weatherResult)
                setSunriseSunsetTexts(weatherResult)
                setUpMap(weatherResult, cityName)
            }

            override fun onFailure(call: Call<WeatherResult>, t: Throwable) {
                tvHumidity.text = t.message
            }
        })
    }

    private fun setHumidityDescriptionTexts(weatherResult: WeatherResult?) {
        tvHumidity.text = getString(R.string.humidity, weatherResult?.main?.humidity)
        val description = weatherResult?.weather?.first()?.description
        tvDescription.text =
            getString(R.string.description, description)
    }

    private fun setUpMap(weatherResult: WeatherResult?, cityName: String) {
        val cityLat = weatherResult?.coord?.lat!!.toDouble()
        val cityLong = weatherResult?.coord?.lon!!.toDouble()
        val city = LatLng(cityLat, cityLong)
        mMap.addMarker(MarkerOptions().position(city).title(getString(R.string.city_location, cityName)))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(city))
    }

    @SuppressLint("SimpleDateFormat")
    private fun setSunriseSunsetTexts(weatherResult: WeatherResult?) {
        val calendar = Calendar.getInstance()

        val sunriseUnixSeconds: Long = weatherResult?.sys?.sunrise!!.toLong()
        calendar.timeInMillis = sunriseUnixSeconds * 1000L
        val formattedSunrise =
            SimpleDateFormat(getString(R.string.time_pattern)).format(calendar.time)
        val sunsetUnixSeconds: Long = weatherResult?.sys?.sunset!!.toLong()
        calendar.timeInMillis = sunsetUnixSeconds * 1000L
        val formattedSunset =
            SimpleDateFormat(getString(R.string.time_pattern)).format(calendar.time)

        tvSunrise.text = getString(R.string.sunrise, formattedSunrise)
        tvSunset.text = getString(R.string.sunset, formattedSunset)
    }

    private fun setTemperatureTexts(weatherResult: WeatherResult?) {
        tvCurrentTemperature.text =
            getString(R.string.current_temperature, weatherResult?.main?.temp).plus(0x00B0.toChar())
                .plus(getString(R.string.f))
        tvMaxTemperature.text =
            getString(R.string.max_temperature, weatherResult?.main?.temp_max).plus(0x00B0.toChar())
                .plus(getString(R.string.f))
        tvMinTemperature.text =
            getString(R.string.min_temperature, weatherResult?.main?.temp_min).plus(0x00B0.toChar())
                .plus(getString(R.string.f))
    }

    private fun setUpGlideIconAndCityName(
        cityName: String,
        response: Response<WeatherResult>
    ) {
        tvCityNameDetails.text = cityName
        Glide.with(this@DetailsActivity)
            .load(
                (getString(R.string.image_url) +
                        response.body()?.weather?.get(0)?.icon
                        + getString(R.string.png))
            ).into(ivWeatherIcon)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}