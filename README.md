# 🔐 File Encryption POC 📱

This **Proof of Concept (POC)** is a demonstration Android application that allows users to **encrypt** and **decrypt** local files using **symmetric encryption**.  

🔒 **Encryption** is performed using the **AES (Advanced Encryption Standard)** algorithm in **CBC (Cipher Block Chaining)** mode, with a **user password** used to derive a key through a secure key derivation algorithm (**PBKDF2WithHmacSHA256**).

The project also explores different methods to **secure files** on an Android device.

---

## 📂 Features:

- 🔑 **Symmetric AES encryption** with key derivation from a password (via PBKDF2).
- 🔓 **AES decryption** for files.
- 🗂️ **User interface** to select files from local storage.
- 📝 **Password entry dialog** for key derivation during encryption.
- ➕ **Support for easily adding other encryption types** in the future (e.g., asymmetric encryption, hashing, etc.).
- 💻 **Simple UI** for interacting with file encryption and decryption.

---

## 🛠️ Tech Stack:

- **Kotlin** for Android development.  
- **Jetpack Compose** for building the user interface.  
- **MVVM** with a **repository pattern** for separating business logic from the UI.  
- **Coroutines** for handling background tasks (encryption/decryption).  
- **AES** for symmetric encryption, with an IV (initialization vector) for each encryption.  
- **PBKDF2WithHmacSHA256** for secure key derivation from the user password.  
- **SecureRandom** for generating random salt and IV to enhance security.

---

## 🏗️ Architecture:

This app follows the **MVVM** (Model-View-ViewModel) architecture for a **clear separation of concerns** between business logic and the user interface.

- **ViewModel**: Handles the UI-related data and interacts with the repository.  
- **CryptoService**: Encapsulates the file encryption and decryption logic.  
- **CryptoRepository**: A layer between the ViewModel and the CryptoService to manage business logic.

---

## ⚙️ How it Works:

1. **File Selection**: Choose a file from local storage.
2. **Encryption**: Enter a password to derive a key and encrypt the file using AES.
3. **Decryption**: Provide the same password used for encryption to decrypt the file.
4. **Future Enhancements**: Easily extend the app to support asymmetric encryption, hashing, or other cryptographic techniques.

---

## 📌 Security Practices:

This app follows **best security practices** to handle sensitive data:

- Use of **PBKDF2WithHmacSHA256** for secure key derivation from passwords.
- **AES encryption** in **CBC mode** to ensure confidentiality.
- **Random IV generation** for each encryption operation to prevent attack patterns.
- **SecureRandom** for generating random salts and IVs to enhance encryption security.

---

## 🚀 Future Improvements:

- Add support for **asymmetric encryption** (RSA, etc.).
- Implement **file integrity checks** using hashing algorithms (SHA256, etc.).
- Enhance the user interface for a more **seamless experience**.

---

## 🤝 Contributing:

Feel free to fork the project, open issues, or submit pull requests to contribute improvements, enhancements, or new encryption techniques.

---

## 📚 References:

- [AES Encryption](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)
- [PBKDF2WithHmacSHA256](https://en.wikipedia.org/wiki/PBKDF2)
