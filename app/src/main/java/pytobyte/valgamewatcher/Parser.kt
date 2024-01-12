package pytobyte.valgamewatcher

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.escape.UnicodeEscaper
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.PercentEscaper
import pytobyte.valgamewatcher.data.Gamemodes
import java.net.HttpURLConnection
import java.net.URL

fun simpleGetRequest(url: String): String {
    try {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.requestMethod = "GET"
        return urlConnection.inputStream.bufferedReader().readText()
    } catch (ex: Exception) {
        ex.printStackTrace()
        return ""
    }

}

fun encodeString(s: String): String {
    val basicEscaper: UnicodeEscaper = PercentEscaper("-", false)
    return basicEscaper.escape(s)
}

fun getMatches(nickname: String, type: String=Gamemodes.COMPETITIVE.type): String {
    return simpleGetRequest("https://api.tracker.gg/api/v2/valorant/standard/matches/riot/${encodeString(nickname)}?type=$type&season=&agent=all&map=all")
}