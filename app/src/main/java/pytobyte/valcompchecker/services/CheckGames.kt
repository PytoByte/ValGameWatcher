package pytobyte.valcompchecker.services

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
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pytobyte.valcompchecker.R
import pytobyte.valcompchecker.encodeString
import pytobyte.valcompchecker.getMatches
import pytobyte.valcompchecker.simpleGetRequest

class CheckGames : Service() {

    private var serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sp = getSharedPreferences("IDandNAME", ComponentActivity.MODE_PRIVATE)
        var lastID = ""
        var name = ""
        var showingText = ""
        var response = ""

        serviceScope.launch {
            while (true) {
                lastID = sp.getString("lastID", "0")!!
                name = sp.getString("name", "Оберон#09KD")!!

                try {
                    Log.d("Service", "Request")
                    val tr = Thread {
                        simpleGetRequest("https://tracker.gg/valorant/profile/riot/${encodeString(name)}/overview?season=all")
                        Thread.sleep(1000)
                        response = getMatches(name)
                    }
                    tr.start()
                    withContext(Dispatchers.IO) {
                        tr.join(1000 * 16)
                    }
                    val matches = JSONObject(response).getJSONObject("data").getJSONArray("matches")

                    val firstID = matches.getJSONObject(0).getJSONObject("attributes").getString("id")

                    if (lastID != firstID) {
                        var gamesCount = 0;
                        var wins = 0
                        var found = false;
                        for (i in 0 until matches.length()) {
                            gamesCount = i
                            val curID = matches.getJSONObject(i).getJSONObject("attributes").getString("id")
                            if (matches.getJSONObject(i).getJSONObject("metadata").getString("result")=="victory") {wins++}
                            if (curID == lastID) {
                                found = true
                                break
                            }
                        }

                        if (found) {
                            gamesCount++
                            showingText = "Замечена активность! (${++gamesCount} игр; ${((wins/gamesCount)*100)}% побед)"
                        } else {
                            showingText = "Замечена активность! (>20 игр; ${((wins/gamesCount)*100)}% побед)"
                        }

                        SendNotification(showingText)
                    }

                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                delay(1000 * 60)
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