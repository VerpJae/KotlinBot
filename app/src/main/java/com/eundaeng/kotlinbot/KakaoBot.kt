package com.eundaeng.kotlinbot

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import java.io.*

/**
 * Created by Dark Tornado on 2018-01-18.
 */
class KakaoBot : Application() {


    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
    }

    companion object {

        private val sdcard = Environment.getExternalStorageDirectory().absolutePath
        val VERSION = "1.0"
        var ctx: Context? = null

        fun readData(name: String): String {
            try {
                val file = "$sdcard/KakaoBot/$name.txt"
                val str = loadFromExternalStorage(file)
                return str
            } catch (e: Exception) {
                //toast(e.toString());
            }

            return ""
        }

        fun saveData(name: String, value: String) {
            try {
                val file ="$sdcard/KakaoBot/$name.txt"
                saveToExternalStorage(value, file)
            } catch (e: Exception) {
                //toast(e.toString());
            }

        }

        fun removeData(name: String) {
            try {
                val file = File("$sdcard/KakaoBot/$name.txt")
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                //toast(e.toString());
            }

        }

        private fun toast(msg: String) {
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
        }
        private fun getAppDataFileFromExternalStorage(filename: String) : File{
            //kitkat버전부터는
            val dir = File(filename)

            // 디렉토리없으면 만듦
            dir?.mkdirs()
            return File("$filename")
        }
        private fun saveToExternalStorage(text: String, filename: String){
            val fileOutputStream = FileOutputStream(getAppDataFileFromExternalStorage(filename))
            fileOutputStream.write(text.toByteArray())
            fileOutputStream.close()
        }
        private fun loadFromExternalStorage(filename: String) :String {
            return FileInputStream(getAppDataFileFromExternalStorage(filename)).reader().readText()
        }

    }
}
