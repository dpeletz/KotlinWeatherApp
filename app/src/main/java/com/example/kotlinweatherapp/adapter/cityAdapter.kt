package com.example.kotlinweatherapp.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kotlinweatherapp.DetailsActivity
import com.example.kotlinweatherapp.R
import com.example.kotlinweatherapp.ScrollingActivity
import com.example.kotlinweatherapp.data.AppDatabase
import com.example.kotlinweatherapp.data.City
import com.example.shoppinglistkotlin.touch.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.city_row.view.*
import java.util.*

class cityAdapter : RecyclerView.Adapter<cityAdapter.ViewHolder>, ItemTouchHelperCallback {

    var cities = mutableListOf<City>()
    private val context: Context

    constructor(context: Context, listCities: List<City>) : super() {
        this.context = context
        cities.addAll(listCities)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val cityRowView = LayoutInflater.from(context).inflate(
            R.layout.city_row, viewGroup, false
        )
        return ViewHolder(cityRowView)
    }

    override fun getItemCount(): Int {
        return cities.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val city = cities.get(position)

        viewHolder.tvCityName.text = city.cityName

        setOnClickListeners(viewHolder, city)
    }

    override fun onDismissed(position: Int) {
        deleteCity(position)
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(cities, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun setOnClickListeners(
        viewHolder: ViewHolder,
        city: City
    ) {
        viewHolder.btnDelete.setOnClickListener {
            deleteCity(viewHolder.adapterPosition)
        }

        viewHolder.tvCityName.setOnClickListener {
            var cityIntent = Intent(context, DetailsActivity::class.java)
//            val cityName =
            cityIntent.putExtra(context.resources.getString(R.string.city_name), viewHolder.tvCityName.text)
            context.startActivity(cityIntent)
        }

        viewHolder.btnEdit.setOnClickListener {
            (context as ScrollingActivity).showEditCityDialog(
                city, viewHolder.adapterPosition
            )
        }
    }

    fun updateCity(city: City) {
        Thread {
            AppDatabase.getInstance(context).cityDao().updateCity(city)
        }.start()
    }

    fun updateCity(city: City, editIndex: Int) {
        cities[editIndex] = city
        notifyItemChanged(editIndex)
    }

    fun addCity(city: City) {
        cities.add(0, city)
        notifyItemInserted(0)
    }

    fun removeAll() {
        cities.clear()
        notifyDataSetChanged()
    }

    fun deleteCity(deletePosition: Int) {
        Thread { deleteDBCity(deletePosition) }.start()
    }

    private fun deleteDBCity(deletePosition: Int) {
        AppDatabase.getInstance(context).cityDao().deleteCity(cities.get(deletePosition))
        (context as ScrollingActivity).runOnUiThread {
            cities.removeAt(deletePosition)
            notifyItemRemoved(deletePosition)
        }
    }

    inner class ViewHolder(cityView: View) : RecyclerView.ViewHolder(cityView) {
        var tvCityName = cityView.tvCityName
        var btnDelete = cityView.btnDelete
        var btnEdit = cityView.btnEdit
    }
}