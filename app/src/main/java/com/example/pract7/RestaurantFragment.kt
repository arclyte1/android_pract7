package com.example.pract7

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.pract7.api.RetrofitClient
import com.example.pract7.databinding.FragmentRestaurantBinding
import com.example.pract7.model.Restaurant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*
import kotlin.random.Random

class RestaurantFragment : Fragment() {

    private lateinit var binding: FragmentRestaurantBinding
    private val viewModel: RestaurantViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_restaurant, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentRestaurantBinding.bind(view)
        binding.site.visibility = View.INVISIBLE
        binding.phone.visibility = View.INVISIBLE

        // Кнопка показа случайного ресторана
        binding.anotherButton.setOnClickListener {
            showRandomRestaurant()
        }

        // Кнопка показа ресторана на карте
        binding.showMapButton.setOnClickListener {
            showMap(viewModel.restaurant.value!!)
        }

        // Кнопка отправки письма
        binding.emailButton.setOnClickListener {
            sendEmail(viewModel.restaurant.value!!)
        }

        // Кнопка планирования похода в ресторан
        binding.dateButton.setOnClickListener {
            planDate(viewModel.restaurant.value!!)
        }

        if (viewModel.restaurant.value != null)
            showRestaurant(viewModel.restaurant.value!!)
        else
            showRandomRestaurant()
    }

    // Показ случайного ресторана на экране
    private fun showRandomRestaurant() {
        if (!viewModel.restaurants.value.isNullOrEmpty()) { // Если мы уже получили список ресторанов
            val restaurant = viewModel.restaurants.value!![Random.nextInt(viewModel.restaurants.value!!.size)]
            try {
                showRestaurant(restaurant)
            }
            catch (e: Exception) {
                showRandomRestaurant()
            }
        }
        else { // Если не получили список ресторанов, получаем
            CoroutineScope(Dispatchers.IO).launch {
                val response = RetrofitClient.apiInterface.getRestaurants("Где поесть, Москва", "biz", "ru_RU", BuildConfig.MAPS_KEY, 500)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        viewModel.restaurants.value = response.body()?.restaurants!!
                        showRandomRestaurant()
                    } else {
                        Log.e("RETROFIT_ERROR", response.code().toString())
                    }
                }
            }
        }
    }

    // Показ ресторана на экране
    private fun showRestaurant(restaurant: Restaurant) {
        binding.site.visibility = View.VISIBLE
        binding.phone.visibility = View.VISIBLE
        binding.name.text = restaurant.properties.metaData.name
        binding.address.text = restaurant.properties.metaData.address
        binding.category.text = restaurant.properties.metaData.categories.joinToString(separator = ", ")
        binding.url.text = restaurant.properties.metaData.url
        binding.url.setOnClickListener {
            if (restaurant.properties.metaData.url != null && (
                        restaurant.properties.metaData.url.startsWith("http://") ||
                                restaurant.properties.metaData.url.startsWith("https://"))) {
                val uri = Uri.parse(restaurant.properties.metaData.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
        binding.phoneNumber.text = restaurant.properties.metaData.phones[0].formatted
        binding.hours.text = restaurant.properties.metaData.hours.text
        viewModel.restaurant.value = restaurant
    }

    // Показ ресторана на карте
    private fun showMap(restaurant: Restaurant) {
        val id = restaurant.properties.metaData.id
        val uri = Uri.parse("yandexmaps://maps.yandex.ru/?oid=$id")
        val intent = Intent(Intent.ACTION_VIEW, uri) // Intent приложению Яндекс Карт
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Нет Яндекс Карт", Toast.LENGTH_LONG).show()
        }
    }

    // Отправка письма
    private fun sendEmail(restaurant: Restaurant) {
        val intent = Intent(Intent.ACTION_SENDTO).apply { // Intent почтовому приложению
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_TEXT, "Привет, пошли в ${restaurant.properties.metaData.name}")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Нет почтового приложения", Toast.LENGTH_LONG).show()
        }
    }

    // Запланировать дату посещения
    private fun planDate(restaurant: Restaurant) {
        // DatePickerDialog для получения даты от пользователя
        val datePicker = DatePickerDialog(requireContext())
        val c = Calendar.getInstance()
        datePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        datePicker.setOnDateSetListener { _, year, month, day -> // Обработка выбранной даты

            val date = Calendar.getInstance()
            date.set(year, month, day, 0, 0, 0)
            val intent = Intent(Intent.ACTION_INSERT).apply { // Intent к приложению календаря
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Поход в ${restaurant.properties.metaData.name}")
                putExtra(CalendarContract.Events.EVENT_LOCATION, restaurant.properties.metaData.address)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.timeInMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date.timeInMillis + 24 * 60 * 60 * 1000)
            }
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Нет календаря", Toast.LENGTH_LONG).show()
            }

        }
        datePicker.show()
    }

}