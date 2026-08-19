# NetShield Pro IDS — Static Analysis Report

**Repository:** `homelessbum5000/NetShield-Pro-IDS-`
**Analysis date:** 2026-08-18
**Analyst:** Perplexity Computer (automated static review)
**Method:** Clone + manual source review of all Kotlin modules, manifests, and Gradle configuration.

---

## 1. Executive Summary

NetShield Pro is an **Android mobile application** (Kotlin + Jetpack Compose) that presents itself as a "quantum-safe mobile network security and intrusion detection" product. It appears to be an AI Studio project (application id `com.aistudio.quantumfirewall.a67l`, package `com.example`, AI Studio metadata in `metadata.json`) and consists of **66 Kotlin source files / ~32,500 lines**, the large majority of which (43 files) are Compose UI "dashboard cards."

The codebase is a **feature-rich UI demo / prototype**, not a functional intrusion-detection system. Key conclusions:

- There is **no actual packet capture, traffic interception, or VPN tunnel** anywhere in the code. No `VpnService`, no raw sockets, no `pcap`, no `BIND_VPN_SERVICE` permission. All "captured" traffic is **synthetically generated** by view models.
- The only genuinely implemented security primitive is **AES-256-GCM field-level encryption at rest** (`RoomAesGcmCryptoManager`) backed by the AndroidKeyStore — this is real, correct, and tamper-resistant.
- A second genuinely implemented module is an **exponential-backoff OkHttp retry interceptor** with jitter — sound engineering, though the endpoints it targets are fictitious (`.onion` URLs).
- The "Local Heuristic Engine" performs **substring matching against hardcoded IP fragments and keywords** plus a Shannon-entropy calculation on a string argument; it does **not** inspect real network traffic.
- The headline "dual-LLM cloud threat analysis" is **fully simulated** — it picks a random IP, a random attack vector, and a random confidence score, then writes fabricated reasoning text. No Gemini/AI SDK call occurs on the detection path (a Gemini call appears to exist only in the separate "LLM Auto-Debugger" card, wrapped in try/catch with a hardcoded fallback string).

In short: the project is an elaborate mockup of an IDS rather than a working IDS.

---

## 2. Project Structure

```
NetShield-Pro-IDS-/
├── metadata.json                  # AI Studio app metadata
├── .env.example                    # Gemini API key placeholder
├── build.gradle.kts               # Root Gradle config
├── settings.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml      # Version catalog (dependency catalog)
├── gradlew / gradlew.bat          # Gradle wrapper
└── app/
    ├── build.gradle.kts            # Android app module config (deps, signing)
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/example/
        │   │   ├── MainActivity.kt                # 1,418 LOC — Compose entry, assembles all cards
        │   │   ├── network/                       # 10 files — networking & "detection"
        │   │   ├── data/                          # 9 files — Room DB, DAOs, entities, crypto
        │   │   └── ui/                            # 43 files — Compose dashboard cards + 2 view models
        │   └── res/                               # Icons, themes, strings
        ├── androidTest/                            # Instrumented test stub
        └── test/                                  # Unit tests (Robolectric / Roborazzi screenshot tests)
```

### Module breakdown

| Package | Files | LOC | Role |
|---|---|---|---|
| `com.example` (root) | 1 | 1,418 | `MainActivity` — single-activity Compose host rendering all dashboard cards |
| `com.example.network` | 10 | ~1,027 | Networking: Retrofit clients, OkHttp interceptors, mock simulator, "heuristic engine," notification/tunnel status |
| `com.example.data` | 9 | ~880 | Room database, DAOs, entities, AES-GCM crypto manager, repositories |
| `com.example.ui` | 43 | ~29,200 | Compose UI cards (43 of them) + `NetworkViewModel` (2,367 LOC) and `NetworkTrafficLogViewModel` (240 LOC) |

The UI layer dominates the codebase by volume. Card names are illustrative of intent rather than capability, e.g. `NgfwEnterpriseCommandCenterCard`, `QuantumCryptoPanelCard`, `DpuAcceleratedSecurityCard`, `DpiProtocolFilterCard`, `PerAppFirewallCard`, `GeoThreatHeatMapCard`.

---

## 3. Main Modules and Responsibilities

