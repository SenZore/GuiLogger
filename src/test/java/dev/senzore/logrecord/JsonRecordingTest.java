package dev.senzore.logrecord;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class JsonRecordingTest {
    @TempDir Path root;

    @Test void persistsEachEventBeforeStopAndExportsInOrder() throws Exception {
        var recording = new JsonRecording(root, new JsonObject());
        var item = new JsonObject();
        item.addProperty("text", "Diamond \"sale\"\nBuy: $1,250 • 日本語");
        recording.append("menu", item);
        item.addProperty("text", "changed");
        recording.append("chat", item);
        var saved = JsonParser.parseString(Files.readString(recording.directory().resolve("events/00000001.json")));
        assertEquals("Diamond \"sale\"\nBuy: $1,250 • 日本語", saved.getAsJsonObject().getAsJsonObject("data").get("text").getAsString());
        var output = JsonParser.parseString(Files.readString(recording.finish("command"))).getAsJsonObject();
        assertEquals(2, output.getAsJsonArray("events").size());
        assertEquals("chat", output.getAsJsonArray("events").get(1).getAsJsonObject().get("type").getAsString());
        assertEquals(2, output.getAsJsonObject("session").get("event_count").getAsInt());
        assertThrows(java.io.IOException.class, () -> recording.append("late", item));
    }

    @Test void rapidStartsCannotOverwriteEarlierSessions() throws Exception {
        var first = new JsonRecording(root, new JsonObject());
        var second = new JsonRecording(root, new JsonObject());
        assertNotEquals(first.directory(), second.directory());
        assertTrue(JsonParser.parseString(Files.readString(first.finish("empty"))).getAsJsonObject()
                .getAsJsonArray("events").isEmpty());
    }

    @Test void failedEventIsNotCountedOrReportedAsSaved() throws Exception {
        var recording = new JsonRecording(root, new JsonObject());
        Files.delete(recording.directory().resolve("events"));
        assertThrows(java.io.IOException.class, () -> recording.append("menu", new JsonObject()));
        assertEquals(0, recording.count());
    }
}
