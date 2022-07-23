package com.eos.simplemusicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast

class MusicPlayerService : Service() {

    var mMediaPlayer: MediaPlayer? = null
    var mBinder: MusicPlayerBinder = MusicPlayerBinder()

    inner class MusicPlayerBinder : Binder() {
        fun getService() : MusicPlayerService {
            return this@MusicPlayerService
        }
    }
    // 서비스가 실행될 때 딱 한번만 실행
    override fun onCreate() {
        super.onCreate()
        startForegroundService() // 포그라운드 서비스 시작
    }

    // 바인드, bindService() 호출 될 때 실행됨
    // IBinder -> 구성요소와 서비스를 이어주는 매개체 역할
    // 시작된 서비스는 바인드가 필요 없으므로 null 을 반환하면 됩니다.
    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }

    // 시작된 상태 & 백그라운드
    // startService() 를 호출하면 실행되는 콜백 함수
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    // 서비스 종료 -> 상태 표시줄의 알림을 해제
    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
    }

    fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as
                    NotificationManager
            val mChannel = NotificationChannel(
                "CHANNEL_ID",
                "CHANNEL_NAME",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(mChannel)
        }

        val notification: Notification = Notification.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle("뮤직 플레이어 앱")
            .setContentText("앱이 실행 중입니다.")
            .build()

        startForeground(1, notification)
    }

    fun isPlaying() : Boolean {
        return (mMediaPlayer != null && mMediaPlayer?.isPlaying ?: false)
    } // 재생 중인지 확인

    fun play() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.chocolate)

            mMediaPlayer?.setVolume(1.0f, 1.0f)
            mMediaPlayer?.isLooping = true
            mMediaPlayer?.start()
        } else {
            if (mMediaPlayer!!.isPlaying) {
                Toast.makeText(this, "이미 음악이 실행 중입니다.",
                    Toast.LENGTH_SHORT).show()
            } else {
                mMediaPlayer?.start()
            }
        }
    } // 재생


    fun pause() {
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    } // 일시정지


    fun stop() {
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.release()
                mMediaPlayer = null
            }
        }
    } // 재생 중지
}