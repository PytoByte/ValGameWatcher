package pytobyte.valcompchecker

import java.net.HttpURLConnection
import java.net.URL

fun simpleGetRequest(url: String): String {
    val urlConnection = URL(url).openConnection() as HttpURLConnection
    urlConnection.requestMethod = "GET"
    return urlConnection.inputStream.bufferedReader().readText()
}