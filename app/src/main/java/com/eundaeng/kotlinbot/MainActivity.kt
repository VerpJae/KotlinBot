package com.eundaeng.kotlinbot

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    internal var names = ArrayList<String>()
    val allApps: Array<String?>?
        get() {
            try {
                val intent = Intent(Intent.ACTION_MAIN, null)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                val pm = packageManager
                val apps = pm.queryIntentActivities(intent, 0)
                val strArr = arrayOfNulls<String>(apps.size)
                for (n in apps.indices) {
                    val pack = apps.get(n)
                    strArr[n] = pack.activityInfo.applicationInfo.packageName
                }
                return strArr
            } catch (e: Exception) {
                toast("getAllApps\n$e")
            }

            return null
        }
    private fun initSettings() {
        val sdcard = getExternalStorageDirectory().getAbsolutePath()
        File("$sdcard/KakaoBot/").mkdirs()
        if (KakaoBot.readData("botOn") == null) {
            KakaoBot.saveData("botOn", "false")
        }
        if (KakaoBot.readData("preventCover") == null) {
            KakaoBot.saveData("preventCover", "false")
        }
        if (KakaoBot.readData("toast") == null) {
            KakaoBot.saveData("toast", "false")
        }
        if (KakaoBot.readData("makeLog") == null) {
            KakaoBot.saveData("makeLog", "false")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            val intent = Intent(this, KakaoTalkListener::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            var isWhiteListing = false
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                isWhiteListing = pm.isIgnoringBatteryOptimizations(applicationContext.packageName)
            }
            if (!isWhiteListing) {
                val intent = Intent()
                intent.action =
                    android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:" + applicationContext.packageName)
                startActivity(intent)
            }

            val enl: String? = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            if (!enl!!.contains(BuildConfig.APPLICATION_ID)){
                Toast.makeText(this, "알림 접근 권한!!!", Toast.LENGTH_SHORT).show()
                return
            }
            if(enl == null){
                Toast.makeText(this,"This is return 1", Toast.LENGTH_SHORT).show()
                return
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"This is return 2", Toast.LENGTH_SHORT).show()
                return
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"This is return 3", Toast.LENGTH_SHORT).show()
                return
            }
            checkES()
            val btn = findViewById<Button>(R.id.button)
            btn.setOnClickListener {
                finishAffinity()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                System.exit(0)
                Toast.makeText(applicationContext, "재시작", Toast.LENGTH_LONG).show()
            }

            val on = findViewById<Switch>(R.id.switch1)
            on.isChecked = KakaoBot.readData("botOn").toBoolean()
                on.setOnCheckedChangeListener { swit, onoff ->
                    KakaoBot.saveData("botOn", onoff.toString())
                    if (onoff) {
                        toast("카카오톡 봇이 활성화되었습니다.")
                        KakaoTalkListener.switchOn = true
                    }
                    else {
                        toast("카카오톡 봇이 비활성화되었습니다.")
                        KakaoTalkListener.switchOn = false
                    }
                }
            //}
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            Thread(Runnable { checkSomething() }).start()
        } catch (e: Exception) {
            toast(e.toString())
        }

    }
    private fun checkSomething() {
        val enl = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (enl != null && !enl.contains(BuildConfig.APPLICATION_ID) || enl == null) {
            runOnUiThread {
                showDialog(
                    "알림 접근 허용",
                    "카카오톡 봇은 알림 접근 권한이 허용되어 있어야 작동합니다. 알림 접근 권한을 허용해주세요. 화면 왼쪽 끝에서 오른쪽으로 드래그하면 나오는 메뉴에서 설정 창으로 이동하실 수 있습니다."
                )
            }
        }

        val apps = getAApps()
        var noApp = true
        for (n in apps?.indices!!) {
            if (apps?.get(n) == "com.google.android.wearable.app") noApp = false
        }
        if (noApp) {
            runOnUiThread {
                showDialog(
                    "안드로이드 웨어 설치",
                    "카카오톡 봇은 안드로이드 웨어가 깔려있어야 정상적으로 작동합니다. Play 스토어에서 안드로이드 웨어를 설치해주세요. 화면 왼쪽 끝에서 오른쪽으로 드래그하면 나오는 메뉴에서 다운로드 링크로 이동할 수 있습니다."
                )
            }
        }

    }

    fun getAApps(): Array<String?>? {
        try {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pm = packageManager
            val apps = pm.queryIntentActivities(intent, 0)
            val strArr = arrayOfNulls<String>(apps.size)
            for (n in apps.indices) {
                val pack = apps[n]
                strArr[n] = pack.activityInfo.applicationInfo.packageName
            }
            return strArr
        } catch (e: Exception) {
            toast("getAllApps\n$e")
        }

        return null
    }
    fun showDialog(title: String, msg: String) {
        try {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(title)
            dialog.setMessage(msg)
            dialog.setNegativeButton("닫기", null)
            dialog.show()
        } catch (e: Exception) {
            toast(e.toString())
        }

    }
    fun toast(msg: String) {
        runOnUiThread(Runnable {
            val toast = Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG)
            toast.show()
        })
    }
    fun checkES() {
        var state = Environment.getExternalStorageState()
        // 외부메모리 상태
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // 읽기 쓰기 모두 가능
            toast("외부메모리 읽기 쓰기 모두 가능 " + state)
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            //읽기전용
            toast("외부메모리 읽기만 가능 " + state)
        } else {
            // 읽기쓰기 모두 안됨
            toast("외부메모리 읽기쓰기 모두 안됨 : " + state)
        }
    }
}