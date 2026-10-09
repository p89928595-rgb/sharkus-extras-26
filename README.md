# Sharkus Extras (Fabric, Minecraft 26.2)

Menü öffnen: **Rechte Shift-Taste** (in den Steuerungs-Einstellungen änderbar).
Features (alle einzeln an/aus): AppleSkin, Health Indicator, Armor Status,
Team Tracker, Wortfilter, Hit Color, Effekt-Timer.

## Jar ohne Installation bauen (GitHub)
1. Kostenloses Konto auf github.com, neues Repository erstellen.
2. Den entpackten Inhalt dieses Ordners hochladen (inkl. Ordner `.github`).
3. Reiter "Actions" -> "Build Mod" -> nach ca. 3-5 Minuten unter "Artifacts" `sharkus-extras-jar` herunterladen.
4. Die entpackte `.jar` in den `mods`-Ordner legen (Fabric Loader 0.19.3+ und Fabric API für 26.2 nötig).

## Selbst bauen
Java 25 und Gradle 9.5.1 installieren, dann `gradle build`.
Ergebnis: `build/libs/sharkus-extras-1.0.0.jar`
