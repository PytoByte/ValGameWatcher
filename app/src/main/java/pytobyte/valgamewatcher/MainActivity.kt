package pytobyte.littleparser

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import pytobyte.valgamewatcher.R
import pytobyte.valgamewatcher.data.Gamemodes
import pytobyte.valgamewatcher.encodeString
import pytobyte.valgamewatcher.getMatches
import pytobyte.valgamewatcher.services.CheckGames
import pytobyte.valgamewatcher.simpleGetRequest
import pytobyte.valgamewatcher.ui.theme.ValCompCheckerTheme


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val showingText = mutableStateOf("")
        startService(Intent(this, CheckGames::class.java))


        setContent {
            val text = remember { showingText }
            val sp = getSharedPreferences("data", MODE_PRIVATE)
            val name = remember { mutableStateOf(sp.getString("name", "Оберон#09KD")!!) }
            val openDialog = remember { mutableStateOf(false) }

            var isDarkTheme by remember { mutableStateOf(sp.getBoolean("darkTheme", false)) }

            ValCompCheckerTheme(
                darkTheme = isDarkTheme,
            ) {
                Surface() {
                    Row(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                isDarkTheme = !isDarkTheme
                                val edit = sp.edit()
                                edit.putBoolean("darkTheme", isDarkTheme)
                                edit.apply()
                            }
                        ) {
                            Text(
                                text = if (isDarkTheme) "Светлая тема" else "Тёмная тема",
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                openDialog.value = true
                            }
                        ) {
                            Text(text = "Настройки", textAlign = TextAlign.Center)
                        }
                    }


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
                    if (openDialog.value) {
                        Dialog(onDismissRequest = { openDialog.value = false }) {
                            Surface(modifier = Modifier.fillMaxHeight(0.9f)) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "Параметры",
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.size(10.dp))
                                    LazyColumn() {
                                        item {
                                            Column {
                                                Text(
                                                    text = "Интервал запросов в мин (0 для откл)"
                                                )
                                                val checkInterval = remember {
                                                    mutableStateOf(
                                                        sp.getFloat(
                                                            "CheckInterval",
                                                            120f
                                                        ).toString()
                                                    )
                                                }
                                                TextField(
                                                    modifier = Modifier.onFocusEvent {
                                                        if (checkInterval.value.isEmpty() || checkInterval.value.toFloatOrNull() == null) {
                                                            checkInterval.value = "0.0"
                                                        }
                                                        val edit = sp.edit()
                                                        edit.putFloat(
                                                            "CheckInterval",
                                                            checkInterval.value.toFloat()
                                                        )
                                                        edit.apply()
                                                    },
                                                    value = checkInterval.value,
                                                    onValueChange = {
                                                        checkInterval.value = it
                                                    },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                                )
                                            }
                                        }
                                        item {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val checkState = remember { mutableStateOf(sp.getBoolean("showFailCheck", false)) }
                                                Checkbox(
                                                    checked = checkState.value,
                                                    onCheckedChange = {
                                                        checkState.value = !checkState.value
                                                        val edit = sp.edit()
                                                        edit.putBoolean(
                                                            "showFailCheck",
                                                            checkState.value
                                                        )
                                                        edit.apply()
                                                    })
                                                Text(text = "Уведомлять об отсутствии изменений")
                                            }
                                        }
                                        itemsIndexed(Gamemodes.values()) { index, item ->
                                            val checkState = remember { mutableStateOf(sp.getBoolean("${item.name}Check", true))}
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = checkState.value,
                                                    onCheckedChange = {
                                                        checkState.value = !checkState.value
                                                        val edit = sp.edit()
                                                        edit.putBoolean(
                                                            "${item.name}Check",
                                                            checkState.value
                                                        )
                                                        edit.apply()
                                                    })
                                                Text(text = item.translate)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun check(
        showingText: MutableState<String>,
        activity: ComponentActivity
    ) {
        Thread {
            Log.d("Thread", "call")
            val sp = activity.getSharedPreferences("data", ComponentActivity.MODE_PRIVATE)
            val name = sp.getString("name", "Оберон#09KD")!!

            val sb = StringBuilder()
            var active = false
            var winsSummary = 0
            var gamesSummary = 0
            var errorsSummary = 0
            var gamemodesCount = 0
            var more = false
            showingText.value = "Проверка.."

            Gamemodes.values().forEach {
                if (sp.getBoolean("${it.name}Check", true)) {
                    gamemodesCount++
                    try {
                        val lastID = sp.getString("${it.name}lastID", "0")!!

                        val response: String
                        runBlocking {
                            response = getMatches(name, it.type)
                        }
                        if (JSONObject(response).isNull("errors")) {
                            val matches =
                                JSONObject(response).getJSONObject("data").getJSONArray("matches")
                            val firstID = if (matches.length() > 0) {
                                matches.getJSONObject(0).getJSONObject("attributes").getString("id")
                            } else {
                                lastID
                            }


                            if (lastID != firstID) {
                                if (!active) {
                                    sb.appendLine("Замечена активность!")
                                    active = true
                                }

                                var gamesCount = 0
                                var found = false
                                var wins = 0
                                if (matches.length() != 0) {
                                    for (i in 0 until matches.length()) {
                                        val curID = matches.getJSONObject(i).getJSONObject("attributes")
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

                                    if (found || gamesCount < 20) {
                                        sb.appendLine("${gamesCount} ${it.translate} ${(wins * 100 / gamesCount)}% побед")
                                    } else {
                                        more = true
                                        sb.appendLine(">20 ${it.translate} ${(wins * 100 / gamesCount)}% побед")
                                    }
                                    val editor = sp.edit()
                                    editor.putString("${it.name}lastID", firstID)
                                    editor.apply()

                                    showingText.value = "$sb\nЗагрузка.."


                                }
                            }
                        }
                    } catch (ex: Exception) {
                        errorsSummary++
                        sb.appendLine("Error ${ex.message}. Cause ${ex.cause}")
                        ex.printStackTrace()
                    }
                }
            }

            if (gamesSummary == 0) {
                if (errorsSummary == gamemodesCount) {
                    showingText.value =
                        "Никаких измененией (${errorsSummary} ошибок)\nВероятно вы делаете запросы слишком часто\nИли нет доступа в интернет"
                } else {
                    showingText.value = "Никаких измененией (${errorsSummary} ошибок)"
                }
            } else {
                if (more) {
                    sb.appendLine("\n>${gamesSummary} Суммарно ${(winsSummary * 100 / gamesSummary)}% побед (${errorsSummary} ошибок)")
                } else {
                    sb.appendLine("\n${gamesSummary} Суммарно ${(winsSummary * 100 / gamesSummary)}% побед (${errorsSummary} ошибок)")
                }
                showingText.value = sb.toString()
            }

        }.start()
    }
}
