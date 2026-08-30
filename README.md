# <img src="art/logo_android.png" alt="" width="28"> Blabber

This repository is a privacy-focused fork of [kriztan/blabber.im](https://codeberg.org/kriztan/blabber.im), an Android XMPP client based on [Conversations](https://github.com/siacs/Conversations).

## Fork changes

- Android application ID: `de.xmpp.blabber`
- Minimum supported Android version: Android 6.0 (API 23)
- New-account registration is limited to `xmpp.de`
- Existing XMPP accounts from any server can be added and used
- Location sharing, map display, location permissions, and automatic map-preview requests are removed
- Sentry error reporting is optional and disabled by default
- No Sentry DSN is built into the app; users may enter their own HTTPS DSN in Settings
- Legacy provider-specific service references are removed

## Build

The Git debug variant can be built with:

```bash
./gradlew clean assembleGitDebug lintGitDebug
```

The APK is written below `build/outputs/apk/git/debug/`.

## Privacy

The app does not request Android location permissions and has no UI for sending or opening locations. A received `geo:` URI is treated as ordinary message text.

Sentry remains inactive until the user explicitly saves a valid HTTPS DSN in Settings. Clearing the field disables it again. The app does not ship with a predefined DSN.

## Source and issues

- Fork repository: [github.com/ecxod/blabber](https://github.com/ecxod/blabber)
- Issue tracker: [github.com/ecxod/blabber/issues](https://github.com/ecxod/blabber/issues)
- Upstream project: [codeberg.org/kriztan/blabber.im](https://codeberg.org/kriztan/blabber.im)
- Technical legacy identifiers: [BRANDING-MIGRATION.md](BRANDING-MIGRATION.md)

## License

See [LICENSE](LICENSE).
