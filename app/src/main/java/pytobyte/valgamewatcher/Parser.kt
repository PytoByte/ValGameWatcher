package pytobyte.valgamewatcher

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.escape.UnicodeEscaper
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.PercentEscaper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.request.get
import io.ktor.client.statement.request
import pytobyte.valgamewatcher.data.Gamemodes
import java.net.HttpURLConnection
import java.net.URL

suspend fun simpleGetRequest(url: String): String {
    try {
        val client = HttpClient(CIO) {
            BrowserUserAgent()
        }
        val response = client.get(url)
        return response.body()
    } catch (ex: Exception) {
        ex.printStackTrace()
        return ""
    }

}

fun encodeString(s: String): String {
    val basicEscaper: UnicodeEscaper = PercentEscaper("-", false)
    return basicEscaper.escape(s)
}

suspend fun getMatches(nickname: String, type: String=Gamemodes.COMPETITIVE.type): String {
    return simpleGetRequest("https://api.tracker.gg/api/v2/valorant/standard/matches/riot/${encodeString(nickname)}?type=$type&season=&agent=all&map=all")
}