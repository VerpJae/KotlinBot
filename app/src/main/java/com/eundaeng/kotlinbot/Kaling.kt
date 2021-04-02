package com.eundaeng.kotlinbot

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder

class Kaling {
    companion object {
        var apiKey = ""
        var cookies: MutableMap<String, String> = mutableMapOf()
        var loginReferer = ""
        var cryptoKey = ""
        var parsedTemplate: String = ""
        var csrf = ""
        var rooms = JSONObject()
        fun init(key: String) {
            if (key.length != 32) throw Error("api key " + key + " is not valid api key" + key.length)
            apiKey = key
        }

        fun isInitialized(): Boolean {
            return (apiKey != "")
        }

        val ua =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
        private const val ka = "sdk/1.36.6 os/javascript lang/en-US device/Win32 origin/http%3A%2F%2Flt2.kr"
        fun login(id: String, password: String) {
            if (!isInitialized()) throw Error("method login is called before initialization")

            fun loginManager() {
                val connection =
                    org.jsoup.Jsoup.connect("https://sharer.kakao.com/talk/friends/picker/link")
                connection.header("User-Agent", ua)
                connection.data(
                    mutableMapOf(
                        "app_key" to apiKey,
                        "validation_action" to "default",
                        "validation_params" to "{}",
                        "ka" to ka,
                        "lcba" to ""
                    )
                )
                connection.ignoreHttpErrors(true)
                connection.method(org.jsoup.Connection.Method.POST)
                val response = connection.execute()
                if (response.statusCode() == 401) throw Error("invalid api key")
                if (response.statusCode() != 200) throw Error("unexpected error on method login")
                this.cookies["_kadu"] = response.cookie("_kadu")
                this.cookies["_kadub"] = response.cookie("_kadub")
                this.cookies["_maldive_oauth_webapp_session"] =
                    response.cookie("_maldive_oauth_webapp_session")
                val document = response.parse()
                this.cryptoKey = document.select("input[name=p]").attr("value")
                this.loginReferer = response.url().toString()
            }
            loginManager()

            fun tiara() {
                val connection =
                    org.jsoup.Jsoup.connect("https://track.tiara.kakao.com/queen/footsteps")
                connection.ignoreContentType(true)
                val response = connection.execute()
                this.cookies["TIARA"] = response.cookie("TIARA")
            }
            tiara()

            fun authenticate() {
                val connection =
                    org.jsoup.Jsoup.connect("https://accounts.kakao.com/weblogin/authenticate.json")
                connection.header("User-Agent", ua)
                connection.header("Referer", this.loginReferer)
                connection.cookies(this.cookies)
                connection.data(
                    mutableMapOf(
                        "os" to "web",
                        "webview_v" to "2",
                        "email" to encrypt(id, this.cryptoKey),
                        "password" to encrypt(password, this.cryptoKey),
                        "continue" to URLDecoder.decode(this.loginReferer.split("=")[1], "utf-8"),
                        "third" to "false",
                        "k" to "true"
                    )
                )
                connection.ignoreContentType(true)
                connection.method(org.jsoup.Connection.Method.POST)
                val response = connection.execute()
                val result = JSONObject(response.body())
                if (result.getInt("status") == -450) throw Error("invalid id or password")
                if (result.getInt("status") != 0) throw Error("unexpected error on method login" + result.getInt("status"))
                this.cookies["_kawlt"] = response.cookie("_kawlt")
                this.cookies["_kawltea"] = response.cookie("_kawltea")
                this.cookies["_karmt"] = response.cookie("_karmt")
                this.cookies["_karmtea"] = response.cookie("_karmtea")
            }
            authenticate()
        }

        fun send(roomTitle: String, data: String, type: String){
            fun proceed() {
                val connection =
                    org.jsoup.Jsoup.connect("https://sharer.kakao.com/talk/friends/picker/link")
                connection.header("User-Agent", ua)
                connection.header("Referer", this.loginReferer)
                connection.cookies(
                    mutableMapOf(
                        "TIARA" to this.cookies["TIARA"],
                        "_kawlt" to this.cookies["_kawlt"],
                        "_kawltea" to this.cookies["_kawltea"],
                        "_karmt" to this.cookies["_karmt"],
                        "_karmtea" to this.cookies["_karmtea"]
                    )
                )
                connection.data(
                    mutableMapOf(
                        "app_key" to this.apiKey,
                        "validation_action" to type,
                        "validation_params" to data,
                        "ka" to ka,
                        "lcba" to ""
                    )
                )
                connection.method(org.jsoup.Connection.Method.POST)
                connection.ignoreHttpErrors(true)
                val response = connection.execute()
                if (response.statusCode() == 400) throw Error("invalid template parameter")
                this.cookies["KSHARER"] = response.cookie("KSHARER")
                this.cookies["using"] = "true"
                val document = response.parse()
                this.parsedTemplate = document.select("#validatedTalkLink").attr("value")
                this.csrf = document.select("div").last().attr("ng-init").split("\'")[1]
            }
            proceed()

            fun getRooms() {
                val connection = org.jsoup.Jsoup.connect("https://sharer.kakao.com/api/talk/chats")
                connection.header("User-Agent", ua)
                connection.header("Referer", "https://sharer.kakao.com/talk/friends/picker/link")
                connection.header("Csrf-Token", this.csrf)
                connection.header("App-Key", this.apiKey)
                connection.cookies(this.cookies)
                connection.ignoreContentType(true)
                val response = connection.execute()
                val document = response.body().replace(Regex("\u200b"), "")
                this.rooms = JSONObject(document)
            }
            getRooms()

            fun sendTemplate() {
                var id = ""
                var securityKey = ""
                val room = JSONArray(rooms.getString("chats"))
                for (i in 0..room.length()) {
                    if (room.getJSONObject(i).getString("title") == roomTitle) {
                        id = room.getJSONObject(i).getString("id")
                        securityKey = this.rooms.getString("securityKey")
                        break
                    }
                }
                if (id == "") throw Error("invalid room name $roomTitle")
                val connection =
                    org.jsoup.Jsoup.connect("https://sharer.kakao.com/api/talk/message/link")
                connection.header("User-Agent", ua)
                connection.header("Referer", "https://sharer.kakao.com/talk/friends/picker/link")
                connection.header("Csrf-Token", this.csrf)
                connection.header("App-Key", this.apiKey)
                connection.header("Content-Type", "application/json;charset=UTF-8")
                connection.cookies(
                    mutableMapOf(
                        "KSHARER" to this.cookies["KSHARER"],
                        "TIARA" to this.cookies["TIARA"],
                        "using" to this.cookies["using"],
                        "_kadu" to this.cookies["_kadu"],
                        "_kadub" to this.cookies["_kadub"],
                        "_kawlt" to this.cookies["_kawlt"],
                        "_kawltea" to this.cookies["_kawltea"],
                        "_karmt" to this.cookies["_karmt"],
                        "_karmtea" to this.cookies["_karmtea"]
                    )
                )
                connection.requestBody(
                    """{
"receiverChatRoomMemberCount":[1],
"receiverIds":["$id"],
"receiverType":"chat",
"securityKey":"$securityKey",
"validatedTalkLink":${this.parsedTemplate}
}""".replace("\n", ""))
                connection.ignoreContentType(true)
                connection.ignoreHttpErrors(true)
                connection.method(org.jsoup.Connection.Method.POST)
                connection.execute()
            }
            sendTemplate()
        }

        private fun encrypt(value: String, key: String): String =
            AESCipher.encrypt(value, key).toString()
    }
}