### 3.1 `network/LocalHeuristicEngine.kt` — "On-device threat detection" (244 LOC)
Singleton exposing `inspectTrafficLocally(target: String, customPayload: String): LocalHeuristicVerdict`. This is the closest thing to a detection engine. It:
1. Computes **Shannon entropy** of the `target`/`payload` string.
2. Runs a series of boolean checks using **`String.contains()` against hardcoded fragments**: `192.168.`, `10.`, `185.220.`, `45.154.`, `103.21.244`, `109.236.81`, `198.51.100`, plus keywords `KYBER`, `Harvest`, `SYN`, `Flood`, `Reset`, `HTTP/2`.
3. Maps the first matching branch to a verdict (attack vector, severity, confidence, "eBPF rule" string).

Limitations: operates on a **caller-supplied string**, not on captured packets; the "1,248 loaded signatures" (`loadedSignaturesCount`) is a **hardcoded literal**, not a loaded ruleset; the `eBPF_LOCAL_DROP_SRC ...` output is a display string, never executed.

### 3.2 `network/RetrofitClient.kt` + `ApiServices.kt` + `ApiModels.kt` — Cloud API layer
Defines two Retrofit services:
- `QuantumApiService` — `quantum/health`, `quantum/random`, `quantum/keygen`, `quantum/anomaly`.
- `GatewayApiService` — `gateway/health`, `gateway/ingest`.

Base URLs are **fictitious**: `http://quantum.onion:9000/` and `http://gateway.onion/`. Because these cannot resolve, the `MockNetworkSimulationInterceptor` (below) is installed in front of every client and returns canned JSON for all requests. The API layer is therefore effectively non-functional against any real backend.

### 3.3 `network/MockNetworkSimulationInterceptor.kt` — Demo traffic simulator (106 LOC)
OkHttp interceptor that **short-circuits every outbound request** and returns synthetic responses. Modes include `MOCK_SUCCESS`, `FORCE_503_OVERLOAD`, `FORCE_429_RATE_LIMIT`, `FORCE_NETWORK_OUTAGE`, and `FORCE_FAIL_THEN_SUCCEED`. In `SimulationMode.NONE` it still falls back to a mock success body if a real `IOException` occurs. This confirms the app is built to run entirely offline with fabricated data.

### 3.4 `network/ExponentialBackoffInterceptor.kt` — Retry with jitter (203 LOC) ✅ genuinely implemented
A correct, well-structured retry interceptor: retries on `{429, 500, 502, 503, 504}` and `IOException`, computes `min(maxDelay, initialDelay * mult^(attempt-1))` plus optional ±25% jitter, caps attempts, emits `RetryLogEntry` events to a listener. This is real, production-grade resilience logic — it just targets non-existent servers.

**Interceptor ordering caveat:** in `RetrofitClient`, `mockSimulator` is added **before** the backoff interceptor. Simulation modes that return a response directly (e.g. `MOCK_SUCCESS`, `FORCE_503_OVERLOAD`) therefore bypass the backoff interceptor entirely, so the mocked network path may not exercise the retry logic at all.

### 3.5 `network/QuantumTunnelStatusManager.kt` — Foreground service & status (203 LOC)
Implements a `Service` (`QuantumTunnelNotificationService`) with `foregroundServiceType="specialUse"` and a `BroadcastReceiver` for "toggle protection" / "rotate keys" quick actions. Despite the "post-quantum VPN encryption tunnel" labeling, **no tunneling, encryption, or packet forwarding occurs** — the service only displays a persistent notification. "Rotate PQC keys" toggles a string between `"FIPS-203 ML-KEM-1024"` and `"FIPS-204 ML-DSA-87"`; no cryptographic operation is performed.

### 3.6 `network/ThreatNotificationManager.kt` / `NetworkConnectivityManager.kt`
Threat alerting and connectivity monitoring helpers (131 / 91 LOC).

