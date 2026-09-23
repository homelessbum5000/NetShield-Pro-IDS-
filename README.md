# NetShield-Pro-IDS

NetShield-Pro-IDS is an Android-based network security and monitoring project focused on defensive network visibility, threat detection, and security diagnostics.

## Features

- Network monitoring and diagnostics
- Security and threat dashboard
- Network telemetry
- Defensive security tooling
- Encrypted application data
- Android foreground-service support
- Kotlin and Jetpack Compose UI

## Requirements

- JDK 21
- Android SDK 36.1
- Android Build Tools 36.1.0

## Build

```bash
./gradlew assembleDebug --no-parallel
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Testing

```bash
./gradlew testDebugUnitTest --no-configuration-cache
```

## Security

See [SECURITY.md](SECURITY.md) for security reporting information. Do not commit private keys, signing keys, passwords, API tokens, or other secrets.

## License

This project is released under the MIT License. See [LICENSE](LICENSE).

## Support

Funding options are available through the repository funding configuration.
