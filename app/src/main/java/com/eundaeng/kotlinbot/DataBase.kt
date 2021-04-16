package com.eundaeng.kotlinbot

import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import org.json.JSONObject
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DataBase {
    companion object{
        var db: SQLiteDatabase? = null
        var db2: SQLiteDatabase? = null
        fun getRoot(){
            Runtime.getRuntime().exec("su -c \"\"chmod -R 777 /data/data/com.kakao.talk/databases\"\"")
            Runtime.getRuntime().exec("su -c \"\"chmod -R 777 /data/data/com.kakao.talk/shared_prefs\"\"")
            Runtime.getRuntime().exec("su")
        }
        fun updateDB(){
            db = SQLiteDatabase.openDatabase(
                "/data/data/com.kakao.talk/databases/KakaoTalk.db",
                null,
                SQLiteDatabase.CREATE_IF_NECESSARY
            )
            /*
            db2 = SQLiteDatabase.openDatabase(
                "/data/data/com.kakao.talk/databases/KakaoTalk2.db",
                null,
                SQLiteDatabase.CREATE_IF_NECESSARY
            )
             */
        }
        fun toCharArray(chars: Array<Int>): CharArray {
            return (chars.map{it.toChar()}).toCharArray()
        }

        fun toByteArray(bytes: Array<Int>): ByteArray {
            var res = ByteArray(bytes.size)
            bytes.forEachIndexed {  i, e -> res[i] = java.lang.Integer(e).toByte() }
            return res
        }

        val dream_arr1 = arrayOf("adrp.ldrsh.ldnp", "ldpsw", "umax", "stnp.rsubhn", "sqdmlsl", "uqrshl.csel", "sqshlu", "umin.usubl.umlsl", "cbnz.adds", "tbnz", "usubl2", "stxr", "sbfx", "strh", "stxrb.adcs", "stxrh", "ands.urhadd", "subs", "sbcs", "fnmadd.ldxrb.saddl", "stur", "ldrsb", "strb", "prfm", "ubfiz", "ldrsw.madd.msub.sturb.ldursb", "ldrb", "b.eq", "ldur.sbfiz", "extr", "fmadd", "uqadd", "sshr.uzp1.sttrb", "umlsl2", "rsubhn2.ldrh.uqsub", "uqshl", "uabd", "ursra", "usubw", "uaddl2", "b.gt", "b.lt", "sqshl", "bics", "smin.ubfx", "smlsl2", "uabdl2", "zip2.ssubw2", "ccmp", "sqdmlal", "b.al", "smax.ldurh.uhsub", "fcvtxn2", "b.pl")
        val dream_arr2 = arrayOf("saddl", "urhadd", "ubfiz.sqdmlsl.tbnz.stnp", "smin", "strh", "ccmp", "usubl", "umlsl", "uzp1", "sbfx", "b.eq", "zip2.prfm.strb", "msub", "b.pl", "csel", "stxrh.ldxrb", "uqrshl.ldrh", "cbnz", "ursra", "sshr.ubfx.ldur.ldnp", "fcvtxn2", "usubl2", "uaddl2", "b.al", "ssubw2", "umax", "b.lt", "adrp.sturb", "extr", "uqshl", "smax", "uqsub.sqshlu", "ands", "madd", "umin", "b.gt", "uabdl2", "ldrsb.ldpsw.rsubhn", "uqadd", "sttrb", "stxr", "adds", "rsubhn2.umlsl2", "sbcs.fmadd", "usubw", "sqshl", "stur.ldrsh.smlsl2", "ldrsw", "fnmadd", "stxrb.sbfiz", "adcs", "bics.ldrb", "ldursb", "subs.uhsub", "ldurh", "uabd", "sqdmlal")
        fun dream(param: Int): String{
            return dream_arr1[param % 54] + "." + dream_arr2[(param + 31) % 57];
        }

        fun decrypt(userId: String, enc: Int, text: String?): String? {
            if(text == null) return null
            try {
                val iv = toByteArray(arrayOf(15, 8, 1, 0, 25, 71, 37, -36, 21, -11, 23, -32, -31, 21, 12, 53))
                //val iv = iva.foldIndexed(ByteArray(iva.size)) { i, a, v -> a.apply { set(i, v.toByte()) } }
                val password = toCharArray(arrayOf(22, 8, 9, 111, 2, 23, 43, 8, 33, 33, 10, 16, 3, 3, 7, 6));
                val prefixes = arrayOf("", "", "12", "24", "18", "30", "36", "12", "48", "7", "35", "40", "17", "23", "29", "isabel", "kale", "sulli", "van", "merry", "kyle", "james", "maddux", "tony", "hayden", "paul", "elijah", "dorothy", "sally", "bran", dream(0xcad63));
                val salt = java.lang.String((prefixes[enc] + userId).slice(0..16).padEnd(16, 0.toChar())).getBytes("UTF-8")
                val secretKeySpec = SecretKeySpec(SecretKeyFactory.getInstance("PBEWITHSHAAND256BITAES-CBC-BC").generateSecret(PBEKeySpec(password, salt, 2, 256)).getEncoded(), "AES");
                val ivParameterSpec = IvParameterSpec(iv);
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(2, secretKeySpec, ivParameterSpec);
                return java.lang.String(cipher.doFinal(Base64.decode(text, 0)), "UTF-8").toString()
            } catch (e: Exception) {
                return e.message
            }
        }

        fun getLastChat(): JSONObject {
            updateDB()
            val obj = JSONObject()
            val cursor = db!!.rawQuery("select * from chat_logs order by created_at desc limit 1", null);
            cursor.moveToLast();
            cursor.getColumnNames().forEachIndexed {i, e ->
                obj.put(e, cursor.getString(i))
                if (e == "v") obj.put("v", JSONObject(obj.getString("v")))
            }
            cursor.close();
            obj.put("attachment", decrypt(obj.getString("user_id"), obj.getJSONObject("v").getString("enc").toInt(), obj.getString("attachment")))
            obj.put("message", decrypt(obj.getString("user_id"), obj.getJSONObject("V").getString("enc").toInt(), obj.getString("message")))
            return obj
        }
    }
}
