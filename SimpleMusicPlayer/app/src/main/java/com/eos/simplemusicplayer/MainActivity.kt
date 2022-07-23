package com.eos.simplemusicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import com.eos.simplemusicplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var mService: MusicPlayerService? = null

    val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = (service as MusicPlayerService.MusicPlayerBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        binding.btnPlay.setOnClickListener {
            play()
        }

        binding.btnPause.setOnClickListener {
            pause()
        }

        binding.btnStop.setOnClickListener {
            stop()
        }
    }

    override fun onResume() {
        super.onResume()
        // 서비스 실행
        if (mService == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, MusicPlayerService::class.java))
            } else {
                startService(Intent(applicationContext, MusicPlayerService::class.java))
            }
        }

        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }
    // 사용자가 액티비티를 떠났을 때 처리
    override fun onPause() {
        super.onPause()

        if (mService != null) {
            if (!mService!!.isPlaying()) {
                mService!!.stopSelf()
            }
            unbindService(mServiceConnection)
            mService = null
        }
    }

    private fun play() {
        mService?.play()
    }

    private fun pause() {
        mService?.pause()
    }

    private fun stop() {
        mService?.stop()
    }
}