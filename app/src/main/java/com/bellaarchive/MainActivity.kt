package com.bellaarchive

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.webkit.WebView
import android.webkit.WebSettings
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalTextStyle provides androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Serif)) {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1A0A2E)) {
                        PresidentialBinderApp()
                    }
                }
            }
        }
    }
}

private val Navy = Color(0xFF5A189A)
private val NavyLight = Color(0xFF7B2CBF)
private val Gold = Color(0xFFFF69B4)
private val Paper = Color(0xFFF4ECD8)
private val Ink = Color(0xFF1A1410)

data class Item(val uri: String, var title: String, var type: String, var note: String = "", var favorite: Boolean = false)

@Composable
fun PresidentialBinderApp() {
    var opened by remember { mutableStateOf(false) }
    val scale = remember { Animatable(0.85f) }
    val rotation = remember { Animatable(-12f) }
    LaunchedEffect(Unit) {
        delay(200)
        scale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        rotation.animateTo(0f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        delay(400)
        opened = true
    }
    Box(
        modifier = Modifier.fillMaxSize().systemBarsPadding()
            .background(Brush.radialGradient(listOf(Color(0xFF7B2CBF), Color(0xFF5A189A), Color(0xFF240046)))),
        contentAlignment = Alignment.Center
    ) {
        if (!opened) {
            Box(
                modifier = Modifier.scale(scale.value).graphicsLayer { rotationY = rotation.value }
                    .size(width = 200.dp, height = 200.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(NavyLight, Navy)))
                    .border(2.dp, Gold, CircleShape).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bella's Archive", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Cursive, textAlign = TextAlign.Center)
                }
            }
        } else {
            ArchiveInterior()
        }
    }
}