### 3.7 `data/RoomAesGcmCryptoManager.kt` — AES-256-GCM at rest (381 LOC) ✅ genuinely implemented
The strongest piece of real security code in the repo. The cryptography itself is sound, but it has production-readiness caveats (see below):
- Generates an AES-256 key in the **AndroidKeyStore** (`KeyGenParameterSpec`, GCM/NoPadding, 256-bit), with a software fallback for JVM/test environments.
- `encrypt()` produces `ENC:GCM256:<Base64(IV(12B) ‖ ciphertext ‖ tag(16B))>` using a fresh random 12-byte IV per call and a 128-bit auth tag.
- `decrypt()` validates the tag and returns a tamper error on `AEADBadTagException`.
- `testTamperResistance()` flips a ciphertext byte and asserts the AEAD rejects it — a real self-test.
- `rotateMasterKey()` deletes and regenerates the keystore alias.
- `encryptThreatLog()` / `encryptPacketAnalysis()` apply field-level encryption to sensitive DB columns (`sourceIp`, `attackVector`, `details`, `protocol`, etc.).

Minor caveats: `setRandomizedEncryptionRequired(false)` is used (acceptable because IVs are managed manually via `SecureRandom`, but it places the burden of IV uniqueness on the implementation — which it does satisfy). On failure, `encrypt()` returns the **plaintext** unchanged (logged), so a KeyStore failure silently degrades to cleartext storage. `rotateMasterKey()` deletes and regenerates the keystore alias but does **not** re-encrypt existing DB rows under the new key — previously-encrypted rows would become undecryptable after rotation. The `isHardwareBacked` flag is inferred from alias existence rather than from a hardware attestation check.

### 3.8 `data/` persistence layer
- `NetShieldDatabase.kt` — Room DB v2 (`netshield_database`) with `fallbackToDestructiveMigration()`.
- Entities: `ThreatLogEntity`, `PacketAnalysisEntity`, `NetworkTrafficLogEntity`.
- `ThreatRepository.kt` / `NetworkTrafficLogRepository.kt` — repositories that decrypt on read / encrypt on write via the crypto manager. Correct separation of concerns.

### 3.9 `ui/NetworkViewModel.kt` — The simulation core (2,367 LOC)
The largest non-UI file. Hosts all mock state: firewall rules, DNS sinkhole blocklists, "AI models," hardware telemetry, and the two headline flows:
- `runDualLlmScanAndBlock()` — **simulated** (see §5.1).
- `inspectTrafficLocally()` invocations at lines 682 and 1360 — calls the heuristic engine with a target string chosen from a hardcoded list.
- Generates `BlockedFirewallRule` entries with `kernelRule = "iptables -A INPUT -s $ip -j DROP"` — these are **display strings only**, never executed (no `Runtime.exec` / root; Android cannot run iptables).

### 3.10 `ui/NetworkTrafficLogViewModel.kt` — Synthetic capture (240 LOC)
- `simulateIntrusionCaptureBurst()` generates 4 random `NetworkTrafficLogEntity` rows with payload snippets like `"INT-IDS-CAPTURE #3: Packet payload checked against neural signatures."`
- `captureCustomPacket(...)` inserts a user-supplied row (still not real capture).
- Seeds the DB with default "traffic logs" on first launch.

---

## 4. Dependency Inventory

Source of truth: `gradle/libs.versions.toml` + `app/build.gradle.kts`. AGP 9.1.1, Kotlin 2.2.10, compileSdk 36, minSdk 24, targetSdk 36.

### Core / UI
- AndroidX Core KTX 1.18.0, Activity Compose 1.10.1
- Compose BOM 2024.09.00 (Material3, Material Icons core+extended, UI, UI-graphics, UI-tooling)
- Lifecycle (runtime-ktx / runtime-compose / viewmodel-compose) 2.8.7

### Persistence & Data
- Room (runtime / ktx / compiler) 2.7.0, via KSP
- (DataStore Preferences 1.1.7 — declared but commented out)

### Networking
- Retrofit 2.12.0 + Converter-Moshi 2.12.0
- OkHttp 4.10.0 + Logging Interceptor 4.10.0
- Moshi 1.15.2 (+ codegen) — JSON (de)serialization

### Concurrency
- kotlinx.coroutines (android + core) 1.10.2

### Firebase / AI
- Firebase BOM 34.15.0
- `firebase-ai` (Gemini) — declared; `firebase-appcheck-recaptcha` — declared
- Firebase Auth, Firestore, Credentials, GoogleID — **commented out**
- Gemini API key via `secrets-gradle-plugin` 2.0.1 reading `.env` / `.env.example`

