package com.example.pract7.model

import com.google.gson.annotations.SerializedName

data class RestaurantData(
    @SerializedName("features")
    val restaurants: List<Restaurant>
)

data class Restaurant(
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val coordinates: List<Double>
)

data class Properties(
    @SerializedName("CompanyMetaData")
    val metaData: MetaData
)

data class MetaData(
    val id: Long,
    val name: String,
    val address: String,
    var url: String,
    @SerializedName("Phones")
    val phones: List<Phone>,
    @SerializedName("Categories")
    val categories: List<Category>,
    @SerializedName("Hours")
    val hours: Hours,
)

data class Phone(
    val formatted: String
)

data class Category(
    val name: String
) {
    override fun toString(): String {
        return name
    }
}

data class Hours(
    val text: String
)