package com.example.sshclientcompose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.*
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.Date

// ================= DATABASE CLASSES =================

@Entity(tableName = "ssh_sessions")
data class SshSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val host: String,
    val timestamp: Long,
    val content: String
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM ssh_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SshSession>>

    @Insert
    suspend fun insertSession(session: SshSession)

    @Delete
    suspend fun deleteSession(session: SshSession)
}

@Database(entities = [SshSession::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}

// ================= MAIN UI =================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SSHClientApp()
            }
        }
    }
}

@Composable
fun SSHClientApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database Setup
    val db = remember { Room.databaseBuilder(context, AppDatabase::class.java, "ssh_database").build() }
    val sessionDao = db.sessionDao()
    val savedSessions by sessionDao.getAllSessions().collectAsState(initial = emptyList())

    // SSH & UI States
    var host by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var password by remember { mutableStateOf("") }
    var commandInput by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }
    var showDbManager by remember { mutableStateOf(false) }
    var viewingSessionContent by remember { mutableStateOf<String?>(null) }

    val logs = remember { mutableStateListOf<String>() }
    val scrollState = rememberLazyListState()

    var session by remember { mutableStateOf<Session?>(null) }
    var channel by remember { mutableStateOf<ChannelShell?>(null) }
    var commandWriter by remember { mutableStateOf<OutputStream?>(null) }

    fun cleanSshOutput(text: String): String = text.replace(Regex("\u001B\\[[;\\d]*[A-Za-z]"), "")

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) scrollState.animateScrollToItem(logs.size - 1)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6))) {

            // HEADER
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B)).padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SSH Terminal (SQL Database)", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showDbManager = true }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "History", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = host, onValueChange = { host = it }, label = { Text("Host") }, enabled = !isConnected, modifier = Modifier.weight(2f))
                    TextField(value = port, onValueChange = { port = it }, label = { Text("Port") }, enabled = !isConnected, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = username, onValueChange = { username = it }, label = { Text("User") }, enabled = !isConnected, modifier = Modifier.weight(1f))
                    TextField(value = password, onValueChange = { password = it }, label = { Text("Pass") }, enabled = !isConnected, modifier = Modifier.weight(1f), visualTransformation = PasswordVisualTransformation())
                }
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (isConnected) {
                            channel?.disconnect(); session?.disconnect()
                            isConnected = false; logs.clear(); logs.add("System: Disconnected.")
                        } else {
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val jsch = JSch()
                                    val newSession = jsch.getSession(username, host, port.toIntOrNull() ?: 22)
                                    newSession.setPassword(password)
                                    newSession.setConfig("StrictHostKeyChecking", "no")
                                    newSession.connect(5000)
                                    val newChannel = newSession.openChannel("shell") as ChannelShell
                                    val inputStream = newChannel.inputStream
                                    commandWriter = newChannel.outputStream
                                    newChannel.connect()
                                    session = newSession; channel = newChannel
                                    withContext(Dispatchers.Main) { isConnected = true }

                                    val reader = inputStream.bufferedReader()
                                    val buffer = CharArray(1024)
                                    while (newChannel.isConnected) {
                                        if (reader.ready()) {
                                            val charsRead = reader.read(buffer)
                                            if (charsRead > 0) {
                                                val chunk = String(buffer, 0, charsRead)
                                                withContext(Dispatchers.Main) {
                                                    chunk.split("\n").forEach { line ->
                                                        if (line.isNotBlank()) logs.add(cleanSshOutput(line))
                                                    }
                                                }
                                            }
                                        }
                                        Thread.sleep(50)
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) { logs.add("Error: ${e.message}") }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isConnected) Color.Red else Color.Blue)
                ) { Text(if (isConnected) "Disconnect" else "Connect") }
            }

            // TERMINAL
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(10.dp).background(Color.Black).padding(8.dp)) {
                LazyColumn(state = scrollState, modifier = Modifier.fillMaxSize()) {
                    items(logs) { logLine ->
                        Text(text = logLine, color = Color.Green, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                    }
                }
            }

            // INPUT & DATABASE SAVE
            Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(value = commandInput, onValueChange = { commandInput = it }, placeholder = { Text("Command...") }, enabled = isConnected, modifier = Modifier.weight(1f))
                    Button(onClick = {
                        if (commandInput.isNotEmpty() && isConnected) {
                            val cmd = commandInput + "\n"
                            commandInput = ""
                            scope.launch(Dispatchers.IO) {
                                commandWriter?.write(cmd.toByteArray())
                                commandWriter?.flush()
                            }
                        }
                    }, enabled = isConnected) { Text("Send") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    if (logs.isNotEmpty()) {
                        val sessionData = SshSession(
                            host = host,
                            timestamp = System.currentTimeMillis(),
                            content = logs.joinToString("\n")
                        )
                        scope.launch(Dispatchers.IO) {
                            sessionDao.insertSession(sessionData)
                        }
                        Toast.makeText(context, "Session Saved to SQL!", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                    Text("Save to SQLite Database")
                }
            }
        }

        // DATABASE MANAGER OVERLAY
        if (showDbManager) {
            Surface(modifier = Modifier.fillMaxSize().padding(16.dp), color = Color.White, shadowElevation = 10.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Session History (SQL)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { showDbManager = false }) { Icon(Icons.Default.Close, "Close") }
                    }
                    HorizontalDivider()
                    LazyColumn {
                        items(savedSessions) { sessionItem ->
                            ListItem(
                                headlineContent = { Text(sessionItem.host) },
                                supportingContent = { Text(Date(sessionItem.timestamp).toString()) },
                                modifier = Modifier.clickable { viewingSessionContent = sessionItem.content },
                                trailingContent = {
                                    IconButton(onClick = { scope.launch(Dispatchers.IO) { sessionDao.deleteSession(sessionItem) } }) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // VIEW CONTENT OVERLAY
        if (viewingSessionContent != null) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(onClick = { viewingSessionContent = null }) { Text("Close") }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        item { Text(viewingSessionContent!!, color = Color.Green, fontFamily = FontFamily.Monospace) }
                    }
                }
            }
        }
    }
}