### Other
- ZXing core 3.5.3 (QR scanning), CameraX 1.5.0 (camera2/lifecycle/view/core)
- Accompanist Permissions 0.37.3, Play Services Location 21.3.0 — **commented out**
- LeakCanary 2.14 (debug only)
- Testing: JUnit 4.13.2, Espresso 3.7.0, Robolectric 4.16.1, Roborazzi 1.59.0, kotlinx-coroutines-test 1.10.2

No native IDS/security libraries (no `libpcap`, `scapy`, `nDPI`, `Snort`/`Suricata` rules, `tshark`, or packet-capture bindings). There is no dependency capable of reading live network packets.

---

## 5. Security / IDS Pattern Audit

### 5.1 Signature-based detection — present but degenerate
"Signature" matching exists in `LocalHeuristicEngine.inspectTrafficLocally()`, but it is **substring containment on a string argument**, not pattern matching against packet payloads:
```kotlin
val isKnownBotnetIp = target.contains("185.220.") || target.contains("45.154.") || ...
val isSynFlood = target.contains("185.220.101") || payload.contains("SYN", true) || payload.contains("Flood", true)
val isHttp2Reset = target.contains("45.154.255") || payload.contains("Reset", true) || payload.contains("HTTP/2", true)
```
- No regex engine, no compiled ruleset, no signature file is loaded. `loadedSignaturesCount = 1248` is a hardcoded literal with no backing data.
- The "dual-LLM" path (`runDualLlmScanAndBlock`) is **fully simulated**: it selects `candidateIps.random()`, picks `vectors.random()`, sets `confidence = (91..99).random()/100f`, and writes fabricated reasoning such as `"Primary LLM flagged packet entropy anomaly; Secondary LLM confirmed malicious $chosenVector signature..."`. No Gemini API call occurs on this path (the only Gemini call is in the separate "LLM Auto-Debugger" card, wrapped in try/catch with a hardcoded fallback string).

### 5.2 Traffic analysis logic — simulated, not real
- There is **no live traffic source**. `NetworkTrafficLogViewModel.simulateIntrusionCaptureBurst()` fabricates rows with `Random` IPs/ports/protocols and the literal snippet `"Packet payload checked against neural signatures."`
- Verified by grep: no `VpnService`, `DatagramSocket`, `ServerSocket`, `TrafficStats`, or `Process.exec`/`Runtime.exec` usage exists anywhere in the source (the only `Runtime.getRuntime()` calls are `availableProcessors()` and memory-stat reads in the leak-monitor card). No packet-capture library is present.
- The "Deep Packet Inspection" (`DpiProtocolFilterCard`) and "Per-App Firewall" (`PerAppFirewallCard`) cards are **pure UI state** — they mutate in-memory lists and render toggles; no packet is ever classified or dropped.

### 5.3 What is genuinely implemented and correct
| Component | Status |
|---|---|
| AES-256-GCM field encryption at rest (AndroidKeyStore) | ✅ Real, tamper-tested |
| Exponential backoff + jitter retry interceptor | ✅ Real, sound |
| Room repositories with encrypt-on-write / decrypt-on-read | ✅ Correct |
| OkHttp/Retrofit/Moshi wiring | ✅ Functional plumbing (to mock endpoints) |
| Foreground service + notification channel | ✅ Compliant Android FGS implementation |

### 5.4 Security findings & risks

1. **No actual network protection exists.** Despite "quantum VPN tunnel," "IDS," "NGFW," and "DPI" branding, the app does not intercept, inspect, or block any real network traffic. A user relying on this for protection would have none. The manifest requests no `BIND_VPN_SERVICE`; no `VpnService` is declared.

2. **Hardcoded bearer tokens as default parameters.** `QuantumApiService`/`GatewayApiService` default `authHeader = "Bearer quantum_token"` / `"Bearer gateway_token"`. Even though endpoints are fictional, embedding default credentials in interface signatures is a bad pattern and would ship real secrets if endpoints were real.

