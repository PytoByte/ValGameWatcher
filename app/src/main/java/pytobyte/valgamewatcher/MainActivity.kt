package pytobyte.littleparser

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import pytobyte.valgamewatcher.R
import pytobyte.valgamewatcher.data.Gamemodes
import pytobyte.valgamewatcher.encodeString
import pytobyte.valgamewatcher.getMatches
import pytobyte.valgamewatcher.services.CheckGames
import pytobyte.valgamewatcher.simpleGetRequest


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val showingText = mutableStateOf("")
        check(showingText, this, true)


        setContent {
            val text = remember { showingText }

            val sp = getSharedPreferences("IDandNAME", MODE_PRIVATE)
            val name = remember { mutableStateOf(sp.getString("name", "Оберон#09KD")!!) }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "appIcon",
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.size(20.dp))
                Text(
                    text = getString(R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.size(20.dp))
                TextField(value = name.value, onValueChange = {
                    name.value = it
                    val editor = sp.edit()
                    editor.putString("name", it)
                    editor.apply()
                })
                Spacer(modifier = Modifier.size(10.dp))
                Text(text = text.value, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.size(10.dp))
                Button(onClick = { check(showingText, this@MainActivity) }) {
                    Text(text = "Обновить", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

fun check(showingText: MutableState<String>, activity: ComponentActivity, start: Boolean = false) {
    Thread {
        Log.d("Thread", "call")
        val sp = activity.getSharedPreferences("IDsandNAME", ComponentActivity.MODE_PRIVATE)
        val name = sp.getString("name", "Оберон#09KD")!!

        val sb = StringBuilder()
        var active = false
        var winsSummary = 0
        var gamesSummary = 0
        var more = false
        showingText.value = "Проверка.. (это на долго)"
        Gamemodes.values().forEach {
            try {
                val lastID = sp.getString("${it.name}lastID", "0")!!

                simpleGetRequest("https://tracker.gg/valorant/profile/riot/${encodeString(name)}/overview?season=all&playlist=${it.type}")
                val response = getMatches(name, it.type)
                if (JSONObject(response).isNull("errors")) {
                    val matches = JSONObject(response).getJSONObject("data").getJSONArray("matches")
                    val firstID = if (matches.length()>0) {
                        matches.getJSONObject(0).getJSONObject("attributes").getString("id")
                    } else {
                        lastID
                    }


                    if (lastID != firstID) {
                        if (!active) {
                            sb.appendLine("Замечена активность!")
                            active=true
                        }
                        showingText.value = "$sb\nЗагрузка.. (это на долго)"

                        var gamesCount = 0
                        var found = false
                        var wins = 0
                        if (matches.length() != 0) {
                            for (i in 0 until matches.length()) {
                                val curID =
                                    matches.getJSONObject(i).getJSONObject("attributes").getString("id")
                                if (matches.getJSONObject(i).getJSONObject("metadata")
                                        .getString("result") == "victory"
                                ) {
                                    wins++
                                    winsSummary++
                                }
                                if (curID == lastID) {
                                    found = true
                                    break
                                } else {
                                    gamesCount++
                                    gamesSummary++
                                }
                            }

                            if (found || gamesCount<20) {
                                sb.appendLine("${gamesCount} ${it.translate} ${(wins * 100 / gamesCount)}% побед")
                            } else {
                                more = true
                                sb.appendLine(">20 ${it.translate} ${(wins * 100 / gamesCount)}% побед")
                            }
                            val editor = sp.edit()
                            editor.putString("${it.name}lastID", firstID)
                            editor.apply()
                        }
                    }
                }
            } catch (ex: Exception) {
                sb.appendLine("Error ${ex.message}. Cause ${ex.cause}")
                ex.printStackTrace()
            }
        }

        if (gamesSummary==0) {
            showingText.value = "Никаких измененией"
        } else {
            if (more) {
                sb.appendLine("\n>${gamesSummary} Суммарно ${(winsSummary * 100 / gamesSummary)}% побед")
            } else {
                sb.appendLine("\n${gamesSummary} Суммарно ${(winsSummary * 100 / gamesSummary)}% побед")
            }
            showingText.value = sb.toString()
        }

        if (start) {
            activity.startService(Intent(activity, CheckGames::class.java))
        }

    }.start()
}
