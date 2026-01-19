package com.example.sshclientcompose

import android.os.Bundle
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.FileOutputStream
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.font.FontFamily



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
    /* Variables */
    var host by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var password by remember { mutableStateOf("") }

    var commandInput by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }

    var showFileManager by remember { mutableStateOf(false) }
    var viewingFileContent by remember { mutableStateOf<String?>(null) } //Holds content of file being viewed

    val logs = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var savedFiles by remember { mutableStateOf(listOf<java.io.File>()) }

    //Helper function to load files from internal storage
    fun loadFiles() {
        val filesDir = context.filesDir
        savedFiles = filesDir.listFiles()?.filter { it.isFile} ?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    //Main App Content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        // HEADER (DARK BLUE SECTION)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(16.dp)
        ) {
            // Top Bar with Title and File Manager Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SSH Client",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    loadFiles()
                    showFileManager = true
                }) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Files", tint = Color.White)
            }
        }
            Spacer(modifier = Modifier.height(10.dp))

            // Row 1: Host and Port
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                // Host Input
                TextField(
                    value = host, //shows host variable
                    onValueChange = { host = it },
                    label = { Text("Host/IP") },
                    enabled = !isConnected, //locked when connected
                    modifier = Modifier.weight(2f), //host gets more space
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    )
                )
                // Port Input
                TextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    enabled = !isConnected,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Row 2: Username and Password
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                // USERNAME INPUT
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    enabled = !isConnected,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    )
                )
                // PASSWORD INPUT (hides text)
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    enabled = !isConnected,
                    modifier = Modifier.weight(1f),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // CONNECT BUTTON
            Button(
                onClick = {
                    if (isConnected) {
                        isConnected = false
                        logs.add ("System: Disconnected from $host")
                    } else {
                        if (host.isNotEmpty() && username.isNotEmpty()) {
                            scope.launch {
                                logs.add("System: Connecting to $host as $username")
                                delay(1000)
                                logs.add("System: Connected to $host as $username")
                                isConnected = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) Color.Red else Color.Blue,
                ),
                ) {
                Text(if (isConnected) "Disconnect" else "Connect"
            )
            }
        }
        // TERMINAL OUTPUT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp)
                .background(Color.Black)
                .padding(8.dp)
        ){
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs) { logLine ->
                    Text(
                        text = logLine,
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
        //BOTTOM SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp)
        ) {
            //PUTS INPUT FIELD AND SEND BUTTON SIDE BY SIDE
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    placeholder = { Text("Enter command...") },
                    enabled = isConnected, //Only enable if connected
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (commandInput.isNotEmpty()) {
                            logs.add("$ $commandInput")

                            val response = when (commandInput.trim()){
                                "ls" -> "documents downloads public_html"
                                "whoami" -> username
                                "pwd" -> "/home/$username"
                                "clear" -> "__CLEAR__"
                                "uptime" -> "14:23:01 up 42 days, 1 user, load average: 0.05"
                                "help" -> "Try: ls, whoami, pwd, clear, help"
                                else -> "bash: $commandInput: command not found"
                            }
                            if (response == "__CLEAR__"){
                                logs.clear()
                            } else {
                                logs.add(response)
                            }
                            commandInput = "" //Clear textbox
                        }
                        },
                    enabled = isConnected //Disable button if disconnected
                ) {
                    Text("Send")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            //Save Button
            Button(onClick = {
                    val filename =
                        "ssh_session_${System.currentTimeMillis()}.txt" //create unique filename based on time
                    val content =
                        "SSH Session Log\nHost: $host\nUsername: $username\n\n" + logs.joinToString(
                            "\n"
                        ) // combine the log lines into one string

                    try { //write to internal storage
                        val fileOutputStream: FileOutputStream =
                            context.openFileOutput(filename, Context.MODE_PRIVATE)
                        fileOutputStream.write(content.toByteArray())
                        fileOutputStream.close()
                        Toast.makeText(context, "Log saved to $filename", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Error saving log", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) { Text("Save Session to File") }
        }
    }
    // ------ FileManager Overlay -----
    if (showFileManager) {
        // Surface acts like a card/popup window
        Surface(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            shadowElevation = 10.dp,
            color = Color.White
        ){
            Column(modifier = Modifier.padding(16.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Saved Files", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // File List
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(savedFiles) { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewingFileContent = file.readText()
                                } // Clicking on a file will show its content
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(file.name)
                            IconButton(onClick = {
                                file.delete()
                                loadFiles() //Refresh file list
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
    // ------ FileViewer Overlay -----
    if (viewingFileContent != null) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            shadowElevation = 15.dp,
            color = Color.Black,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(onClick = { viewingFileContent = null }) {
                    Text("Close Viewer")
                }
                LazyColumn{
                    item {
                        Text(
                            text = viewingFileContent ?: "",
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
