package com.eos.airquality

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eos.airquality.databinding.ActivityMainBinding
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eos.airquality.retrofit.AirQualityResponse
import com.eos.airquality.retrofit.AirQualityService
import com.eos.airquality.retrofit.RetrofitConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val PERMISSION_REQUEST_CODE = 100

    var REQUIRED_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>
    lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()
        updateUI()
        setRefreshButton()
    }

    private fun setRefreshButton() {
        binding.btnRefresh.setOnClickListener {
            updateUI()
        }
    }

    private fun getCurrentAddress(latitude: Double, longitude: Double): Address? {
        // GeoCoding : 주소나 지명 <-> 위도, 경도
        val geocoder = Geocoder(this, Locale.getDefault())
        // Address 객체는 주소와 관련된 여러 정보를 가지고 있습니다.
        // android.location.Address 패키지 참조.

        val addresses: List<Address>? = try {
            // Geocoder 객체를 이용하여 위도와 경도로부터 리스트를 가져옵니다.
            geocoder.getFromLocation(latitude, longitude, 7)
        } catch (ioException: IOException) {
            Toast.makeText(
                this,
                "지오코더 서비스 사용불가합니다.",
                Toast.LENGTH_LONG
            ).show()
            return null
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(
                this,
                "잘못된 위도, 경도입니다.",
                Toast.LENGTH_LONG
            ).show()
            return null
        }

        // 에러는 아니지만 주소가 발견되지 않았을 경우
        if (addresses == null || addresses.isEmpty()) {
            Toast.makeText(
                this,
                "주소가 발견되지 않았습니다.",
                Toast.LENGTH_LONG
            ).show()
            return null
        }

        return addresses[0]
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        locationProvider = LocationProvider(this@MainActivity)

        val latitude: Double = locationProvider.getLocationLatitude()
        val longitude: Double = locationProvider.getLocationLongitude()

        Log.d("TTAAGG", "$latitude, $longitude")

        if (latitude != 0.0 && longitude != 0.0) {
            // 1. 현재 위치를 가져오고 UI 업데이트
            // 현재 위치를 가져오기
            val address = getCurrentAddress(latitude, longitude)
            address?.let {
                binding.tvLocationTitle.text = it.thoroughfare
                binding.tvLocationSubTitle.text = "${it.countryName} ${it.adminArea}"
            }
            // 2. 현재 미세먼지 농도를 가져오고 UI 업데이트
            getAirQualityData(latitude, longitude)


        } else {
            Toast.makeText(
                this@MainActivity,
                "위도, 경도 정보를 가져올 수 없습니다. 새로고침을 눌러주십시오.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getAirQualityData(latitude: Double, longitude: Double) {
        val retrofitAPI = RetrofitConnection.getInstance().create(AirQualityService::class.java)

        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            "98fb5959-0fb7-4a87-9f48-e23ebd83357d"
        ).enqueue(object : Callback<AirQualityResponse> {
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ) {
                // 정상적인 response 가 왔다면 UI 업데이트
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity,
                        "최신 정보 업데이트 완료", Toast.LENGTH_SHORT).show()
                    response.body()?.let { updateAirUI(it) }
                } else {
                    Toast.makeText(this@MainActivity,
                        "업데이트에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MainActivity, "업데이트에 실패하였습니다.",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAirUI(airQualityData: AirQualityResponse) {
        val pollutionData = airQualityData.data.current.pollution

        // 수치 지정
        binding.tvCount.text = pollutionData.aqius.toString()

        // 측정된 날짜 지정
        val dateTime = ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(
            ZoneId.of("Asia/Seoul"))
            .toLocalDateTime()
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm")

        binding.tvCheckTime.text = dateTime.format(dateFormatter).toString()

        when (pollutionData.aqius) {
            in 0..50 -> {
                binding.tvTitle.text = "좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }

            in 51..150 -> {
                binding.tvTitle.text = "보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }

            in 151..200 -> {
                binding.tvTitle.text = "나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }

            else -> {
                binding.tvTitle.text = "매우 나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }

    private fun checkAllPermissions() {
        if (!isLocationServicesAvailable()) {
            showDialogForLocationServiceSetting()
        } else {
            isRunTimePermissionGranted()
        }
    }

    private fun isLocationServicesAvailable() : Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE)
                as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    private fun isRunTimePermissionGranted() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED
            || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE
            && grantResults.size == REQUIRED_PERMISSION.size) {
            var checkResult = true

            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if (checkResult) {
                // 위치값을 가져올 수 있음
                updateUI()
            } else {
                // 거부되었다면 앱 종료
                Toast.makeText(
                    this@MainActivity,
                    "권한이 거부되었습니다. 앱을 다시 실행하여 권한을 허용해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun showDialogForLocationServiceSetting() {
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            // 결과값을 받았을 때의 로직
            if (result.resultCode == Activity.RESULT_OK) {
                // 사용자가 GPS를 활성화시켰는지 확인
                if (isLocationServicesAvailable()) {
                    isRunTimePermissionGranted() // 런타임 권한 확인
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "위치 서비스를 사용할 수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish() // 액티비티 종료
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스가 꺼져 있습니다. 설정해야 앱을 실행할 수 있습니다.")
        builder.setCancelable(true) // 창 바깥 터치 시 창 닫힘
        builder.setPositiveButton("설정"
        ) { dialog, id ->
            val callGPSSettingIntent = Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        }
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
                Toast.makeText(this@MainActivity,
                    "기기에서 GPS 설정 후 사용해주세요.",
                    Toast.LENGTH_SHORT).show()
                finish()
            })
        builder.create().show()
    }
}