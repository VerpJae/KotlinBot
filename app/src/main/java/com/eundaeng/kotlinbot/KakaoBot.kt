package com.eundaeng.kotlinbot

import android.app.Application
import android.content.Context
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

        fun readData(name: String): String? {
            try {
                val file = File("$sdcard/KakaoBot/$name.txt")
                if (!file.exists()) return null
                val fis = FileInputStream(file)
                val isr = InputStreamReader(fis)
                val br = BufferedReader(isr)
                var str = br.readLine()
                var line = ""
                while (br.readLine() != null) {
                    line = br.readLine()
                    str += "\n" + line
                }
                fis.close()
                isr.close()
                br.close()
                return str
            } catch (e: Exception) {
                //toast(e.toString());
            }

            return ""
        }

        fun saveData(name: String, value: String) {
            try {
                val file = File("$sdcard/KakaoBot/$name.txt")
                val fos = FileOutputStream(file)
                fos.write(value.toByteArray())
                fos.close()
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

    }
}
