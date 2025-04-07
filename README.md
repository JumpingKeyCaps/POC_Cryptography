This POC is a demonstration Android application that allows users to encrypt and decrypt local files using symmetric encryption.

Encryption is performed using the AES (Advanced Encryption Standard) algorithm in CBC (Cipher Block Chaining) mode,
with a user password used to derive a key through a secure key derivation algorithm (PBKDF2WithHmacSHA256).

The project also explores different methods to secure files on an Android device.


The app provides a simple interface allowing the user to:

 - Select a file from the local storage of the device.

 - Encrypt the file by entering a password via a dialog.

 - Decrypt a file by providing the password used for encryption.

 - Choose from different encryption algorithms, with a clear architecture to allow adding more encryption types
   in the future (symmetric, asymmetric, etc.).


The app follows best security practices to handle sensitive data and is based on an MVVM architecture
with a repository pattern for clean business logic management.



%%% Features:

 - Symmetric AES encryption with key derivation from a password (via PBKDF2).

 - AES decryption for files.

 - User interface to select files from local storage.

 - Password entry dialog for key derivation during encryption.

 - Support for easily adding other encryption types in the future (e.g., asymmetric encryption, hashing, etc.).

 - Simple UI for interacting with file encryption and decryption.


%%% Tech Stack:

 - Kotlin for Android development.

 - Jetpack Compose for building the user interface.

 - MVVM with a repository pattern for separating business logic from the UI.

 - Coroutines for handling background tasks (encryption/decryption).

 - AES for symmetric encryption, with an IV (initialization vector) for each encryption.

 - PBKDF2WithHmacSHA256 for secure key derivation from the user password.

 - SecureRandom for generating random salt and IV to enhance security.



%%% Architecture:

The app follows the MVVM architecture for clear separation between the business logic (ViewModel) and the user interface (UI).
The file encryption and decryption process is encapsulated in a CryptoService,
and a CryptoRepository interacts with this service from the ViewModel.
