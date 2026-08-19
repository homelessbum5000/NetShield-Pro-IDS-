# From Mockup to Security Product — NetShield Pro Build Roadmap

**Goal:** Turn the existing UI prototype into a genuine, shippable Android network-security product.
**Date:** 2026-08-18

---

## 1. The honest reframe

Your current app claims "quantum-safe IDS / NGFW / DPI / dual-LLM threat analysis" but performs **no real traffic interception**. Step one is to stop overclaiming and pick a scope you can actually deliver. On a non-rooted Android device, the only legitimate way to see and control network traffic is the platform **`VpnService`** — the same mechanism NetGuard, RethinkDNS, DNS66, and AdGuard use. Nothing ever has to leave the phone: the app opens a virtual network interface, Android routes traffic into it, and your code decides per-connection whether to forward, drop, or sinkhole it ([Android Developers — VPN](https://developer.android.com/develop/connectivity/vpn), [Fulldive — How No-Root Android Firewalls Work](https://www.fulldive.com/post/how-no-root-android-firewalls-work/)).

**Realistic v1 positioning:** an on-device network firewall, DNS threat blocker, and privacy-preserving traffic monitor. Call it "blocks known malicious domains and network destinations" — not "IDS/IPS" — until you genuinely inspect and enforce on real packets.

---

## 2. The Android constraints you must design around

| Constraint | Implication |
|---|---|
| Only **one `VpnService` active per user/profile** at a time ([Bayton — Multiple VPN connections](https://bayton.org/android/android-enterprise-faq/multiple-vpn-connections/)) | Your app conflicts with the user's real VPN client. You must support a "VPN mode" (proxy traffic to a remote server) **and** a "firewall-only mode" (local sinkhole, no remote tunnel). NetGuard does exactly this ([NetGuard FAQ](https://github.com/M66B/NETGuard/blob/master/FAQ.md)). |
| `VpnService` only routes **TCP, UDP, and ICMP ping**; other protocols are dropped ([NetGuard FAQ](https://github.com/M66B/NETGuard/blob/master/FAQ.md)) | You cannot inspect arbitrary L3 protocols. Build for TCP/UDP/DNS only. |
| Only **outbound** connections are intercepted ([NetGuard FAQ](https://github.com/M66B/NETGuard/blob/master/FAQ.md)) | You are a forward firewall for app-initiated traffic, not a server-side IPS. |
| **TLS payloads are encrypted**; SNI is visible only when ECH is absent; **QUIC/HTTP-3** over UDP complicates filtering ([GitHub discussion — always-on content filter](https://github.com/orgs/community/discussions/171226)) | Do not promise "deep packet inspection" of HTTPS payloads. Inspect metadata (SNI, IPs, ports, flow timing), not cleartext bodies. |
| Per-app routing via `VpnService.Builder.addAllowedApplication()` / `addDisallowedApplication()` — **mutually exclusive** allowlist vs blocklist ([Bayton — Global VPN support](https://bayton.org/android/android-enterprise-faq/global-vpn-support/)) | Per-app firewall is real and supported; design your rule model around it. |
| App attribution: `ConnectivityManager.getConnectionOwnerUid()` on Android 10+; parse `/proc/net/*` on older (blocked on 10+) ([Stack Overflow](https://stackoverflow.com/questions/49699096/is-there-a-way-to-see-which-app-sent-data-to-android-vpn-service)) | You can map a flow to an app UID → package name for per-app rules and logs. |

---

## 3. Phased roadmap

### Phase 0 — Stop the bleeding (1–2 weeks)
Fix the dangerous/misleading code before adding anything:
- `RoomAesGcmCryptoManager.encrypt()` **fails open to plaintext** on error — make it fail closed (throw / write nothing) and surface the failure.
- `rotateMasterKey()` deletes the old key **without re-encrypting existing rows** — re-encrypt all rows under the new key first, then rotate.
- Set `android:allowBackup="false"` in the manifest.
- Remove hardcoded `Bearer quantum_token` / `gateway_token` defaults from `ApiServices.kt`.
- Move all mock code (`MockNetworkSimulationInterceptor`, simulated scans, `simulateIntrusionCaptureBurst`) into a **debug build flavor** only; production builds must contain zero mock data.
- Strip or relabel every "quantum / NGFW / DPI / dual-LLM" string in UI — these are now unimplemented claims.

### Phase 1 — The real core: a DNS firewall (4–8 weeks)
This is your MVP. It is enforceable, honest, and genuinely useful.
1. Implement a real `VpnService` with a TUN interface (see [TunMode](https://github.com/gxosty/TunMode) and [NetGuard](https://github.com/M66B/NETGuard) as references).
2. Build a **local DNS resolver** inside the VPN: intercept UDP/53 (and 853/DoT, 443/DoH) queries before they leave the device.
3. Implement a real **`RuleEngine`** (separate from UI) with:
   - Domain blocklists: exact, suffix, wildcard matching.
   - IP/CIDR blocklists.
   - Allowlist overrides.
   - Per-app policy via `addAllowedApplication()` / `addDisallowedApplication()`.
4. **Source real threat feeds** (e.g. curated hosts lists, [RethinkDNS](https://github.com/celzero/rethink-app) blocklist packs) and store them locally, versioned and **signature-verified** on update.
5. Log **real** allowed/blocked DNS events (domain, app UID, timestamp, decision) into your existing Room store — reusing your AES-256-GCM field encryption, now hardened.
6. Replace `LocalHeuristicEngine`'s substring matching with the real `RuleEngine`; delete `loadedSignaturesCount = 1248` and report the **actual** rule count.

### Phase 2 — Real, honest detection (4–8 weeks, after Phase 1)
Once you see real flows, add detections that are truthful:
- **Malicious domain/IP blocked** (blocklist hit) — your core alert.
- **Suspicious domain patterns**: newly-registered domains, cheap-TLD spikes, Punycode/homograph domains.
- **DNS-tunneling heuristics**: long labels, high subdomain entropy, excessive query rate per app — your existing Shannon-entropy code finally has a real input.
- **C2 beaconing** from flow metadata: periodic, low-volume, fixed-destination connections from a single app.
- **Connection-rate / SYN-rate anomalies per app** (volumetric), as a flow-stat signal — not the fake "50k pps" string.

Do **not** claim payload inspection. ECH, QUIC, and cert pinning make HTTPS content inspection unreliable or impossible without MITM proxies the user must opt into and which break many apps ([GitHub — always-on content filter](https://github.com/orgs/community/discussions/171226)).

### Phase 3 — Backend & threat intelligence (ongoing)
- A small backend service for: threat-feed ingestion, rule normalization/scoring, **signed delta updates** pushed to devices, and optional **opt-in** telemetry.
- **Never upload raw packet payloads by default.** Send only anonymized metadata (blocked domain counts, app categories) and require explicit consent.
- Keep all allow/block decisions **local and deterministic**; the backend updates rules, it does not decide per-packet.

### Phase 4 — AI done honestly (after Phase 1–2)
Reframe the "dual-LLM scan" from a fake blocking engine into a legitimate **explainability layer**:
- Explain to the user, in plain language, why a domain was blocked and what the threat category means.
- Generate a weekly security digest summarizing real activity.
- Natural-language search over the local threat log ("what did app X connect to this week?").
- Optional cloud LLM for these summaries — sending **only** the minimal metadata the user explicitly approves.

Never claim "an LLM inspected your packets and blocked a zero-day" unless that is literally true. Your report identified this as the single most misleading element of the current app.

---

## 4. Reuse what's already good

Your existing codebase is not wasted — three pieces are genuinely production-grade and should be the foundation:
- **`RoomAesGcmCryptoManager`** → becomes the at-rest encryption for the real threat log and rule cache (after the Phase 0 hardening).
- **`ExponentialBackoffInterceptor`** → the resilience layer for your backend rule-update API (pointed at a real `https://` endpoint, not `http://.onion`).
- **The Compose UI / 43 cards** → keep the design system and dashboard shell, but wire each card to real `StateFlow` data from the `RuleEngine` and traffic logger instead of `Random` values.

---

## 5. Compliance, trust & go-to-market

Before shipping anything you claim is "security," verify against the primary sources:
- **Google Play VpnService policy**: only apps where VPN/firewall is core functionality may use `VpnService`; **prominent disclosure and consent** are required for any traffic handling, and you cannot collect sensitive data or redirect traffic for monetization ([Google Play — VpnService policy](https://support.google.com/googleplay/android-developer/answer/12564964?hl=en)). Your app qualifies as a "device security app / firewall" — an allowed category — but the disclosure rules are strict.
- **Data Safety form** must accurately reflect what you collect (ideally: minimal/no network data leaves the device).
- **Foreground service justification**: you already use `foregroundServiceType="specialUse"` for the VPN — this is correct, but the declared `FOREGROUND_SERVICE_DATA_SYNC` permission is inconsistent with a `specialUse` service; align these.
- **Privacy policy** covering the opt-in telemetry path.
- Consider **open-sourcing** the detection core (as NetGuard, RethinkDNS, and DNS66 do) — verifiable security code is a major trust advantage in this category.

---

## 6. Recommended v1 scope (the minimum credible product)

Three enforceable, honest features:
1. **Real `VpnService` DNS firewall** — intercept DNS, apply domain/IP blocklists, sinkhole or allow.
2. **Signed blocklist updates** from your backend, verified on-device.
3. **Honest logs** of real allowed/blocked DNS events per app, encrypted at rest.

Ship that, and NetShield Pro stops being a mockup and becomes a legitimate mobile security utility — with a credible path to add detection depth over time.

---

## References

- [Android Developers — VPN (VpnService)](https://developer.android.com/develop/connectivity/vpn)
- [Google Play — VpnService policy](https://support.google.com/googleplay/android-developer/answer/12564964?hl=en)
- [NetGuard FAQ (open-source no-root firewall)](https://github.com/M66B/NETGuard/blob/master/FAQ.md)
- [RethinkDNS (open-source DNS firewall + monitor)](https://github.com/celzero/rethink-app)
- [Fulldive — How No-Root Android Firewalls Work](https://www.fulldive.com/post/how-no-root-android-firewalls-work/)
- [Bayton — Multiple VPN connections on Android](https://bayton.org/android/android-enterprise-faq/multiple-vpn-connections/)
- [Bayton — Per-app VPN / Global VPN support](https://bayton.org/android/android-enterprise-faq/global-vpn-support/)
- [GitHub discussion — Always-on VPN content filter limitations](https://github.com/orgs/community/discussions/171226)
- [Stack Overflow — App attribution via getConnectionOwnerUid](https://stackoverflow.com/questions/49699096/is-there-a-way-to-see-which-app-sent-data-to-android-vpn-service)
- [TunMode — VpnService packet interceptor reference](https://github.com/gxosty/TunMode)
