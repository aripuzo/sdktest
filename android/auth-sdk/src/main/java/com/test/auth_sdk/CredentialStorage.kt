package com.test.auth_sdk

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CredentialStorage(private val context: Context) {

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "MyCredentialKey"
    private val transformation = "AES/GCM/NoPadding"
    private val ivLength = 12 // For GCM

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    fun storeCredential(username: String, password: String) {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedPassword = cipher.doFinal(password.toByteArray())

        val combinedData = Base64.encodeToString(iv + encryptedPassword, Base64.DEFAULT)

        // Store the combined data in a secure storage (e.g., SharedPreferences)
        val sharedPreferences = context.getSharedPreferences("credentials", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(username, combinedData).apply()
    }

    fun retrieveCredential(username: String): String? {
        val sharedPreferences = context.getSharedPreferences("credentials", Context.MODE_PRIVATE)
        val combinedData = sharedPreferences.getString(username, null)

        if (combinedData != null) {
            val cipher = Cipher.getInstance(transformation)
            val decodedCombinedData = Base64.decode(combinedData, Base64.DEFAULT)
            val iv = decodedCombinedData.copyOfRange(0, ivLength)
            val encryptedPassword = decodedCombinedData.copyOfRange(ivLength, decodedCombinedData.size)

            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(ivLength * 8, iv))

            return String(cipher.doFinal(encryptedPassword))
        }

        return null
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }
}