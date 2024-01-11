package pytobyte.valcompchecker

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.escape.UnicodeEscaper
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.PercentEscaper
import java.net.HttpURLConnection
import java.net.URL

fun simpleGetRequest(url: String): String {
    val urlConnection = URL(url).openConnection() as HttpURLConnection
    urlConnection.requestMethod = "GET"
    return urlConnection.inputStream.bufferedReader().readText()
}

fun encodeString(s: String): String {
    val basicEscaper: UnicodeEscaper = PercentEscaper("-", false)
    return basicEscaper.escape(s)
}

fun getMatches(nickname: String): String {
    return simpleGetRequest("https://api.tracker.gg/api/v2/valorant/standard/matches/riot/${encodeString(nickname)}?type=competitive&season=&agent=all&map=all")
}