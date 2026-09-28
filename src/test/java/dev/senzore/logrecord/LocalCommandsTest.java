package dev.senzore.logrecord;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocalCommandsTest {
    @Test void recorderCommandsAndMalformedArgumentsStayLocal() {
        for (String command : new String[] {"logrecord", "/logrecord start", "logrecord stop", "logrecord nonsense", " /LOGRECORD\tstart"})
            assertTrue(LocalCommands.isRecorder(command), command);
        for (String command : new String[] {"shop", "/shop", "logrecording", "say logrecord", "login"})
            assertFalse(LocalCommands.isRecorder(command), command);
    }

    @Test void autocompleteCannotTransmitRecorderInput() {
        for (String input : new String[] {"/logr", "/logre", "/logrecord", "/logrecord st"})
            assertTrue(LocalCommands.isRecorderSuggestion(input), input);
        assertFalse(LocalCommands.isRecorderSuggestion("/shop"));
        assertFalse(LocalCommands.isRecorderSuggestion("/login"));
        assertFalse(LocalCommands.isRecorderSuggestion("/lo"));
    }
}
