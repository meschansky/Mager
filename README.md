# ArmoredAge (Android/Kotlin)

Mobile app for **encrypting/decrypting ASCII-armored AGE messages** with local key management.

## Features

- Generates and stores AGE identities on-device (encrypted preferences).
- Stores known recipient public keys for encryption.
- Encrypts plaintext to armored AGE payloads.
- Decrypts only armored AGE payloads.
- GitHub Actions CI builds a signed release APK artifact from version tags.

## Notes

- Crypto wiring is implemented through a small reflection bridge over `kage` to reduce breakage when API signatures change across versions.
- The app intentionally rejects non-armored payloads for decryption.

## Build locally

```bash
make debug
make release
```

## CI APK

Workflow: `.github/workflows/android-apk.yml`

Tagged builds use the Git tag to set app version metadata. For example, `v1.0.5` becomes:

- `versionName = 1.0.5`
- `versionCode = 1000005`
- release asset `armored-age-v1.0.5.apk`

To allow in-place upgrades on Android, CI must use one persistent signing key. Configure these GitHub Actions secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

`ANDROID_KEYSTORE_BASE64` should contain the base64-encoded bytes of your release keystore. Every tagged release must use that same keystore, otherwise Android will reject upgrades over an installed build.
