package com.example.androidlabfinal

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.squareup.picasso.Picasso
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocation: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermission()) {
            if (locationEnable()) {
                fusedLocation.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val lat = location.latitude.toString()
                        val long = location.longitude.toString()

                        getJsonData(lat, long)
                    }
                }
            } else {
                Toast.makeText(this, "Please Turn on your GPS location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermission()
        }
    }

    private fun locationEnable(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        try {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1001
            )
        } catch (e: Exception) {

        }
    }

    private fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                requestPermission()
            }
        }
    }

    private fun getJsonData(lat: String, long: String) {
        val apiKey = "bbd0d0f1e9bedd121a13ca33a096f067" // use your own api_key
        val queue = Volley.newRequestQueue(this)
        val cityName = getCityName(lat.toDouble(), long.toDouble())

        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=${cityName}&appid=${apiKey}"

        try {
            val jsonRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    setValues(response)
                },
                {
                    Toast.makeText(
                        this,
                        "Please turn on internet connection",
                        Toast.LENGTH_LONG
                    ).show()
                })


            queue.add(jsonRequest)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "ERROR" + e.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setValues(response: JSONObject) {
        val weatherIcon = response.getJSONArray("weather").getJSONObject(0).getString("icon")
        runOnUiThread{
            Picasso.get().load("https://openweathermap.org/img/wn/${weatherIcon}@2x.png").into(weather_img)
            weather_img.visibility = View.VISIBLE
        }

        city.text = "City Name: " + response.getString("name")
        country.text = "Country: " + response.getJSONObject("sys").getString("country")
        val temperature = response.getJSONObject("main").getString("temp")
        temp.text = "Temperature: ${(temperature.toFloat() - 273).toInt()}°C"
        humidity.text = "Humidity: " + response.getJSONObject("main").getString("humidity") + "%"
    }

    private fun getCityName(lat: Double, long: Double): String {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat, long, 3)

        return address[0].locality
    }

}