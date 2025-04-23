package com.example.fortune_whell_v3.Utils

import android.content.Context
import android.provider.Settings
import android.content.SharedPreferences
import android.bluetooth.BluetoothDevice



class DeviceUtils {
    companion object {
        /**
         * Obtém o Android ID do dispositivo.
         * @param context Contexto necessário para acessar o contentResolver.
         * @return Android ID do dispositivo como String.
         */
        fun getAndroidId(context: Context): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }
}
