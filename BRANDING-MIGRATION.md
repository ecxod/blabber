# Blabber branding migration

The user-facing product name is **Blabber**. New UI text, store metadata, release artifacts, and documentation must not use `blabber.im` as the product name.

Some technical references are intentionally retained temporarily because changing them without migration support would break existing installations:

- legacy backup and media paths such as `Documents/blabber.im/Database`
- existing web, invite, update, and migration URLs on the `blabber.im` domain
- the legacy XMPP capabilities node and Android deep-link host
- migration code that discovers files created by earlier app versions

Future cleanup must introduce replacement paths and endpoints first, migrate existing data once, and retain read-only compatibility with the old identifiers for upgrades. Historical changelog entries and upstream repository URLs remain historical or technical references, not product branding.
