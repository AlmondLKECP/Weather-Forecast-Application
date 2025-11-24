package com.example.project_final

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var provinceSpinner: Spinner
    private lateinit var resultText: TextView
    private lateinit var checkWeatherButton: Button
    private lateinit var checkCurrentButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiKey = "924355e2a4bfd5d3665dade58b276baf"

    private val thailandCities = mapOf(
        "กรุงเทพมหานคร" to "Bangkok", "กระบี่" to "Krabi", "กาญจนบุรี" to "Kanchanaburi",
        "กาฬสินธุ์" to "Kalasin", "กำแพงเพชร" to "Kamphaeng Phet", "ขอนแก่น" to "Khon Kaen",
        "จันทบุรี" to "Chanthaburi", "ฉะเชิงเทรา" to "Chachoengsao", "ชลบุรี" to "Chon Buri",
        "ชัยนาท" to "Chainat", "ชัยภูมิ" to "Chaiyaphum", "เชียงราย" to "Chiang Rai",
        "เชียงใหม่" to "Chiang Mai", "ตรัง" to "Trang", "ตราด" to "Trat", "ตาก" to "Tak",
        "นครนายก" to "Nakhon Nayok", "นครปฐม" to "Nakhon Pathom", "นครพนม" to "Nakhon Phanom",
        "นครราชสีมา" to "Nakhon Ratchasima", "นครศรีธรรมราช" to "Nakhon Si Thammarat",
        "นครสวรรค์" to "Nakhon Sawan", "นนทบุรี" to "Nonthaburi", "นราธิวาส" to "Narathiwat",
        "น่าน" to "Nan", "บึงกาฬ" to "Bueng Kan", "บุรีรัมย์" to "Buri Ram", "ปทุมธานี" to "Pathum Thani",
        "ประจวบคีรีขันธ์" to "Prachuap Khiri Khan", "ปราจีนบุรี" to "Prachin Buri", "ปัตตานี" to "Pattani",
        "พระนครศรีอยุธยา" to "Phra Nakhon Si Ayutthaya", "พังงา" to "Phang Nga", "พัทลุง" to "Phatthalung",
        "พิจิตร" to "Phichit", "พิษณุโลก" to "Phitsanulok", "เพชรบุรี" to "Phetchaburi",
        "เพชรบูรณ์" to "Phetchabun", "แพร่" to "Phrae", "ภูเก็ต" to "Phuket", "มหาสารคาม" to "Maha Sarakham",
        "มุกดาหาร" to "Mukdahan", "แม่ฮ่องสอน" to "Mae Hong Son", "ยโสธร" to "Yasothon",
        "ยะลา" to "Yala", "ร้อยเอ็ด" to "Roi Et", "ระนอง" to "Ranong", "ระยอง" to "Rayong",
        "ราชบุรี" to "Ratchaburi", "ลพบุรี" to "Lop Buri", "ลำปาง" to "Lampang", "ลำพูน" to "Lamphun",
        "เลย" to "Loei", "ศรีสะเกษ" to "Si Sa Ket", "สกลนคร" to "Sakon Nakhon",
        "สงขลา" to "Songkhla", "สตูล" to "Satun", "สมุทรปราการ" to "Samut Prakan",
        "สมุทรสงคราม" to "Samut Songkhram", "สมุทรสาคร" to "Samut Sakhon",
        "สระแก้ว" to "Sa Kaeo", "สระบุรี" to "Saraburi", "สิงห์บุรี" to "Sing Buri",
        "สุโขทัย" to "Sukhothai", "สุพรรณบุรี" to "Suphan Buri", "สุราษฎร์ธานี" to "Surat Thani",
        "สุรินทร์" to "Surin", "หนองคาย" to "Nong Khai", "หนองบัวลำภู" to "Nong Bua Lam Phu",
        "อ่างทอง" to "Ang Thong", "อำนาจเจริญ" to "Amnat Charoen", "อุดรธานี" to "Udon Thani",
        "อุตรดิตถ์" to "Uttaradit", "อุทัยธานี" to "Uthai Thani", "อุบลราชธานี" to "Ubon Ratchathani"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        provinceSpinner = findViewById(R.id.spinnerProvince)
        resultText = findViewById(R.id.textResult)
        checkWeatherButton = findViewById(R.id.btnCheckWeather)
        checkCurrentButton = findViewById(R.id.btnCheckCurrent)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val provinces = thailandCities.keys.toList()
        provinceSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, provinces)

        checkWeatherButton.setOnClickListener {
            val provinceTH = provinceSpinner.selectedItem.toString()
            val provinceEN = thailandCities[provinceTH]
            if (provinceEN != null) fetchWeather(provinceEN, provinceTH)
            else resultText.text = "ไม่พบจังหวัดนี้ในฐานข้อมูล"
        }

        checkCurrentButton.setOnClickListener {
            getCurrentLocationWeather()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                fetchWeatherByLocation(location.latitude, location.longitude)
            } else {
                resultText.text = "ไม่สามารถดึงตำแหน่งได้"
            }
        }
    }

    private fun fetchWeatherByLocation(lat: Double, lon: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = "https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&appid=$apiKey&units=metric&lang=th"
                val queue = Volley.newRequestQueue(this@MainActivity)

                val request = JsonObjectRequest(Request.Method.GET, url, null,
                    { response ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            updateForecastUI(response)
                        }
                    },
                    {
                        lifecycleScope.launch(Dispatchers.Main) {
                            resultText.text = "❌ ไม่สามารถดึงข้อมูลสภาพอากาศจากตำแหน่งปัจจุบันได้"
                        }
                    }
                )
                queue.add(request)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    resultText.text = "เกิดข้อผิดพลาด: ${e.message}"
                }
            }
        }
    }

    private fun fetchWeather(cityEN: String, cityTH: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityEN,TH&appid=$apiKey&units=metric&lang=th"
            val queue = Volley.newRequestQueue(this@MainActivity)

            val request = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        updateWeatherUI(response, cityTH)
                    }
                },
                {
                    lifecycleScope.launch(Dispatchers.Main) {
                        resultText.text = "❌ ไม่พบข้อมูลสภาพอากาศสำหรับ $cityTH"
                    }
                }
            )
            queue.add(request)
        }
    }

    private fun updateWeatherUI(response: JSONObject, cityTH: String) {
        val main = response.getJSONObject("main")
        val weather = response.getJSONArray("weather").getJSONObject(0)
        val temp = main.getDouble("temp")
        val feelsLike = main.getDouble("feels_like")
        val desc = weather.getString("description")

        resultText.text = """
            📍 จังหวัด: $cityTH
            🌤 สภาพอากาศ: $desc
            🌡 อุณหภูมิ: $temp °C
            🧍‍♂️ รู้สึกเหมือน: $feelsLike °C
        """.trimIndent()
    }

    private fun updateForecastUI(response: JSONObject) {
        val city = response.getJSONObject("city").getString("name")
        val list = response.getJSONArray("list")
        val nextForecast = list.getJSONObject(1)
        val main = nextForecast.getJSONObject("main")
        val weather = nextForecast.getJSONArray("weather").getJSONObject(0)

        val temp = main.getDouble("temp")
        val desc = weather.getString("description")
        val time = nextForecast.getString("dt_txt")

        resultText.text = """
            📍 พื้นที่ปัจจุบัน: $city
            🕒 พยากรณ์ช่วงถัดไป: $time
            🌤 สภาพอากาศ: $desc
            🌡 อุณหภูมิ: $temp °C
        """.trimIndent()
        val timeNow = SimpleDateFormat("HH:mm", Locale("th", "TH")).format(Date())
        findViewById<TextView>(R.id.textUpdatedTime).text = "อัปเดตล่าสุด: $timeNow น."
    }
}