3. **Plaintext HTTP base URLs.** `http://quantum.onion:9000/` and `http://gateway.onion/` use `http://`, not `https://`. No TLS. (Moot for mock endpoints, but the pattern must not be copied to real services.)

4. **API key handling.** `.env.example` contains `GEMINI_API_KEY=MY_GEMINI_API_KEY`. The Secrets Gradle Plugin injects `.env` values at build; if a real key were uncommented in `.env` it would be packaged into the APK (BuildConfig). No obfuscation (`isMinifyEnabled = false`), so secrets would be trivially extractable. `.gitignore` should ensure `.env` is not committed — worth confirming.

5. **Silent fallback to plaintext.** `RoomAesGcmCryptoManager.encrypt()` returns the plaintext unchanged if encryption throws (only logs). A KeyStore failure thus degrades to storing sensitive fields in cleartext SQLite without surfacing the failure to the user. Relatedly, `rotateMasterKey()` deletes the old key without re-encrypting existing rows, which would orphan already-encrypted data.

6. **`fallbackToDestructiveMigration()`** on the Room database drops and recreates tables on schema change — acceptable for a prototype, risky for production threat-log retention.

7. **`allowBackup="true"`** in the manifest. Although `data_extraction_rules.xml`/`backup_rules.xml` are referenced, full backup being enabled means the encrypted DB (and the app's data) could be exfiltrated via adb backup on debuggable builds. For a security product, `allowBackup="false"` is the safer default.

8. **`setRandomizedEncryptionRequired(false)`** — acceptable given manual random IVs, but it disables a platform safety net. IV uniqueness is currently satisfied via `SecureRandom.nextBytes(12)`, which is fine.

9. **"Firewall" rules are cosmetic.** `iptables -A INPUT -s $ip -j DROP` strings are generated for display only; they are never executed and could not be on a non-rooted device. This may mislead users into believing traffic is being blocked.

10. **Overbroad permissions relative to function.** `CAMERA`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS` are requested. CAMERA is used only by the QR gateway scanner card; the foreground service is a status notification, not data sync — the `FOREGROUND_SERVICE_DATA_SYNC` permission is declared but the service type is `specialUse`, which is inconsistent.

11. **Test coverage is minimal.** Only `ExampleUnitTest`, `ExampleRobolectricTest`, and a Roborazzi screenshot test exist. No tests cover the heuristic engine, crypto manager, or backoff interceptor — the components that most warrant testing.

---

## 6. Gaps & Recommended Next Review Steps

1. **Decide intent.** If this is a demo/prototype, label it as such in-app and in the README (none exists). If intended as a real IDS, the entire detection path must be rebuilt on an actual traffic source.
2. **Add a real packet source** — on Android this means a `VpnService` (with `BIND_VPN_SERVICE`) or a per-app network policy via `NetworkPolicyManager`/`ConnectivityManager` callbacks. Without this, no IDS claim is supportable.
3. **Replace substring "signatures" with a real rules engine** — load a versioned ruleset, compile patterns (regex or Aho-Corasick), and match against actual payloads/flows.
4. **Wire the dual-LLM path to the real Gemini SDK** (`firebase-ai`) or remove the simulated reasoning to avoid misleading auditability.
5. **Harden crypto**: fail closed (not plaintext) on `encrypt()` failure; set `allowBackup="false"`; switch base URLs to `https://`; remove default bearer-token parameters.
6. **Add unit tests** for `LocalHeuristicEngine.calculateShannonEntropy()`, `RoomAesGcmCryptoManager` round-trip + tamper, and `ExponentialBackoffInterceptor.calculateDelay()` / retry behavior.
7. **Confirm `.env` is git-ignored** and that no real API key is committed in history.

---

## 7. Verdict

NetShield Pro is a polished, AI-Studio-generated **mockup** of a quantum-safe mobile IDS. Its genuine engineering value is concentrated in two areas — AES-256-GCM database encryption and an exponential-backoff HTTP interceptor — both of which are correctly implemented. Everything branded as "detection," "traffic analysis," "DPI," "NGFW," or "quantum VPN tunnel" is **UI state and synthetic data**: no packets are captured, no signatures are loaded, no traffic is blocked, and no LLM is consulted on the detection path. Treat the repository as a UI/UX prototype, not a security product.
