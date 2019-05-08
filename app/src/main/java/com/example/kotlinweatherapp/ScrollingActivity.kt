package com.example.kotlinweatherapp

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.kotlinweatherapp.adapter.cityAdapter
import com.example.kotlinweatherapp.data.AppDatabase
import com.example.kotlinweatherapp.data.City
import com.example.kotlinweatherapp.touch.ItemRecyclerTouchCallback
import kotlinx.android.synthetic.main.activity_scrolling.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class ScrollingActivity : AppCompatActivity(), CityDialog.CityHandler {

    lateinit var cityAdapter: cityAdapter
    var editIndex: Int = -1

    companion object {
        val KEY_CITY_TO_EDIT = R.string.key_city_to_edit.toString()
    }

    override fun cityCreated(city: City) {
        Thread { insertCityAndRunOnUiThread(city) }.start()
    }

    override fun cityUpdated(city: City) {
        Thread { updateCityAndRunOnUiThread(city) }.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)

        setSupportActionBar(toolbar)
        setOnClickListeners()

        if (!wasOpenedEarlier()) {
            setUpFabPrompt()
        }
        saveFirstOpenInfo()
        initRecyclerViewFromDB()
    }

    private fun insertCityAndRunOnUiThread(city: City) {
        val cityId = AppDatabase.getInstance(
            this@ScrollingActivity
        ).cityDao().insertCity(city)
        city.cityId = cityId
        runOnUiThread {
            cityAdapter.addCity(city)
        }

    }


    private fun updateCityAndRunOnUiThread(city: City) {
        AppDatabase.getInstance(
            this@ScrollingActivity
        ).cityDao().updateCity(city)
        runOnUiThread {
            cityAdapter.updateCity(city, editIndex)
        }
    }


    private fun setUpFabPrompt() {
        MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.fab)
            .setPrimaryText(getText(R.string.add_city).toString())
            .setSecondaryText(getText(R.string.add_click_hint).toString())
            .show()
    }

    private fun setOnClickListeners() {
        var demoAnim = AnimationUtils.loadAnimation(
            this@ScrollingActivity, R.anim.demo_anim
        )

        demoAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })

        fab.setOnClickListener { view ->
            fab.startAnimation(demoAnim)
            showAddCityDialog()
        }

        btnDeleteAll.setOnClickListener {
            btnDeleteAll.startAnimation(demoAnim)
            Thread {
                AppDatabase.getInstance(this@ScrollingActivity).cityDao().deleteAll()
                runOnUiThread {
                    cityAdapter.removeAll()
                }
            }.start()
        }
    }

    fun saveFirstOpenInfo() {
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        var editor = sharedPref.edit()
        editor.putBoolean(getText(R.string.key_open).toString(), true)
        editor.apply()
    }

    fun wasOpenedEarlier(): Boolean {
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPref.getBoolean(getText(R.string.key_open).toString(), false)
    }

    private fun initRecyclerViewFromDB() {
        Thread { getCitiesAndRunOnUiThread() }.start()
    }

    private fun getCitiesAndRunOnUiThread() {
        var listCities =
            AppDatabase.getInstance(this@ScrollingActivity).cityDao().getAllCities()

        runOnUiThread {
            cityAdapter = cityAdapter(this, listCities)
            recyclerItem.layoutManager = LinearLayoutManager(this)
            recyclerItem.adapter = cityAdapter

            val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
            recyclerItem.addItemDecoration(itemDecoration)

            val callback = ItemRecyclerTouchCallback(cityAdapter)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(recyclerItem)
        }
    }

    private fun showAddCityDialog() {
        CityDialog().show(supportFragmentManager, getText(R.string.tag_item).toString())
    }

    fun showEditCityDialog(cityToEdit: City, idx: Int) {
        editIndex = idx
        val editCityDialog = CityDialog()
        val bundle = Bundle()

        bundle.putSerializable(KEY_CITY_TO_EDIT, cityToEdit)
        editCityDialog.arguments = bundle
        editCityDialog.show(supportFragmentManager, R.string.edit_city_dialog.toString())
    }
}