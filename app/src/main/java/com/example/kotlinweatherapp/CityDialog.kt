package com.example.kotlinweatherapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import com.example.kotlinweatherapp.data.City
import kotlinx.android.synthetic.main.new_city_dialog.view.*
import java.lang.RuntimeException

class CityDialog : DialogFragment(), AdapterView.OnItemSelectedListener {
    interface CityHandler {
        fun cityCreated(city: City)
        fun cityUpdated(city: City)
    }

    private lateinit var cityHandler: CityHandler
    private lateinit var etCityName: EditText

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is CityHandler) cityHandler = context
        else throw RuntimeException(R.string.city_handler_error.toString())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val rootView = initializeRootView()
        builder.setTitle(R.string.new_city)
        builder.setView(rootView)
        setUpEditTextFields(builder)
        builder.setPositiveButton(R.string.ok) { dialog, witch -> }
        return builder.create()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}

    override fun onResume() {
        super.onResume()
        val positiveButton = (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener { createOrEditAfterCheckingFields() }
    }

    private fun setUpEditTextFields(builder: AlertDialog.Builder) {
        val arguments = this.arguments
        if (arguments != null && arguments.containsKey(ScrollingActivity.KEY_CITY_TO_EDIT)) {
            val city = arguments.getSerializable(ScrollingActivity.KEY_CITY_TO_EDIT) as City
            setTextForCityEdit(city, builder)
        }
    }

    private fun setTextForCityEdit(
        city: City,
        builder: AlertDialog.Builder
    ) {
        etCityName.setText(city.cityName)
        builder.setTitle(R.string.edit_city)
    }

    @SuppressLint("InflateParams")
    private fun initializeRootView(): View? {
        val rootView = requireActivity().layoutInflater.inflate(R.layout.new_city_dialog, null)
        etCityName = rootView.etCityName
        return rootView
    }

    private fun createOrEditAfterCheckingFields() {
        val formOk = checkAllFieldsValid()
        if (formOk) editOrCreateCity()
    }

    private fun checkAllFieldsValid(): Boolean {
        var formOk = true
        formOk = checkForEmptyTextFields(formOk)
        return formOk
    }

    private fun checkForEmptyTextFields(formOk: Boolean): Boolean {
        var formOk1 = formOk
        if (etCityName.text.isEmpty()) {
            etCityName.error = getText(R.string.empty_city_name).toString()
            formOk1 = false
        }
        return formOk1
    }

    private fun editOrCreateCity() {
        if (etCityName.text.isNotEmpty()) {
            val arguments = this.arguments
            if (arguments != null && arguments.containsKey(ScrollingActivity.KEY_CITY_TO_EDIT)) {
                handleCityEdit()
            } else {
                cityHandler.cityCreated(createCity())
            }
            dialog.dismiss()
        }
    }

    private fun createCity(): City {
        return City(
            null,
            etCityName.text.toString()
        )
    }

    private fun handleCityEdit() {
        val cityToEdit = arguments?.getSerializable(ScrollingActivity.KEY_CITY_TO_EDIT) as City
        editCity(cityToEdit)
        cityHandler.cityUpdated(cityToEdit)
    }

    private fun editCity(city: City) {
        city.cityName = etCityName.text.toString()
    }
}