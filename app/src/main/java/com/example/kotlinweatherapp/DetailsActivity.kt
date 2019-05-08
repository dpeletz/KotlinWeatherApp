package com.example.kotlinweatherapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kotlinweatherapp.data.WeatherResult
import com.example.kotlinweatherapp.network.WeatherAPI
import kotlinx.android.synthetic.main.activity_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        var cityName = intent.getStringExtra(getString(R.string.city_name))

        var retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.base_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var weatherAPI = retrofit.create(WeatherAPI::class.java)

        val call = weatherAPI.getWeatherDetails(
            cityName,
            getString(R.string.imperial),
            getString(R.string.app_id)
        )

        call.enqueue(object : Callback<WeatherResult> {
            override fun onResponse(call: Call<WeatherResult>, response: Response<WeatherResult>) {
                tvCityNameDetails.setText(cityName)

                val weatherResult = response.body()

                Glide.with(this@DetailsActivity)
                    .load(
                        (getString(R.string.image_url) +
                                response.body()?.weather?.get(0)?.icon
                                + getString(R.string.png))
                    ).into(ivWeatherIcon)

                tvCurrentTemperature.text =
                    getString(R.string.current_temperature, weatherResult?.main?.temp)
                tvMaxTemperature.text =
                    getString(R.string.max_temperature, weatherResult?.main?.temp_max)
                tvMinTemperature.text =
                    getString(R.string.min_temperature, weatherResult?.main?.temp_min)
                tvHumidity.text =
                    getString(R.string.humidity, weatherResult?.main?.humidity)
                val description = weatherResult?.weather?.first()?.description
                tvDescription.text =
                    getString(R.string.description, description)

                val calendar = Calendar.getInstance()

                val sunriseUnixSeconds: Long = weatherResult?.sys?.sunrise!!.toLong()
                calendar.setTimeInMillis(sunriseUnixSeconds * 1000L)
                val formattedSunrise =
                    SimpleDateFormat(getString(R.string.time_pattern)).format(calendar.time)
                tvSunrise.text = getString(R.string.sunrise, formattedSunrise)

                val sunsetUnixSeconds: Long = weatherResult?.sys?.sunset!!.toLong()
                calendar.setTimeInMillis(sunsetUnixSeconds * 1000L)
                val formattedSunset =
                    SimpleDateFormat(getString(R.string.time_pattern)).format(calendar.time)
                tvSunset.text = getString(R.string.sunset, formattedSunset)

            }

            override fun onFailure(call: Call<WeatherResult>, t: Throwable) {
                tvHumidity.text = t.message
            }
        })
    }
}