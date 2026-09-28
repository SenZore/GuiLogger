# LogRecord

LogRecord saves what Minecraft shows in menus as JSON. I made it for checking server shops, where item names, lore, and prices are often tucked into tooltips. It runs on your client, so the server does not need a matching mod.

## Install

This release is for Minecraft Java 26.2 and Java 25. Install Fabric Loader and Fabric API for 26.2, then copy [`logrecord-1.0.0+26.2.jar`](release/logrecord-1.0.0+26.2.jar) into your profile's `mods` folder.

## Record a shop

1. Join the server. Enter `/logrecord start`, or open an inventory or shop menu and click **Record**.
2. Open `/shop` and visit the pages you want to inspect. LogRecord captures each page as it appears on your screen.
3. Enter `/logrecord stop`, or click **Stop** in the open menu.

When you stop, the save location appears in chat. Click the path to copy it; `/logrecord status` shows it again. The `/logrecord` commands stay on your client and are blocked from being sent to the server. `/shop` is still handled by the server.

Recordings are saved under `logs/logrecord/<date>_<id>/`. The main file is `recording.json`. Each event is also written as a separate JSON file in `events/`, so captured data remains available if the game closes before recording is stopped.

## Recorded data

The JSON records menu titles, slot positions, item IDs and counts, item components, tooltips, and price text shown with an item. It also saves incoming chat and text drawn by Minecraft's GUI. Repeated, unchanged menu snapshots are left out.

It can only save information your client has received. You need to open each shop page yourself, and graphics without readable text cannot be represented in JSON. Prices are stored as text; LogRecord does not guess the currency or calculate a number. Recordings may include private chat or server details, so check the JSON before sharing it.

## Build

Build with Java 25. On Windows, run `gradlew.bat build`; on macOS or Linux, run `./gradlew build`. Gradle puts the mod jar in `build/libs/`.

The project uses Fabric Loom 1.17.21, Fabric Loader 0.19.5, and Fabric API 0.160.0 for Minecraft 26.2.

## License

LogRecord is distributed under the MIT License. See [LICENSE](LICENSE).
