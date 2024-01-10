package pytobyte.littleparser

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.escape.UnicodeEscaper
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.PercentEscaper
import org.json.JSONObject
import pytobyte.valcompchecker.simpleGetRequest


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val showingText = mutableStateOf("")
        check(showingText, this)


        setContent{
            val text = remember { showingText }

            val sp = getSharedPreferences("IDandNAME", MODE_PRIVATE)
            val name = remember { mutableStateOf(sp.getString("name", "Оберон#09KD")!!) }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Competitive games checker",
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
                Button(onClick = {check(showingText, this@MainActivity)})  {
                    Text(text = "Обновить", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

fun check(showingText:MutableState<String>, activity: ComponentActivity) {
    Thread{
        var firstID = "0"
        Log.d("Thread", "call")
        val sp = activity.getSharedPreferences("IDandNAME", ComponentActivity.MODE_PRIVATE)
        val lastID = sp.getString("lastID", "0")
        val name = sp.getString("name", "Оберон#09KD")

        val basicEscaper: UnicodeEscaper = PercentEscaper("-", false)
        val encodedName = basicEscaper.escape(name)

        name!!.forEach {
            Log.d("Thread code", it.code.toString())
        }

        try {
            val response = simpleGetRequest("https://api.tracker.gg/api/v2/valorant/standard/matches/riot/$encodedName?type=competitive&season=&agent=all&map=all")
            val matches = JSONObject(response).getJSONObject("data").getJSONArray("matches")

            firstID = matches.getJSONObject(0).getJSONObject("attributes").getString("id")

            if (lastID!=firstID) {
                var gc = 0;
                var found = false;
                for (i in 0 until matches.length()) {
                    gc = i
                    val curID = matches.getJSONObject(i).getJSONObject("attributes").getString("id")
                    if (curID==lastID) {
                        found = true
                        break
                    }
                }

                if (found) {
                    showingText.value = "Замечена активность!\n${++gc} игр"
                } else {
                    showingText.value = "Замечена активность!\n>20 игр"
                }
                val editor = sp.edit()
                editor.putString("lastID", firstID)
                editor.apply()
            } else {
                showingText.value = "Никаких измененией"
            }
        } catch (ex: Exception) {
            showingText.value = "Error $ex"
        }
    }.start()
}
