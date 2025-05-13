package com.lebaillyapp.poc_cryptography.v2

sealed class BottomDialogState {
    data object None : BottomDialogState()
    data object CryptModeSelector : BottomDialogState()
    data object PasswordKeySelector : BottomDialogState()
    data object IVCountSelector : BottomDialogState()
    data object MultiCrypt : BottomDialogState()
    data object Bitrate : BottomDialogState()
}