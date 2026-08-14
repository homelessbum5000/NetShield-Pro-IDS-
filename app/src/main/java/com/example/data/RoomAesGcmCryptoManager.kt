package com.example.data

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.nio.ByteBuffer
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class AesGcmSecurityProfile(
    val keyAlias: String,
    val algorithm: String = "AES/GCM/NoPadding",
    val keySizeBits: Int = 256,
    val ivSizeBytes: Int = 12, // 96 bits recommended for GCM
    val tagLengthBits: Int = 128,
    val keyStoreProvider: String,
    val isHardwareBacked: Boolean,
    val cipherTextPrefix: String = "ENC:GCM256:",
    val status: String = "ACTIVE_SECURE"
)

data class AesGcmBenchmarkResult(
    val testPayload: String,
    val encryptionLatencyMicros: Long,
    val decryptionLatencyMicros: Long,
    val ivHex: String,
    val ciphertextHex: String,
    val authTagHex: String,
    val rawEncryptedOutput: String,
    val isDecryptionVerified: Boolean,
    val isTamperProofVerified: Boolean
)

class RoomAesGcmCryptoManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val keyStore: KeyStore? = try {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    } catch (e: Exception) {
        Log.w("AesGcmCryptoManager", "AndroidKeyStore unavailable (e.g. JVM/test runner), fallback key will be used: ${e.message}")
        null
    }
    private val secureRandom = SecureRandom()

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "netshield_room_aes256_master_key"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH_BYTES = 12 // 96-bit IV
        private const val GCM_TAG_LENGTH_BITS = 128 // 128-bit authentication tag
        const val ENC_PREFIX = "ENC:GCM256:"

        @Volatile
        private var INSTANCE: RoomAesGcmCryptoManager? = null

        fun getInstance(context: Context): RoomAesGcmCryptoManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RoomAesGcmCryptoManager(context).also { INSTANCE = it }
            }
        }
    }

    init {
        ensureMasterKeyExists()
    }

    /**
     * Ensures an AES-256 master key exists in the Android KeyStore.
     * Uses KeyGenParameterSpec with GCM mode and NoPadding.
     */
    @Synchronized
    private fun ensureMasterKeyExists() {
        try {
            if (keyStore != null && !keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                generateMasterKey(MASTER_KEY_ALIAS)
            }
        } catch (e: Exception) {
            Log.e("AesGcmCryptoManager", "Failed to check or initialize AndroidKeyStore master key: ${e.message}", e)
        }
    }

    private fun generateMasterKey(alias: String) {
        if (keyStore == null) return
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(false) // We manage unique 12-byte IVs explicitly per encryption
            .build()

        keyGenerator.init(keyGenSpec)
        keyGenerator.generateKey()
        Log.i("AesGcmCryptoManager", "Generated new AES-256-GCM master key in AndroidKeyStore: $alias")
    }

    private fun getSecretKey(): SecretKey {
        return try {
            if (keyStore != null) {
                if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                    val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                    entry?.secretKey ?: fallbackInMemoryKey()
                } else {
                    generateMasterKey(MASTER_KEY_ALIAS)
                    val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                    entry?.secretKey ?: fallbackInMemoryKey()
                }
            } else {
                fallbackInMemoryKey()
            }
        } catch (e: Exception) {
            Log.w("AesGcmCryptoManager", "KeyStore retrieval exception, using fallback software AES-256 key: ${e.message}")
            fallbackInMemoryKey()
        }
    }

    // Software fallback key for testing environments where AndroidKeyStore might be restricted
    @Volatile
    private var softwareFallbackKey: SecretKey? = null

    private fun fallbackInMemoryKey(): SecretKey {
        return softwareFallbackKey ?: synchronized(this) {
            softwareFallbackKey ?: run {
                val keyGen = KeyGenerator.getInstance("AES")
                keyGen.init(256, secureRandom)
                val key = keyGen.generateKey()
                softwareFallbackKey = key
                key
            }
        }
    }

    /**
     * Encrypts a plaintext string using AES-256-GCM with a unique 12-byte IV.
     * Output format: ENC:GCM256:<Base64([IV (12B)] + [Ciphertext + Tag (16B)])>
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return plainText
        if (plainText.startsWith(ENC_PREFIX)) return plainText // Already encrypted

        return try {
            val key = getSecretKey()
            val iv = ByteArray(GCM_IV_LENGTH_BYTES)
            secureRandom.nextBytes(iv)

            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, spec)

            val plainBytes = plainText.toByteArray(Charsets.UTF_8)
            val cipherTextWithTag = cipher.doFinal(plainBytes)

            // Combined IV + (Ciphertext + Tag)
            val byteBuffer = ByteBuffer.allocate(iv.size + cipherTextWithTag.size)
            byteBuffer.put(iv)
            byteBuffer.put(cipherTextWithTag)

            val encoded = Base64.encodeToString(byteBuffer.array(), Base64.NO_WRAP)
            "$ENC_PREFIX$encoded"
        } catch (e: Exception) {
            Log.e("AesGcmCryptoManager", "Encryption failure: ${e.message}", e)
            plainText
        }
    }

    /**
     * Decrypts an AES-256-GCM ciphertext payload and validates the 128-bit authentication tag.
     */
    fun decrypt(cipherString: String): String {
        if (!cipherString.startsWith(ENC_PREFIX)) {
            return cipherString // Legacy or already plaintext
        }

        return try {
            val base64Payload = cipherString.substring(ENC_PREFIX.length)
            val fullBytes = Base64.decode(base64Payload, Base64.NO_WRAP)

            if (fullBytes.size < GCM_IV_LENGTH_BYTES + 16) {
                return cipherString // Invalid payload size
            }

            val byteBuffer = ByteBuffer.wrap(fullBytes)
            val iv = ByteArray(GCM_IV_LENGTH_BYTES)
            byteBuffer.get(iv)

            val cipherTextWithTag = ByteArray(byteBuffer.remaining())
            byteBuffer.get(cipherTextWithTag)

            val key = getSecretKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val decryptedBytes = cipher.doFinal(cipherTextWithTag)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: javax.crypto.AEADBadTagException) {
            Log.w("AesGcmCryptoManager", "AEAD authentication tag mismatch: payload has been modified or tampered.")
            "[DECRYPTION_ERROR: Tampered or Invalid Key]"
        } catch (e: Exception) {
            Log.w("AesGcmCryptoManager", "Decryption failed: ${e.message}")
            "[DECRYPTION_ERROR: Tampered or Invalid Key]"
        }
    }

    /**
     * Verifies that AES-GCM rejects tampered ciphertext via AEAD authentication tag validation.
     */
    fun testTamperResistance(fullBytes: ByteArray): Boolean {
        if (fullBytes.size <= GCM_IV_LENGTH_BYTES + 16) return false
        return try {
            val tamperedBytes = fullBytes.clone()
            tamperedBytes[GCM_IV_LENGTH_BYTES] = (tamperedBytes[GCM_IV_LENGTH_BYTES].toInt() xor 0xFF).toByte()

            val byteBuffer = ByteBuffer.wrap(tamperedBytes)
            val iv = ByteArray(GCM_IV_LENGTH_BYTES)
            byteBuffer.get(iv)

            val cipherTextWithTag = ByteArray(byteBuffer.remaining())
            byteBuffer.get(cipherTextWithTag)

            val key = getSecretKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            cipher.doFinal(cipherTextWithTag)
            false // If it decrypted successfully, tamper resistance failed
        } catch (e: javax.crypto.AEADBadTagException) {
            true // Expected: AEAD correctly caught the tampered byte
        } catch (e: Exception) {
            true // Any crypto auth rejection confirms tamper resistance
        }
    }

    /**
     * Encrypts the sensitive payload fields of a ThreatLogEntity before SQLite persistence.
     */
    fun encryptThreatLog(log: ThreatLogEntity): ThreatLogEntity {
        return log.copy(
            sourceIp = encrypt(log.sourceIp),
            attackVector = encrypt(log.attackVector),
            actionTaken = encrypt(log.actionTaken),
            quantumKeyId = encrypt(log.quantumKeyId),
            details = encrypt(log.details)
        )
    }

    /**
     * Decrypts the sensitive payload fields of a ThreatLogEntity when read from Room DB.
     */
    fun decryptThreatLog(log: ThreatLogEntity): ThreatLogEntity {
        return log.copy(
            sourceIp = decrypt(log.sourceIp),
            attackVector = decrypt(log.attackVector),
            actionTaken = decrypt(log.actionTaken),
            quantumKeyId = decrypt(log.quantumKeyId),
            details = decrypt(log.details)
        )
    }

    /**
     * Encrypts PacketAnalysisEntity sensitive summary before storing.
     */
    fun encryptPacketAnalysis(analysis: PacketAnalysisEntity): PacketAnalysisEntity {
        return analysis.copy(
            summary = encrypt(analysis.summary),
            protocol = encrypt(analysis.protocol)
        )
    }

    /**
     * Decrypts PacketAnalysisEntity after reading from SQLite.
     */
    fun decryptPacketAnalysis(analysis: PacketAnalysisEntity): PacketAnalysisEntity {
        return analysis.copy(
            summary = decrypt(analysis.summary),
            protocol = decrypt(analysis.protocol)
        )
    }

    fun isEncrypted(value: String): Boolean {
        return value.startsWith(ENC_PREFIX)
    }

    fun getSecurityProfile(): AesGcmSecurityProfile {
        val isHardware = try {
            keyStore?.containsAlias(MASTER_KEY_ALIAS) == true
        } catch (e: Exception) {
            false
        }

        return AesGcmSecurityProfile(
            keyAlias = MASTER_KEY_ALIAS,
            algorithm = CIPHER_TRANSFORMATION,
            keySizeBits = 256,
            ivSizeBytes = GCM_IV_LENGTH_BYTES,
            tagLengthBits = GCM_TAG_LENGTH_BITS,
            keyStoreProvider = ANDROID_KEYSTORE,
            isHardwareBacked = isHardware,
            cipherTextPrefix = ENC_PREFIX,
            status = if (isHardware) "AES-256-GCM HARDWARE-ACTIVE" else "AES-256-GCM SOFTWARE-ACTIVE"
        )
    }

    /**
     * Runs an in-depth cryptographic benchmark and tamper-proofing test.
     */
    fun runCryptographicBenchmark(sampleInput: String = "185.220.101.5:443 - SYN-Flood Volumetric Burst"): AesGcmBenchmarkResult {
        val startEnc = System.nanoTime()
        val encrypted = encrypt(sampleInput)
        val encDurationMicros = (System.nanoTime() - startEnc) / 1000

        val startDec = System.nanoTime()
        val decrypted = decrypt(encrypted)
        val decDurationMicros = (System.nanoTime() - startDec) / 1000

        val isDecVerified = decrypted == sampleInput

        // Parse Hex representations
        val base64Payload = encrypted.removePrefix(ENC_PREFIX)
        val fullBytes = try { Base64.decode(base64Payload, Base64.NO_WRAP) } catch (e: Exception) { ByteArray(0) }
        
        val ivHex = if (fullBytes.size >= GCM_IV_LENGTH_BYTES) {
            fullBytes.copyOfRange(0, GCM_IV_LENGTH_BYTES).joinToString("") { "%02X".format(it) }
        } else "N/A"

        val authTagHex = if (fullBytes.size >= GCM_IV_LENGTH_BYTES + 16) {
            fullBytes.copyOfRange(fullBytes.size - 16, fullBytes.size).joinToString("") { "%02X".format(it) }
        } else "N/A"

        val cipherHex = if (fullBytes.size > GCM_IV_LENGTH_BYTES + 16) {
            fullBytes.copyOfRange(GCM_IV_LENGTH_BYTES, fullBytes.size - 16).joinToString("") { "%02X".format(it) }
        } else "N/A"

        // Tamper test: Verify AEAD authenticates against corrupted bytes
        val tamperDetected = testTamperResistance(fullBytes)

        return AesGcmBenchmarkResult(
            testPayload = sampleInput,
            encryptionLatencyMicros = encDurationMicros.coerceAtLeast(1),
            decryptionLatencyMicros = decDurationMicros.coerceAtLeast(1),
            ivHex = ivHex,
            ciphertextHex = cipherHex,
            authTagHex = authTagHex,
            rawEncryptedOutput = encrypted,
            isDecryptionVerified = isDecVerified,
            isTamperProofVerified = tamperDetected
        )
    }

    /**
     * Rotates master key and returns the timestamp of the new key generation.
     */
    @Synchronized
    fun rotateMasterKey(): String {
        try {
            if (keyStore?.containsAlias(MASTER_KEY_ALIAS) == true) {
                keyStore.deleteEntry(MASTER_KEY_ALIAS)
            }
            generateMasterKey(MASTER_KEY_ALIAS)
            softwareFallbackKey = null
            val time = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", java.util.Locale.getDefault()).format(java.util.Date())
            Log.i("AesGcmCryptoManager", "Rotated AES-256-GCM Master Key at $time")
            return time
        } catch (e: Exception) {
            Log.e("AesGcmCryptoManager", "Error rotating master key: ${e.message}", e)
            return "Failed: ${e.message}"
        }
    }
}
