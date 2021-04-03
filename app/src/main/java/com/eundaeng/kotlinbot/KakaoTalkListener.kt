package com.eundaeng.kotlinbot

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.util.*

/**
 * Created by Dark Tornado on 2018-01-17.
 * Last Edited by EunDaeng on 2021-04-03.
 */
class KakaoTalkListener : NotificationListenerService() {
    companion object{
        var switchOn = KakaoBot.readData("botOn").toBoolean()
    }
    internal var preChat = HashMap<String?, String>()

    override fun onCreate() {
        super.onCreate()
        notificationBuilder()
        Toast.makeText(this, "카카오톡 봇이 알림에 접근하기 시작합니다.", Toast.LENGTH_SHORT - 1500).show()
        CoroutineScope(Dispatchers.IO).launch {
            Kaling.init("javascript key")
            Kaling.login("email@email.com", "password")
        }
        Toast.makeText(this, "카링 로그인 성공", Toast.LENGTH_SHORT - 1500).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        preChat.clear()
        Toast.makeText(this, "카카오톡 봇이 알림에 접근하는 것이 정지되었습니다.", Toast.LENGTH_SHORT - 1500).show()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (sbn.packageName.startsWith("com.kakao") && switchOn) {
            //if(sbn.packageName != null){
            try {
                val wExt: Notification.WearableExtender =
                    Notification.WearableExtender(sbn.notification)
                for (act in wExt.actions) {
                    if (act.remoteInputs != null && act.remoteInputs.size > 0) {
                        if (act.title.toString().toLowerCase()
                                .contains("reply") || act.title.toString().toLowerCase()
                                .contains(
                                    "답장"
                                )
                        ) {
                            val data = sbn.notification.extras
                            var room: String?
                            val sender: String?
                            val msg: String?
                            var isGroupChat: Boolean
                            if (Build.VERSION.SDK_INT > 23) {
                                room = data.getString("android.summaryText")
                                sender = data.get("android.title")!!.toString()
                                isGroupChat = room != null
                                msg = data.get("android.text")!!.toString()
                            } else {
                                room = data.getString("android.subText")
                                msg = data.getString("android.text")
                                sender = data.getString("android.title")
                                isGroupChat = room != null
                            }
                            if (room == null) room = sender
                            chatHook(
                                room.toString(),
                                msg!!.trim { it <= ' ' },
                                sender.toString(),
                                isGroupChat,
                                act
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                toast(e.toString() + "\nAt:" + e.stackTrace[0].lineNumber)
            }

        }
    }

    private fun chatHook(
        room: String,
        msg: String,
        sender: String,
        isGroupChat: Boolean,
        replier: Notification.Action
    ) {
        //toast("sender: " + sender + "\nmsg: " + msg + "\nroom: " + room + "\nisGroupChat: " + isGroupChat);
        try {
            //val chat = preChat.get(room)
            //if (chat != null && chat!!.equals(msg)) return
            preChat.put(room, msg)
            response(room, msg, sender, isGroupChat, replier)
        }
        catch (e: Exception) {
            toast(e.toString())
        }

    }

    fun response(room:String?, msg:String, sender:String, isGroupChat: Boolean, replier: Notification.Action){
        try {
            if (msg == "&개발자") {
                replier.reply("코틀린 봇의 개발자, 은댕이")
            }
            if (msg == "&확률") {
                var a = Math.floor(Math.random() * 100)
                replier.reply(a)
            }
            if (msg == "&테스트"){
                replier.reply("방: $room\n보낸사람: $sender\n단체채팅: $isGroupChat")
            }
            if (msg.startsWith("&주식 ")) {
                CoroutineScope(Dispatchers.IO).launch {
                    delay(100L)
                    val fr =
                        Jsoup.connect("https://www.google.com/search?q=주식%20" + msg.substring(4))
                            .get().select("g-card-section").get(0).select("span[jsname=vWLAgc]")
                            .text()
                    replier.reply(fr)
                }
            }
            if(msg.equals("&카링")){
                CoroutineScope(Dispatchers.IO).launch {
                    Kaling.send(room!!, """{
"link_ver": "4.0",
"template_id": 12345,
"template_args": {}
}""", "custom"
                    )
                    replier.reply("성공")
                }
            }
        }catch(e: Exception){showDialog("봇 에러", e.message)}
    }

    private fun loadSetting(setting: String): Boolean {
        val cache = KakaoBot.readData(setting)
        return if (cache == null)
            false
        else
            cache == "true"
    }


    private fun toast(value: String) {
        val intent = Intent(this, MainService::class.java)
        intent.putExtra("toast", value)
        startService(intent)
    }

    fun showDialog(title: String, msg: String?) {
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

    private fun Notification.Action.reply(value: Any?) {
        val sendIntent = Intent()
        val msg = Bundle()
        for (inputable in remoteInputs) msg.putCharSequence(inputable.resultKey, value.toString())
        RemoteInput.addResultsToIntent(remoteInputs, sendIntent, msg)

        try {
            actionIntent.send(this@KakaoTalkListener, 0, sendIntent)
            toast("<자동응답 실행됨>")
        } catch (e: PendingIntent.CanceledException) {

        }

    }
    private fun Notification.Action.markAsRead() {
        val sendIntent = Intent()
        val msg = Bundle()
        for (inputable in remoteInputs) msg.putCharSequence(inputable.resultKey, "")
        RemoteInput.addResultsToIntent(remoteInputs, sendIntent, msg)

        try {
            actionIntent.send(this@KakaoTalkListener, 1, sendIntent)

        } catch (e: PendingIntent.CanceledException) {

        }

    }

    private fun notificationBuilder() {
        val NOTIFICATION_CHANNEL_ID = "default_id"
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notificationBuilder: NotificationCompat.Builder
                = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .apply {
                setSmallIcon(R.drawable.icon)
                setDefaults(Notification.DEFAULT_ALL)
                setContentTitle("Kotlin Bot")
                setContentText("코틀린 봇")
                setAutoCancel(false)
                setWhen(System.currentTimeMillis())
                priority = NotificationCompat.PRIORITY_MAX
                setContentIntent(pendingIntent)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                "코틀린 카톡 봇", NotificationManager.IMPORTANCE_NONE).apply {
                description = getString(R.string.app_name)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(notificationChannel)
        }

        startForeground(1, notificationBuilder.build())
    }

}
