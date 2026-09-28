# Interface notes

LogRecord adds a Record or Stop button to container screens and also accepts local `/logrecord` commands. The button uses Minecraft's normal controls and stays clear of item slots when the screen has room. Its label tells you whether recording is active.

Recordings are written as JSON on the player's computer. When recording stops, the mod prints the file location in chat so the player can find it again. The mod does not add a separate settings screen or custom artwork.
