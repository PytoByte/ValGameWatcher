package pytobyte.valgamewatcher.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pytobyte.valgamewatcher.R
import pytobyte.valgamewatcher.data.Gamemodes
import pytobyte.valgamewatcher.encodeString
import pytobyte.valgamewatcher.getMatches
import pytobyte.valgamewatcher.simpleGetRequest

class CheckGames : Service() {

    private var serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sp = getSharedPreferences("data", ComponentActivity.MODE_PRIVATE)
        var response = ""
        val checkInterval = sp.getFloat("CheckInterval", 120f)

        serviceScope.launch {
            while (true) {
                if (checkInterval==0f) {
                    break
                }
                delay((1000 * 60 * checkInterval).toLong())
                val name = sp.getString("name", "Оберон#09KD")!!
                var more = false
                var winsSummary = 0
                var gamesSummary = 0
                var errorsSummary = 0

                Gamemodes.values().forEach {
                    val lastID = sp.getString("${it.name}lastID", "0")!!

                    if (sp.getBoolean("${it.name}Check", true)) {
                        try {
                            Log.d("Service", "Request")

                            runBlocking {
                                response = getMatches(name, it.type)
                            }

                            if (JSONObject(response).isNull("errors")) {
                                val matches =
                                    JSONObject(response).getJSONObject("data").getJSONArray("matches")

                                val firstID = if (matches.length()>0) {
                                    matches.getJSONObject(0).getJSONObject("attributes").getString("id")
                                } else {
                                    lastID
                                }

                                if (lastID != firstID) {
                                    var gamesCount = 0
                                    var found = false
                                    var wins = 0

                                    if (matches.length() != 0) {
                                        for (i in 0 until matches.length()) {
                                            val curID =
                                                matches.getJSONObject(i).getJSONObject("attributes")
                                                    .getString("id")
                                            if (curID == lastID) {
                                                found = true
                                                break
                                            } else {
                                                gamesCount++
                                                gamesSummary++
                                            }
                                            if (matches.getJSONObject(i).getJSONObject("metadata")
                                                    .getString("result") == "victory"
                                            ) {
                                                wins++
                                                winsSummary++
                                            }
                                        }

                                        if (!found && gamesCount==20) {
                                            more = true
                                        }
                                    }
                                }
                            }

                        } catch (ex: Exception) {
                            errorsSummary++
                            ex.printStackTrace()
                        }
                    }
                }

                if (gamesSummary>0) {
                    if (more) {
                        SendNotification("Замечена активность! ($gamesSummary игр; ${(winsSummary * 100 / gamesSummary)}% побед) ($errorsSummary ошибок)")
                    } else {
                        SendNotification("Замечена активность! (>$gamesSummary игр; ${(winsSummary * 100 / gamesSummary)}% побед) ($errorsSummary ошибок)")
                    }
                } else if (sp.getBoolean("showFailCheck", false)) {
                    SendNotification("Никаких изменений ($errorsSummary ошибок)")
                }
            }
        }

        return START_STICKY
    }

    private fun SendNotification(message: String) {
        val builder = NotificationCompat.Builder(this, "channelID")
            .setSmallIcon(R.drawable.img)
            .setContentTitle("Уведомление")
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(101, builder.build())
    }


    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
        Log.d("Service", "Service destroyed.")
    }
}