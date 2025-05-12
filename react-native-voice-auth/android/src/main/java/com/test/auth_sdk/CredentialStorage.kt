package com.test.auth_sdk

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Secure credential storage for VoiceCore authentication
 * 
 * Implements secure storage of user credentials using Android KeyStore
 * following the VoiceCore MVP security requirements.
 */
class CredentialStorage(private val context: Context) {

    companion object {
        private const val TAG = "sip_CredentialStorage" // Telephony-specific prefix
        private const val KEY_STORE_TYPE = "AndroidKeyStore"
        private const val SHARED_PREFS_NAME = "voice_auth_credentials"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12 // For GCM
        private const val KEY_SIZE = 256
    }

    private val keyStore: KeyStore
    private val keyAlias = "VoiceAuthCredentialKey"
    
    init {
        try {
            keyStore = KeyStore.getInstance(KEY_STORE_TYPE).apply { load(null) }
            if (!keyStore.containsAlias(keyAlias)) {
                generateKey()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing KeyStore: ${e.message}")
            throw SecurityException("Failed to initialize secure storage", e)
        }
    }

    /**
     * Generate a secure encryption key
     * 
     * @throws NoSuchAlgorithmException if the requested algorithm isn't available
     * @throws NoSuchProviderException if the KeyStore provider isn't available
     * @throws KeyStoreException if the KeyStore operation fails
     */
    private fun generateKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, 
                KEY_STORE_TYPE
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .setRandomizedEncryptionRequired(true)
                .build()
                
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
            
            Log.d(TAG, "Secure key generated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating encryption key: ${e.message}")
            throw SecurityException("Failed to generate secure key", e)
        }
    }

    /**
     * Store user credentials securely
     * 
     * @param username The username to store
     * @param password The password to encrypt and store
     * @throws SecurityException if encryption fails
     */
    fun storeCredential(username: String, password: String) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

            val iv = cipher.iv
            val encryptedPassword = cipher.doFinal(password.toByteArray())

            // Combine IV and encrypted data
            val combinedData = Base64.encodeToString(iv + encryptedPassword, Base64.DEFAULT)

            // Store the combined data in SharedPreferences
            val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(username, combinedData).apply()
            
            Log.d(TAG, "Credential stored successfully for user: $username")
        } catch (e: Exception) {
            Log.e(TAG, "Error storing credential: ${e.message}")
            throw SecurityException("Failed to securely store credential", e)
        }
    }

    /**
     * Retrieve stored credentials
     * 
     * @param username The username to retrieve credentials for
     * @return The decrypted password or null if not found
     * @throws SecurityException if decryption fails
     */
    fun retrieveCredential(username: String): String? {
        try {
            val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val combinedData = sharedPreferences.getString(username, null) ?: return null

            val decodedCombinedData = Base64.decode(combinedData, Base64.DEFAULT)
            
            // Extract IV and encrypted password
            val iv = decodedCombinedData.copyOfRange(0, IV_LENGTH)
            val encryptedPassword = decodedCombinedData.copyOfRange(IV_LENGTH, decodedCombinedData.size)

            // Initialize cipher for decryption
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(IV_LENGTH * 8, iv))

            // Decrypt and return the password
            return String(cipher.doFinal(encryptedPassword))
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving credential: ${e.message}")
            return null
        }
    }

    /**
     * Delete stored credentials
     * 
     * @param username The username to delete credentials for
     * @return true if credentials were deleted, false otherwise
     */
    fun deleteCredential(username: String): Boolean {
        return try {
            val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val exists = sharedPreferences.contains(username)
            
            if (exists) {
                sharedPreferences.edit().remove(username).apply()
                Log.d(TAG, "Credential deleted for user: $username")
            }
            
            exists
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting credential: ${e.message}")
            false
        }
    }

    /**
     * Get the secret key from the Android KeyStore
     * 
     * @return The secret key used for encryption/decryption
     * @throws KeyStoreException if the key cannot be retrieved
     */
    private fun getSecretKey(): SecretKey {
        return try {
            (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
        } catch (e: Exception) {
            Log.e(TAG, "Error getting secret key: ${e.message}")
            throw KeyStoreException("Failed to retrieve encryption key", e)
        }
    }
}
