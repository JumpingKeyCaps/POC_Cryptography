# 🔐 File Encryption POC 📱

This **Proof of Concept (POC)** Android application demonstrates how to **securely encrypt and decrypt files** stored on a device using **symmetric encryption**.

The app leverages the **Storage Access Framework (SAF)** to let users select any folder on their device, then perform encryption or decryption operations directly on the files inside.

---

## 📂 Features

- 📁 **Folder-based encryption**: The user selects a folder, and all files within can be encrypted or decrypted.
- 🔑 **Symmetric AES encryption** with key derivation from a password using **PBKDF2WithHmacSHA256**.
- 🛡️ **Support for multiple AES modes**:
  - `CBC` + `PKCS5Padding`
  - `GCM` + `NoPadding`
  - `CTR` + `NoPadding`
- 🔐 **Configurable AES key size**: Choose between **128**, **192**, or **256-bit** encryption.
- 🔁 **Customizable IV iteration count** for enhanced control over the key derivation process.
- 🔐 **Mandatory password input** to encrypt/decrypt files securely.
- 🎨 **Modern Material Design 3 UI**:
  - **Top & Bottom App Bars** with custom styling.
  - Optimized **LazyColumn** for rendering large lists of files smoothly.
- 📄 Real-time file processing feedback, including selection, progress display, and operation status.

---

## 🛠️ Tech Stack

- **Kotlin** – Core language for Android development  
- **Jetpack Compose** – Declarative UI framework  
- **Material Design 3** – Polished, modern interface  
- **MVVM + Repository Pattern** – Clean architecture separation  
- **Coroutines** – Background task management  
- **AES + IV** – Symmetric encryption algorithm  
- **PBKDF2WithHmacSHA256** – Key derivation from user password  
- **Storage Access Framework (SAF)** – Secure user-driven file/folder access

---

## 🧠 How it Works

1. **Folder Selection**  
   The user selects a folder using the SAF file picker.

2. **Password Input**  
   A password must be provided to encrypt or decrypt files. It’s never stored and is only used to derive the cryptographic key.

3. **Encryption/Decryption**  
   - Choose an AES mode: `CBC`, `GCM`, or `CTR`.
   - Select key size: `128`, `192`, or `256` bits.
   - Define the number of IV derivation iterations.
   - The files in the selected folder are encrypted/decrypted and **replaced in place**.

4. **UI Feedback**  
   - Each file row shows live progress.
   - Files display their current status (encrypted/decrypted).

---

## 🔒 Security Practices

- 🔐 **PBKDF2WithHmacSHA256** for password-based key derivation  
- 🔁 **Random IV generation** using `SecureRandom`  
- 🛡️ **AES in secure modes**: `CBC`, `GCM`, and `CTR`  
- 📁 **Overwrites original files** to avoid leaving sensitive data unprotected  
- ⚠️ **User-chosen password is never saved** – it must be re-entered for each operation

---

## 🏗️ Architecture Overview

- **ViewModel**: Exposes UI state, handles user actions  
- **CryptoRepository**: Mediates between ViewModel and encryption logic  
- **CryptoService**: Core AES encryption/decryption engine  
- **SAFFile abstraction**: Encapsulates file access and metadata

---

## 📚 References

- [AES Encryption](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)  
- [PBKDF2 (Password-Based Key Derivation Function)](https://en.wikipedia.org/wiki/PBKDF2)  
- [Android Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider)

---


![Platform](https://img.shields.io/badge/platform-Android-3ddc84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/kotlin-1.9-blueviolet?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-%F0%9F%8E%B6-blue?logo=android)
![Min SDK](https://img.shields.io/badge/minSDK-33-brightgreen)


