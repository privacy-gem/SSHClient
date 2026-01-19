# SSH Command Client (Android + Kotlin)

A simple Android app for executing SSH commands on remote servers. Built with **Jetpack Compose**.

<img width="230" height="450" alt="image" src="https://github.com/user-attachments/assets/c9e99617-9cdd-4074-a5db-a5afffb0a197" />

---

## **Features**
**SSH Connection**: Connect to a remote server using hostname/IP, port, username, and password.
**Command Execution**: Run commands (e.g., `ls`, `whoami`) and view outputs in a terminal-like interface.
**File Management**: Save session logs and view past commands.
**User-Friendly UI**: Clean and intuitive design built with Jetpack Compose.

---

## **Tech Stack**
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **State Management**: `remember` and `mutableStateOf`
- **Storage**: [Insert]
- **Networking**: [insert] 

---

## **How It Works**
1. **Connect to a Server**:
   - Enter the server’s hostname/IP, port, username, and password.
   - Tap "Connect" to establish an SSH connection.

2. **Execute Commands**:
   - Type commands (e.g., `ls`, `whoami`).
   - Tap "Send" to execute the command and view the output.

3. **Save and View Logs**:
   - Save session logs to your device.
   - View or delete saved logs in the file manager.

---

## **UI Design**
The app’s UI is built with **Jetpack Compose** and includes:
- **Connection Section**: Input fields for SSH credentials.
- **Terminal Output**: Displays command logs in a scrollable list.
- **Command Input**: Text field for entering commands.
- **File Manager**: Lists saved session logs.

---

## **Storage**
- Session logs are saved as text files in the app’s internal storage.
- Files are listed and can be viewed or deleted in the file manager.

---

## **Networking**
