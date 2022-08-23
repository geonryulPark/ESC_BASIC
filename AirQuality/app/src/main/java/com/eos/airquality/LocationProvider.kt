package com.eos.airquality

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.lang.Exception

class LocationProvider(val context: Context) {
    // Location 은 위도, 경도, 고도와 같이
    // 위치에 관련된 정보를 가지고 있는 클래스
    private var location: Location? = null
    // Location Manager 는 시스템 위치 서비스에 접근을 제공하는 클래스
    private var locationManager: LocationManager? = null

    init {
        getLocation()
    }

    private fun getLocation() : Location? {
        try {
            locationManager = context.getSystemService(
                Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation: Location? = null
            var networkLocation: Location? = null

            // GPS Provider, Network Provider 가 활성화 되어있는지 확인
            val isGPSEnabled: Boolean =
                locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled: Boolean =
                locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) {
                // 둘 다 사용할 수 없는 경우
                return null
            } else {
                val hasFineLocationPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                        // Access_coarse_location 보다 더 정밀한 위치 정보 얻기
                    )
                val hasCoarseLocationPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    // 도시 Block 단위의 정밀도의 위치 정보를 얻기
                    )

                // 위 두 개의 권한이 없다면 null 을 반환하기
                if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED
                    || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    return null
                }

                // 네트워크를 통한 위치 파악이 가능한 경우에 위치를 가져옵니다.
                if (isNetworkEnabled) {
                    networkLocation =
                        locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//                    Log.d("TTAAGG", "networkLocation : $networkLocation")
                }

                // GPS 를 통한 위치 파악이 가능한 경우에 위치를 가져옵니다.
                if (isGPSEnabled) {
                    gpsLocation =
                        locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//                    Log.d("TTAAGG", "gpsLocation : $gpsLocation")
                }

                if (gpsLocation != null && networkLocation != null) {
                    // 둘 다 가용 가능하다면 정확도가 더 높은 것으로 선택
                    return if (gpsLocation.accuracy > networkLocation.accuracy) {
                        location = gpsLocation
                        gpsLocation
                    } else {
                        location = networkLocation
                        networkLocation
                    }
                } else {
                    // 가용한 위치 정보가 한 개만 있을 경우
                    location = gpsLocation ?: networkLocation
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return location
    }

    fun getLocationLatitude(): Double {
        return location?.latitude ?: 0.0
    }

    fun getLocationLongitude(): Double {
        return location?.longitude ?: 0.0
    }
}