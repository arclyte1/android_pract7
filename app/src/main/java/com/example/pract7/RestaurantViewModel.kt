package com.example.pract7

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pract7.api.RetrofitClient
import com.example.pract7.model.Restaurant

class RestaurantViewModel : ViewModel() {
    val restaurants: MutableLiveData<List<Restaurant>> by lazy {
        MutableLiveData<List<Restaurant>>()
    }
    val restaurant: MutableLiveData<Restaurant> by lazy {
        MutableLiveData<Restaurant>()
    }
}