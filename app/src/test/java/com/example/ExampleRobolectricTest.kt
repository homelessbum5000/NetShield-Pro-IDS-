package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RoomAesGcmCryptoManager
import com.example.data.ThreatLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NetShield Pro", appName)
  }

  @Test
  fun `aes256 gcm encryption and decryption test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val cryptoManager = RoomAesGcmCryptoManager.getInstance(context)

    val sampleIp = "185.220.101.5"
    val encrypted = cryptoManager.encrypt(sampleIp)

    assertTrue(encrypted.startsWith("ENC:GCM256:"))
    assertNotEquals(sampleIp, encrypted)

    val decrypted = cryptoManager.decrypt(encrypted)
    assertEquals(sampleIp, decrypted)
  }

  @Test
  fun `aes256 gcm threat entity encryption at rest`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val cryptoManager = RoomAesGcmCryptoManager.getInstance(context)

    val rawLog = ThreatLogEntity(
      id = 1L,
      timestampMs = 1700000000L,
      timestampIso = "2026-08-14T07:25:00Z",
      severity = "CRITICAL",
      attackVector = "SYN-Flood Volumetric Burst",
      sourceIp = "198.51.100.42",
      targetPort = 443,
      actionTaken = "PACKET_DROPPED",
      quantumKeyId = "KYBER1024_0x00A1",
      details = "High density SYN flood detected on post-quantum TLS portal."
    )

    val encryptedLog = cryptoManager.encryptThreatLog(rawLog)
    assertTrue(encryptedLog.sourceIp.startsWith("ENC:GCM256:"))
    assertTrue(encryptedLog.attackVector.startsWith("ENC:GCM256:"))
    assertTrue(encryptedLog.quantumKeyId.startsWith("ENC:GCM256:"))

    val decryptedLog = cryptoManager.decryptThreatLog(encryptedLog)
    assertEquals(rawLog.sourceIp, decryptedLog.sourceIp)
    assertEquals(rawLog.attackVector, decryptedLog.attackVector)
    assertEquals(rawLog.details, decryptedLog.details)
  }

  @Test
  fun `aes256 gcm benchmark and tamper verification test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val cryptoManager = RoomAesGcmCryptoManager.getInstance(context)

    val benchmark = cryptoManager.runCryptographicBenchmark("test payload 123")
    assertTrue(benchmark.isDecryptionVerified)
    assertTrue(benchmark.isTamperProofVerified)
  }

  @Test
  fun `quantum tunnel status manager notification building and toggle test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.network.QuantumTunnelStatusManager.init(context)
    com.example.network.QuantumTunnelStatusManager.setProtectionEnabled(context, true)

    val stateInitial = com.example.network.QuantumTunnelStatusManager.state.value
    assertTrue(stateInitial.isProtectionEnabled)

    val notification = com.example.network.QuantumTunnelStatusManager.buildNotification(context)
    assertEquals(com.example.network.QuantumTunnelStatusManager.NOTIFICATION_CHANNEL_ID, notification.channelId)

    com.example.network.QuantumTunnelStatusManager.toggleProtection(context)
    val stateAfterToggle = com.example.network.QuantumTunnelStatusManager.state.value
    assertEquals(false, stateAfterToggle.isProtectionEnabled)
    assertEquals("DISCONNECTED", stateAfterToggle.healthStatus)

    com.example.network.QuantumTunnelStatusManager.setProtectionEnabled(context, true)
    assertTrue(com.example.network.QuantumTunnelStatusManager.state.value.isProtectionEnabled)
  }

  @Test
  fun `quantum tunnel action receiver toggle intent test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.network.QuantumTunnelStatusManager.setProtectionEnabled(context, true)
    val receiver = com.example.network.QuantumTunnelActionReceiver()

    val toggleIntent = android.content.Intent().apply {
      action = com.example.network.QuantumTunnelStatusManager.ACTION_TOGGLE_PROTECTION
    }
    val prevStatus = com.example.network.QuantumTunnelStatusManager.state.value.isProtectionEnabled

    receiver.onReceive(context, toggleIntent)
    assertEquals(!prevStatus, com.example.network.QuantumTunnelStatusManager.state.value.isProtectionEnabled)
  }

  @Test
  fun `ngfw core id and wildfire sample models test`() {
    val appId = com.example.ui.AppIdSignature(
      name = "BitTorrent-P2P",
      category = "File-Sharing",
      riskLevel = 5,
      standardPort = "Dynamic",
      behavioralPattern = "BitTorrent DHT",
      isBlocked = true,
      bandwidthMb = 412.5f,
      sessionsCount = 84
    )
    assertTrue(appId.isBlocked)
    assertEquals(5, appId.riskLevel)

    val sample = com.example.ui.WildFireSample(
      fileHash = "7e29af881",
      fileName = "invoice.pdf.exe",
      fileType = "PE32",
      detonationStatus = "Malicious Zero-Day",
      cveRef = "CVE-2026-9921",
      timestamp = "Just now",
      sandboxConfidence = 99.8f
    )
    assertEquals("Malicious Zero-Day", sample.detonationStatus)
    assertTrue(sample.sandboxConfidence > 99.0f)
  }

  @Test
  fun `ai traffic analysis models toggle and specs test`() {
    val modelA = com.example.ui.TrafficAnalysisAiModel.ON_DEVICE_NPU
    val modelB = com.example.ui.TrafficAnalysisAiModel.CLOUD_GEMINI_DEEPSHIELD
    val dualHybrid = com.example.ui.TrafficAnalysisAiModel.DUAL_CONSENSUS

    assertEquals("Model A", modelA.modelNumber)
    assertEquals("EdgeShield NPU", modelA.displayName)
    assertEquals("ON-DEVICE", modelA.tag)
    assertTrue(modelA.latencyMs < 10)

    assertEquals("Model B", modelB.modelNumber)
    assertEquals("DeepShield Cloud", modelB.displayName)
    assertEquals("CLOUD AI", modelB.tag)
    assertTrue(modelB.accuracyPct > 99.0f)

    assertEquals("Hybrid", dualHybrid.modelNumber)
    assertEquals(99.9f, dualHybrid.accuracyPct)
  }

  @Test
  fun `local heuristic engine offline analysis and entropy calculation test`() {
    val engine = com.example.network.LocalHeuristicEngine.getInstance()
    
    // Test Shannon Entropy
    val benignData = "AAAAAA"
    val highEntropyData = "q8!z#9xL@2mP\$7kQ"
    val benignEntropy = engine.calculateShannonEntropy(benignData)
    val highEntropy = engine.calculateShannonEntropy(highEntropyData)
    assertTrue(highEntropy > benignEntropy)

    // Test Benign LAN
    val benignVerdict = engine.inspectTrafficLocally("192.168.1.50")
    assertFalse(benignVerdict.isThreat)
    assertEquals("RULE_LOCAL_RFC1918_PASSTHROUGH", benignVerdict.heuristicRuleMatched)

    // Test Threat Detection (SYN Flood)
    val threatVerdict = engine.inspectTrafficLocally("185.220.101.5")
    assertTrue(threatVerdict.isThreat)
    assertTrue(threatVerdict.eBpfRule.startsWith("eBPF_LOCAL_DROP_SRC"))

    // Test Health Query
    val health = engine.getEngineHealth()
    assertEquals(1248, health["ruleCount"])
    assertEquals(true, health["entropyReady"])
  }

  @Test
  fun `engine operational health state model test`() {
    val healthState = com.example.ui.EngineOperationalHealthState(
      primaryCloudLlmStatus = com.example.ui.EngineStatus.OPTIMAL,
      edgeNpuStatus = com.example.ui.EngineStatus.OPTIMAL,
      dualConsensusStatus = com.example.ui.EngineStatus.OPTIMAL,
      localHeuristicStatus = com.example.ui.EngineStatus.ARMED_STANDBY,
      overallHealthScore = 98
    )

    assertEquals(98, healthState.overallHealthScore)
    assertEquals(com.example.ui.EngineStatus.OPTIMAL, healthState.primaryCloudLlmStatus)
    assertEquals(com.example.ui.EngineStatus.ARMED_STANDBY, healthState.localHeuristicStatus)
    assertEquals("Optimal", com.example.ui.EngineStatus.OPTIMAL.displayName)
    assertEquals("Active Fallback", com.example.ui.EngineStatus.ACTIVE_FALLBACK.displayName)
  }
}

