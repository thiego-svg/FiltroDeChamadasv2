package com.filtro.ligacoes

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

data class BlockedCall(val time: Long, val number: String)

class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("filtro_prefs", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean("enabled", true)
        set(value) {
            sp.edit { putBoolean("enabled", value) }
        }

    var blockHidden: Boolean
        get() = sp.getBoolean("block_hidden", true)
        set(value) {
            sp.edit { putBoolean("block_hidden", value) }
        }

    var languageCode: String
        get() = sp.getString("language_code", "pt-BR") ?: "pt-BR"
        set(value) {
            sp.edit { putString("language_code", value) }
        }

    val totalBlocked: Int
        get() = sp.getInt("total", 0)

    fun log(): List<BlockedCall> = synchronized(LOCK) {
        val raw = sp.getString("log", "[]") ?: "[]"
        val arr = try {
            JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            BlockedCall(time = o.optLong("t"), number = o.optString("n"))
        }
    }

    fun addLog(number: String) {
        synchronized(LOCK) {
            val arr = JSONArray()
            arr.put(entry(System.currentTimeMillis(), number))
            log().take(MAX_LOG - 1).forEach { (time, num) -> arr.put(entry(time, num)) }
            sp.edit {
                putString("log", arr.toString())
                putInt("total", sp.getInt("total", 0) + 1)
            }
        }
    }

    fun clearLog() {
        synchronized(LOCK) {
            sp.edit { putString("log", "[]") }
        }
    }

    private fun entry(time: Long, number: String): JSONObject =
        JSONObject()
            .put("t", time)
            .put("n", number)

    private companion object {
        val LOCK = Any()
        const val MAX_LOG = 40
    }
}
