# SSH Command Client (Android + Kotlin)

A simple Android app for executing SSH commands on remote servers. Built with **Jetpack Compose**.

<img width="230" height="450" alt="image" src="https://github.com/user-attachments/assets/c9e99617-9cdd-4074-a5db-a5afffb0a197" />

## **Features**
**SSH Connection**: Connect to a remote server using hostname/IP, port, username, and password.
**Command Execution**: Run commands (e.g., `ls`, `whoami`) and view outputs in a terminal-like interface.
**File Management**: Save session logs and view past commands.
**User-Friendly UI**: Clean and intuitive design built with Jetpack Compose.

## **Tech Stack**
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **State Management**: `remember` and `mutableStateOf`
- **Storage**: Room Database (SQLite Abstraction)
- **Networking**: JSch (Java Secure Channel)

## **How It Works**
1. **Connect to a Server**:
   - Enter the server’s hostname/IP, port, username, and password.
   - Tap "Connect" to establish an encrypted SSH tunnel via JSch.

2. **Execute Commands**:
   - Type commands (e.g., `ls`, `whoami`).
   - Tap "Send" to execute the command and view the output.

3. **Save and View Logs**:
   - Tap "Save" to serialize the current terminal session.
   - Logs are committed to the internal SQLite database and can be queried or deleted via the file manager overlay.

## **UI Design**
The app’s UI is built with Jetpack Compose and includes:
- **Connection Section**: Input fields for SSH credentials.
- **Terminal Output**: Displays command logs in a scrollable list.
- **Command Input**: Text field for entering commands.
- **File Manager**: Lists saved session logs fetched from the database.

## **Storage**
Originally built using flat text files, the storage layer has been upgraded to Room Database.
- Why Room? Replaced volatile file operations with SQLite Entities.
- Benefits: Prevents system interference (ghost files), enables structured queries, and allows for automatic timestamping of sessions.

## **Networking**
The app uses the JSch library to manage SSH connections.
- **Persistent Shell**: Unlike stateless command execution, we utilize ChannelShell to create a Pseudo-Terminal (PTY). This keeps the session "alive," allowing users to navigate directories (e.g., cd) just like a real desktop terminal.
- **Asynchronous Buffering**: To prevent UI freezing and latency, the network layer uses raw byte buffering instead of blocking `readLine
## ** Testing**
you can use rebex to test 
host - test.rebex.net
user - demo
pass - password
