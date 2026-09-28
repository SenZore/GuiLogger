package dev.senzore.logrecord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class JsonRecording {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final DateTimeFormatter NAME = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss")
            .withZone(ZoneOffset.UTC);
    private final Path directory;
    private final JsonObject metadata;
    private long sequence;
    private boolean closed;

    public JsonRecording(Path root, JsonObject metadata) throws IOException {
        Files.createDirectories(root);
        directory = Files.createTempDirectory(root, NAME.format(Instant.now()) + "_");
        Files.createDirectory(directory.resolve("events"));
        this.metadata = metadata.deepCopy();
        this.metadata.addProperty("schema_version", 1);
        this.metadata.addProperty("started_at", Instant.now().toString());
        writeAtomic(directory.resolve("session.json"), GSON.toJson(this.metadata));
    }

    public Path directory() { return directory; }
    public long count() { return sequence; }

    public void append(String type, JsonObject data) throws IOException {
        if (closed) throw new IOException("This recording has already stopped.");
        JsonObject event = new JsonObject();
        event.addProperty("sequence", sequence + 1);
        event.addProperty("time", Instant.now().toString());
        event.addProperty("type", type);
        event.add("data", data.deepCopy());
        writeAtomic(eventPath(sequence + 1), GSON.toJson(event));
        sequence++;
    }

    public Path finish(String reason) throws IOException {
        if (closed) throw new IOException("This recording has already stopped.");
        metadata.addProperty("stopped_at", Instant.now().toString());
        metadata.addProperty("stop_reason", reason);
        metadata.addProperty("event_count", sequence);
        Path pending = directory.resolve("recording.json.tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(pending, StandardCharsets.UTF_8)) {
            writer.write("{\"session\":");
            GSON.toJson(metadata, writer);
            writer.write(",\"events\":[\n");
            for (long i = 1; i <= sequence; i++) {
                if (i > 1) writer.write(",\n");
                try (var reader = Files.newBufferedReader(eventPath(i), StandardCharsets.UTF_8)) {
                    reader.transferTo(writer);
                }
            }
            writer.write("\n]}\n");
        }
        Path output = directory.resolve("recording.json");
        Files.move(pending, output, StandardCopyOption.ATOMIC_MOVE);
        closed = true;
        return output;
    }

    private Path eventPath(long number) {
        return directory.resolve("events").resolve(String.format(java.util.Locale.ROOT, "%08d.json", number));
    }

    private static void writeAtomic(Path target, String json) throws IOException {
        Path pending = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(pending, json + "\n", StandardCharsets.UTF_8);
        Files.move(pending, target, StandardCopyOption.ATOMIC_MOVE);
    }
}