@Composable
fun Seal() {
    Box(Modifier.size(52.dp).clip(CircleShape).background(Gold).border(2.dp, Color(0xFFB8962E), CircleShape), contentAlignment = Alignment.Center) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Navy), contentAlignment = Alignment.Center) {
            Text("★", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 2.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ArchiveInterior() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("archive", Context.MODE_PRIVATE)
    fun load(): MutableList<Item> {
        val json = prefs.getString("items", "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<Item>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(Item(o.getString("uri"), o.getString("title"), o.getString("type"), o.optString("note", ""), o.optBoolean("favorite", false)))
        }
        return list
    }
    fun save(list: List<Item>) {
        val arr = JSONArray()
        list.forEach { it -> val o = JSONObject(); o.put("uri", it.uri); o.put("title", it.title); o.put("type", it.type); o.put("note", it.note); o.put("favorite", it.favorite); arr.put(o) }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    var items by remember { mutableStateOf(load()) }
    var selected by remember { mutableStateOf<Item?>(null) }
    var fullScreen by remember { mutableStateOf<String?>(null) }
    var popupImage by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("ALL", "PHOTOS", "BIO", "NOTES")
    var editItem by remember { mutableStateOf<Item?>(null) }
    var editText by remember { mutableStateOf("") }
    var editNote by remember { mutableStateOf("") }
    var editType by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }
    var dedication by remember { mutableStateOf(prefs.getString("dedication", "Bella is our beloved cat. This archive holds her memories.") ?: "") }
    var showDedication by remember { mutableStateOf(true) }
    var player by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) player?.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(showDedication) {
        if (!showDedication) {
            val v = context.getSystemService(android.os.Vibrator::class.java)
            v?.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            val mp = android.media.MediaPlayer.create(context, com.bellaarchive.R.raw.hold_on)
            mp?.setVolume(0.5f, 0.5f)
            mp?.start()
            player = mp
        }
    }
    var editingDedication by remember { mutableStateOf(false) }

    val picker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
        }
        val newItems = uris.map { uri ->
            val mime = try { context.contentResolver.getType(uri) } catch (_: Exception) { null }
            val isImg = mime?.startsWith("image/") == true || (uri.lastPathSegment ?: "").lowercase().let { it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") || it.endsWith(".webp") }
            Item(uri.toString(), uri.lastPathSegment ?: "Item", if (isImg) "Photo" else "Letter")
        }
        items = (items + newItems).toMutableList()
        save(items)
    }

    BackHandler(enabled = fullScreen != null || selected != null) {
        if (fullScreen != null) fullScreen = null
        else selected = null
    }
    if (showDedication) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("ABOUT BELLA", color = Gold, fontSize = 14.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            if (editingDedication) {
                TextField(value = dedication, onValueChange = { dedication = it }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(onClick = { prefs.edit().putString("dedication", dedication).apply(); editingDedication = false }) { Text("Save") }
            } else {
                Text(dedication, color = Color.White, fontSize = 18.sp, fontFamily = FontFamily.Serif, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { editingDedication = true }) { Text("Edit") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { showDedication = false }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("Enter Archive") }
            }
        }
        return
    }
    if (popupImage != null) {
        val pItem = items.firstOrNull { it.uri == popupImage }
        AlertDialog(
            onDismissRequest = { popupImage = null },
            title = { Text(pItem?.title ?: "Item", color = Color.White) },
            text = {
                Column {
                    AsyncImage(model = Uri.parse(popupImage), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().height(300.dp).clickable { fullScreen = popupImage; popupImage = null })
                    if (!pItem?.note.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(pItem!!.note, color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { if (pItem != null) { items = items.filter { it != pItem }.toMutableList(); save(items); popupImage = null } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000))) { Text("Delete") }
                }
            },
            confirmButton = { Button(onClick = { popupImage = null }) { Text("Close") } },
            containerColor = NavyLight
        )
    }
    if (fullScreen != null) {
        Box(Modifier.fillMaxSize().background(Color.Black).clickable { fullScreen = null }, contentAlignment = Alignment.Center) {
            AsyncImage(model = Uri.parse(fullScreen), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
        }
        return
    }

    val filtered = (when (tab) {
        1 -> items.filter { it.type == "Photo" }
        else -> items
    }).sortedByDescending { it.favorite }

    // Notes
    data class Note(val id: Long, var text: String)
    fun loadNotes(): MutableList<Note> {
        val arr = org.json.JSONArray(prefs.getString("notes", "[]"))
        val l = mutableListOf<Note>()
        for (i in 0 until arr.length()) { val o = arr.getJSONObject(i); l.add(Note(o.getLong("id"), o.getString("text"))) }
        return l
    }
    fun saveNotes(l: List<Note>) {
        val arr = org.json.JSONArray()
        l.forEach { val o = org.json.JSONObject(); o.put("id", it.id); o.put("text", it.text); arr.put(o) }
        prefs.edit().putString("notes", arr.toString()).apply()
    }
    var notes by remember { mutableStateOf(loadNotes()) }
    var newNote by remember { mutableStateOf("") }
    var showNoteDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Text("❤️", fontSize = 200.sp, modifier = Modifier.align(Alignment.Center), color = Color.White.copy(alpha = 0.08f))
        Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(NavyLight).padding(12.dp).horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val isFolded = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 400
            if (isFolded) {
                Button(onClick = { player?.setVolume(0.5f, 0.5f); player?.start() }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("Play", fontSize = 9.sp) }
            Button(onClick = { player?.pause() }, colors = ButtonDefaults.buttonColors(containerColor = NavyLight, contentColor = Gold)) { Text("Stop", fontSize = 9.sp) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Bella's Archive by May", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Cursive)
            }
            } else {
                Button(onClick = { player?.setVolume(0.5f, 0.5f); player?.start() }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("Play", fontSize = 9.sp) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bella's Archive by May", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Cursive)
                }
                Button(onClick = { player?.pause() }, colors = ButtonDefaults.buttonColors(containerColor = NavyLight, contentColor = Gold)) { Text("Stop", fontSize = 9.sp) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.weight(1f).padding(8.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
            Column(Modifier.fillMaxWidth()) {
                when (tab) {
                    2 -> {
                        var bioPosts by remember { mutableStateOf(prefs.getString("bioPosts", "")!!.split("||").filter { it.isNotBlank() }.toMutableList()) }
                        var newBio by remember { mutableStateOf("") }
                        Column(Modifier.fillMaxSize().padding(8.dp)) {
                            Text("ABOUT BELLA", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            TextField(value = newBio, onValueChange = { newBio = it }, modifier = Modifier.height(120.dp), label = { Text("Write about Bella...") })
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { if (newBio.isNotBlank()) { bioPosts = (listOf(newBio) + bioPosts).toMutableList(); prefs.edit().putString("bioPosts", bioPosts.joinToString("||")).apply(); newBio = "" } }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("Post") }
                            Spacer(Modifier.height(8.dp))
                            LazyColumn(Modifier.fillMaxSize()) {
                                items(bioPosts) { post ->
                                    Card(colors = CardDefaults.cardColors(containerColor = NavyLight), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Column(Modifier.padding(10.dp)) {
                                            Text(post, color = Color.White, fontSize = 13.sp)
                                            Spacer(Modifier.height(4.dp))
                                            var confirmDel by remember { mutableStateOf(false) }
                                            androidx.compose.material3.TextButton(onClick = { confirmDel = true }) { Text("delete", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp) }
                                            if (confirmDel) {
                                                AlertDialog(onDismissRequest = { confirmDel = false }, title = { Text("Delete?", color = Color.White) }, text = { Text("This cannot be undone.", color = Color.White) }, confirmButton = { Button(onClick = { bioPosts = bioPosts.filter { it != post }.toMutableList(); prefs.edit().putString("bioPosts", bioPosts.joinToString("||")).apply(); confirmDel = false }) { Text("Delete") } }, dismissButton = { Button(onClick = { confirmDel = false }) { Text("Cancel") } }, containerColor = NavyLight)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        Button(onClick = { showNoteDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("ADD NOTE", fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(notes) { note ->
                                Card(colors = CardDefaults.cardColors(containerColor = NavyLight), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Column(Modifier.padding(10.dp)) {
                                        Text(note.text, color = Color.White, fontSize = 13.sp)
                                        Spacer(Modifier.height(4.dp))
                                        var confirmDel by remember { mutableStateOf(false) }
                                        androidx.compose.material3.TextButton(onClick = { confirmDel = true }) { Text("delete", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp) }
                                        if (confirmDel) {
                                            AlertDialog(onDismissRequest = { confirmDel = false }, title = { Text("Delete note?", color = Color.White) }, text = { Text("This cannot be undone.", color = Color.White) }, confirmButton = { Button(onClick = { notes = notes.filter { it != note }.toMutableList(); saveNotes(notes); confirmDel = false }) { Text("Delete") } }, dismissButton = { Button(onClick = { confirmDel = false }) { Text("Cancel") } }, containerColor = NavyLight)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                val f = java.io.File(context.filesDir, "backup.json")
                                f.writeText(org.json.JSONObject().apply { put("items", org.json.JSONArray().apply { items.forEach { o -> put(org.json.JSONObject().apply { put("uri", o.uri); put("title", o.title); put("type", o.type); put("note", o.note); put("favorite", o.favorite) }) } }); put("notes", org.json.JSONArray().apply { notes.forEach { o -> put(org.json.JSONObject().apply { put("id", o.id); put("text", o.text) }) } }); put("dedication", dedication) }.toString())
                            }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("Backup") }
                            Button(onClick = {
                                val f = java.io.File(context.filesDir, "backup.json")
                                if (f.exists()) {
                                    val o = org.json.JSONObject(f.readText())
                                    val arr = o.getJSONArray("items")
                                    val list = mutableListOf<Item>()
                                    for (i in 0 until arr.length()) { val x = arr.getJSONObject(i); list.add(Item(x.getString("uri"), x.getString("title"), x.getString("type"), x.optString("note", ""), x.optBoolean("favorite", false))) }
                                    items = list
                                    save(items)
                                    val nArr = o.getJSONArray("notes")
                                    val nList = mutableListOf<Note>()
                                    for (i in 0 until nArr.length()) { val x = nArr.getJSONObject(i); nList.add(Note(x.getLong("id"), x.getString("text"))) }
                                    notes = nList
                                    saveNotes(notes)
                                    dedication = o.optString("dedication", dedication)
                                    prefs.edit().putString("dedication", dedication).apply()
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = NavyLight, contentColor = Gold)) { Text("Restore") }
                        }
                    }
                    else -> {
                        Column(Modifier.fillMaxWidth().clickable { selected = null }) {
                            if (tab == 0) {
                                Spacer(Modifier.height(8.dp))
                                Text("Bio", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                prefs.getString("bioPosts", "")!!.split("||").filter { it.isNotBlank() }.forEach { post ->
                                    Card(colors = CardDefaults.cardColors(containerColor = NavyLight), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                        Text(post, color = Color.White, modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Notes", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                notes.forEach { note ->
                                    Card(colors = CardDefaults.cardColors(containerColor = NavyLight), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                        Text(note.text, color = Color.White, modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Photos", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            androidx.compose.foundation.layout.FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                filtered.forEach { item ->
                                    val uri = Uri.parse(item.uri)
                                    val mime = try { context.contentResolver.getType(uri) } catch (_: Exception) { null }
                                    val isImg = (mime?.startsWith("image/") == true) || item.type == "Photo"
                                    if (isImg) {
                                        AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(84.dp).aspectRatio(1f).clip(RoundedCornerShape(4.dp)).border(if (selected == item) 3.dp else 1.dp, if (selected == item) Color.White else Gold, RoundedCornerShape(4.dp)).pointerInput(item) { detectTapGestures(onTap = { selected = item; popupImage = item.uri }, onLongPress = { editItem = item; editText = item.title }) })
                                    } else {
                                        Card(colors = CardDefaults.cardColors(containerColor = Paper), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selected = item }.pointerInput(item) { detectTapGestures(onLongPress = { editItem = item; editText = item.title; editNote = item.note; editType = item.type }) }) {
                                            Text(item.title, color = Ink, modifier = Modifier.padding(10.dp), fontSize = 13.sp)
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
    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(NavyLight).padding(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = { picker.launch(arrayOf("image/*", "application/pdf", "*/*")) }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("ADD", fontWeight = FontWeight.Bold, fontSize = 10.sp) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            tabs.forEachIndexed { i, t ->
                Button(onClick = { tab = i }, colors = ButtonDefaults.buttonColors(containerColor = if (tab == i) Gold else NavyLight, contentColor = if (tab == i) Navy else Color.White), modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(t, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
	
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("New Note", color = Color.White) },
            text = { TextField(value = newNote, onValueChange = { newNote = it }, modifier = Modifier.height(200.dp)) },
            confirmButton = { Button(onClick = { notes = (notes + Note(System.currentTimeMillis(), newNote)).toMutableList(); saveNotes(notes); newNote = ""; showNoteDialog = false }) { Text("Save") } },
            dismissButton = { Button(onClick = { showNoteDialog = false }) { Text("Cancel") } },
            containerColor = NavyLight
        )
    }
    if (editItem != null) {
        AlertDialog(
            onDismissRequest = { editItem = null },
            title = { Text("Rename", color = Color.White) },
            text = { Column { TextField(value = editText, onValueChange = { editText = it }, label = { Text("Title") }); Spacer(Modifier.height(8.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { editType = "Letter" }, colors = ButtonDefaults.buttonColors(containerColor = if (editType == "Letter") Gold else NavyLight)) { Text("Letter") }; Button(onClick = { editType = "Photo" }, colors = ButtonDefaults.buttonColors(containerColor = if (editType == "Photo") Gold else NavyLight)) { Text("Photo") } }; Spacer(Modifier.height(8.dp)); TextField(value = editNote, onValueChange = { editNote = it }, label = { Text("Story note") }); Spacer(Modifier.height(8.dp)); Button(onClick = { editItem!!.favorite = !editItem!!.favorite }, colors = ButtonDefaults.buttonColors(containerColor = if (editItem!!.favorite) Gold else NavyLight)) { Text(if (editItem!!.favorite) "Favourited" else "Favourite") } } },
            confirmButton = { Button(onClick = { editItem!!.title = editText; editItem!!.note = editNote; editItem!!.type = editType; items = items.toMutableList(); save(items); editItem = null }) { Text("Save") } },
            dismissButton = { Button(onClick = { editItem = null }) { Text("Cancel") } },
            containerColor = NavyLight
        )
    }
}
}
