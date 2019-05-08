package com.example.kotlinweatherapp.data

import android.arch.persistence.room.*

@Dao
interface CityDAO {
    @Query("SELECT * FROM city")
    fun getAllCities(): List<City>

    @Query("DELETE FROM city")
    fun deleteAll()

    @Insert
    fun insertCity(city: City): Long

    @Delete
    fun deleteCity(city: City)

    @Update
    fun updateCity(city: City)
}